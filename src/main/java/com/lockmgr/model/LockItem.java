package com.lockmgr.model;

import java.util.LinkedList;

public class LockItem {
    private final String name;
    private String currentOwner = null;

    private final LinkedList<String> waitingList = new LinkedList<String>();
    public LockItem(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getCurrentOwner() {
        return currentOwner;
    }

    public void setCurrentOwner(String currentOwner) {
        this.currentOwner = currentOwner;
    }

    public LinkedList<String> getWaitingList() {
        return waitingList;
    }
}
