package com.lockmgr.model;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class Client {
    final String clientId;
    final Set<String> locks = new ConcurrentSkipListSet<>();
    final Set<String> waitForLock = new ConcurrentSkipListSet<>();


    public Client(String clientId) {
       this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
    }

    public Set<String> getLocks() {
        return locks;
    }

    public Set<String> getWaitForLock() {
        return waitForLock;
    }
}
