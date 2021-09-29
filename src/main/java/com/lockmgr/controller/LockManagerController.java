package com.lockmgr.controller;

import com.lockmgr.exception.DeadlockException;
import com.lockmgr.model.Client;

import com.lockmgr.model.LockItem;
import com.lockmgr.services.WaitForGraph;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class LockManagerController {

    //maintains clientId and client
    private final ConcurrentHashMap<String, Client> clients = new ConcurrentHashMap<String, Client>();
    //maintains lockItemId and item
    private final ConcurrentHashMap<String, LockItem> locks = new ConcurrentHashMap<String, LockItem>();
    private final WaitForGraph waitForGraph = new WaitForGraph();

    public void lock(List<String> lockKeys, String clientId) {
        System.out.println(String.format("%s : ---------------------------------------- Attempting to acquire locks %s for client %s - START ------------------------", Thread.currentThread().getName(), lockKeys, clientId));
        Client client = null;
        if (!clients.containsKey(clientId)) {
            client = new Client(clientId);
            clients.putIfAbsent(clientId, client);
        } else {
            client = clients.get(clientId);
        }

        for (String lockKey : lockKeys) {
            if (!locks.containsKey(lockKey)) {
                locks.putIfAbsent(lockKey, new LockItem(lockKey));
            }
            LockItem lockItem = locks.get(lockKey);
            synchronized (this) {
                if (lockItem.getCurrentOwner() == null) {
                    lockItem.setCurrentOwner(client.getClientId());
                    client.getLocks().add(lockKey);
                    System.out.println(Thread.currentThread().getName() + " :: Lock Acquired; ClientId= " + clientId + " , ItemId= " + lockKey);
                } else { // lock is held by other, wait in the queue
                    lockItem.getWaitingList().add(client.getClientId());
                    client.getWaitForLock().add(lockKey);
                    System.out.println(Thread.currentThread().getName() + " :: Lock is already held by others - waiting in queue - client=" + client.getClientId() + " Item= " + lockItem.getName());
                    waitForGraph.add(client.getClientId(), lockItem.getCurrentOwner());
                    try {
                        waitForGraph.detectDeadlock(client);
                    } catch (DeadlockException e) {
                        System.out.println(e.getMessage());
                        unlock(client.getClientId());
                    }

                }
            }
        }
        System.out.println(String.format("%s : ---------------------------------------- Attempting to acquire locks %s for client %s - END ------------------------", Thread.currentThread().getName(), lockKeys, clientId));

    }

    public void unlock(String clientId) {
        System.out.println(String.format("%s : ---------------------------------------- Unlocking locks for client %s - START ------------------------", Thread.currentThread().getName(), clientId));
        Client client = clients.get(clientId);
        if (client.getLocks().isEmpty()) {
            throw new IllegalArgumentException("No lock exists for given client: " + clientId);
        }
        for (String lockKey : client.getLocks()) {
            LockItem lockItem = locks.get(lockKey);
            if (lockItem == null) {
                throw new IllegalArgumentException("Invalid Lock Key : " + lockKey);
            }
            if (lockItem.getCurrentOwner() != null && lockItem.getCurrentOwner().equals(clientId)) {
                if (!lockItem.getWaitingList().isEmpty()) {
                    // some one is waiting
                    // clean myself; assign the lock to the first waiting one
                    client.getLocks().remove(lockKey);
                    String newOwner = lockItem.getWaitingList().removeFirst();
                    lockItem.setCurrentOwner(newOwner);
                    Client newOwnerClient = clients.get(newOwner);
                    newOwnerClient.getWaitForLock().clear();
                    newOwnerClient.getLocks().add(lockKey);
                    System.out.println(Thread.currentThread().getName() + " :: Lock on Item=" + lockKey + " is now assigned to Client= " + lockItem.getCurrentOwner() + " as part of cleanup.");
                } else {
                    // no one is waiting
                    // delete the lock, return ok
                    locks.remove(lockItem);
                    client.getLocks().remove(lockKey);
                    System.out.println(Thread.currentThread().getName() + " :: " + String.format("Delete the idle lock - lock=%s", lockKey));
                }
            }
        }
        client.getWaitForLock().clear();
        System.out.println(String.format("%s : ---------------------------------------- Unlocking locks for client %s - END ------------------------", Thread.currentThread().getName(), clientId));
    }

    public void getWaitingQueue(List<String> lockKeys) {
        System.out.println(String.format("%s : ---------------------------------------- Printing Waiting List for Requested Items - START ------------------------", Thread.currentThread().getName()));
        for (String lockKey : lockKeys) {
            if (!locks.containsKey(lockKey)) {
                continue;
            }
            LockItem item = locks.get(lockKey);
            System.out.println(Thread.currentThread().getName() + " :: Item=" + item.getName() + " ;Wait List Clients: " + item.getWaitingList().toString());
        }
        System.out.println(String.format("%s : ---------------------------------------- Printing Waiting List for Requested Items - END ------------------------", Thread.currentThread().getName()));
    }

    public void printClientState() {
        System.out.println(String.format("%s : ---------------------------------------- Printing Client States - START ------------------------", Thread.currentThread().getName()));
        for (Client client : clients.values()) {
            System.out.println(Thread.currentThread().getName() + " :: Client= " + client.getClientId() + " ;Locks: " + client.getLocks() + " WaitFor : " + client.getWaitForLock());
        }
        System.out.println(String.format("%s : ---------------------------------------- Printing Client States - END ------------------------", Thread.currentThread().getName()));

    }

}
