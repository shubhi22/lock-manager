package com.lockmgr;

import com.lockmgr.controller.LockManagerController;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LockManagerControllerTest {

    private LockManagerController controller = new LockManagerController();

    @Test public void testLock() {
        controller.lock(Arrays.asList("A", "B", "C"), "123");
        controller.lock(Arrays.asList("A", "B", "D"), "234");
        controller.printClientState();

        controller.getWaitingQueue(Arrays.asList("A", "B", "C"));
        controller.unlock("123");
        controller.printClientState();
        controller.getWaitingQueue(Arrays.asList("A", "B", "C"));
        controller.lock(Arrays.asList("A", "B", "M"), "456");
        controller.getWaitingQueue(Arrays.asList("A", "B", "C", "D", "M"));
        controller.printClientState();

    }

    @Test
    public void testLockWithThreading() throws InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(3);
        Thread thread1 = new Thread(() -> {
            controller.lock(Arrays.asList("A", "B", "C"), "123");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            controller.printClientState();
            controller.getWaitingQueue(Arrays.asList("A", "B", "C"));
            controller.unlock("123");
        });
        Thread thread2 = new Thread(() -> {
            controller.lock(Arrays.asList("A", "B", "D"), "234");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            controller.printClientState();
            controller.getWaitingQueue(Arrays.asList("A", "B", "C", "D"));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            controller.unlock("234");
        });

        Thread thread3 = new Thread(() -> {
            controller.lock(Arrays.asList("A", "B", "M"), "456");

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            controller.getWaitingQueue(Arrays.asList("A", "B", "C", "D", "M"));
            controller.printClientState();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            controller.unlock("456");
        });
        service.submit(thread1);
        service.submit(thread2);
        service.submit(thread3);

        service.awaitTermination(10, TimeUnit.SECONDS);
        service.shutdown();
    }
}
