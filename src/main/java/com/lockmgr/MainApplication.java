package com.lockmgr;

import com.lockmgr.controller.LockManagerController;

import java.util.Arrays;

public class MainApplication {

    private static final LockManagerController controller = new LockManagerController();

    public static void main(String[] args) {
        controller.lock(Arrays.asList("A","B","C"), "123");
        controller.lock(Arrays.asList("A","B","D"), "234");
        controller.printClientState();

        controller.getWaitingQueue(Arrays.asList("A","B","C"));
        controller.unlock("123");
        controller.printClientState();
        controller.getWaitingQueue(Arrays.asList("A","B","C"));
        controller.lock(Arrays.asList("A", "B", "M"), "456");
        controller.getWaitingQueue(Arrays.asList("A","B","C", "D", "M"));
        controller.printClientState();
    }
}
