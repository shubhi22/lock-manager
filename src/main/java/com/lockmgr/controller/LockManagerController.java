package com.lockmgr.controller;

import com.lockmgr.model.Client;
import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;
import com.lockmgr.model.LockItem;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class LockManagerController {

    //maintains clientId and client
    private final ConcurrentHashMap<String, Client> clients = new ConcurrentHashMap<String, Client>();
    //maintains lockItemId and client
    private final ConcurrentHashMap<String, LockItem> locks = new ConcurrentHashMap<String, LockItem>();
    private static final Logger LOGGER = LoggerFactory.getLogger(LockManagerController.class);

    public void lock(List<String> lockKeys, String clientId) {
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

            if (lockItem.getCurrentOwner() == null) {
                lockItem.setCurrentOwner(client.getClientId());
                client.getLocks().add(lockKey);
                System.out.println("Lock Acquired; ClientId= " + clientId + " , ItemId= " + lockKey);
            } else { // lock is held by other, wait in the queue
                lockItem.getWaitingList().add(client.getClientId());
                client.getWaitForLock().add(lockKey);
                System.out.println("Lock is already held by others - waiting in queue - client=" + client.getClientId() + " Item= " + lockItem.getName());
            }
        }
    }

    public void unlock(String clientId) {
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
                    System.out.println("Lock on Item=" + lockKey + " is now assigned to Client= " + lockItem.getCurrentOwner() + " as part of cleanup.");
                } else {
                    // no one is waiting
                    // delete the lock, return ok
                    locks.remove(lockItem);
                    client.getLocks().remove(lockKey);
                    System.out.println(String.format("Delete the idle lock - lock=%s", lockKey));
                }
            }
        }
    }

    public void getWaitingQueue(List<String> lockKeys) {
        for (String lockKey : lockKeys) {
            if (!locks.containsKey(lockKey)) {
                continue;
            }
            LockItem item = locks.get(lockKey);
            System.out.println("Item=" + item.getName() + " ;Wait List Clients: " + item.getWaitingList().toString());
        }
    }

    public void printClientState() {
        for(Client client : clients.values()) {
            System.out.println("Client= " + client.getClientId() + " ;Locks: " + client.getLocks() + " WaitFor : " + client.getWaitForLock());
        }
    }

}
