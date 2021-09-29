package com.lockmgr.services;

import com.lockmgr.exception.DeadlockException;
import com.lockmgr.model.Client;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WaitForGraph {
    //client dependencies
    private final ConcurrentMap<String, Set<String>> adjacencyList = new ConcurrentHashMap<>();
    private final Lock reentrantLock = new ReentrantLock();
    private final Lock exclusiveLockForCycles = new ReentrantLock();

    public void add(String predecessor, String successor) {
        reentrantLock.lock();
        try {
            Set<String> txnList = adjacencyList.getOrDefault(predecessor, new ConcurrentSkipListSet<>());
            txnList.add(successor);
            adjacencyList.put(predecessor, txnList);
        } finally {
            reentrantLock.unlock();
        }
    }

    public void remove(String clientId) {
        reentrantLock.lock();
        try {
            adjacencyList.remove(clientId);
            removeSuccessor(clientId);
        } finally {
            reentrantLock.unlock();
        }
    }

    public boolean hasEdge(String client1, String client2) {
        Set<String> txnList = adjacencyList.get(client1);
        if (txnList == null) return false;
        return txnList.contains(client2);
    }


    public List<List<String>> findCycles() {
        exclusiveLockForCycles.lock();
        try {
            GraphDepthFirstSearch dfs = new GraphDepthFirstSearch(adjacencyList);
            dfs.start();
            return dfs.getCycles();
        } finally {
            exclusiveLockForCycles.unlock();
        }
    }

    public void detectDeadlock(Client currentClient) throws DeadlockException {
        List<List<String>> cycles = findCycles();

        for (List<String> cycleGroup : cycles) {
            if (cycleGroup.contains(currentClient.getClientId())) {
                System.out.println();
                throw new DeadlockException(Thread.currentThread().getName() + " :: Deadlock detected - Aborting current client transaction : " + currentClient.getClientId());
            }
        }
    }

    private void removeSuccessor(String clientToRemove) {
        for (String predecessor : adjacencyList.keySet()) {
            Set<String> successors = adjacencyList.get(predecessor);
            if (successors != null) {
                successors.remove(clientToRemove);
            }
        }
    }
}
