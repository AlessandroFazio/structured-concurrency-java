package org.example.intro;

import java.util.ArrayList;
import java.util.List;

public class MaxVirtualThreads {
    private static void handleUserRequest() {
        System.out.println("Starting thread: " + Thread.currentThread());
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Ending thread: " + Thread.currentThread());
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Starting main");
        List<Thread> threads = new ArrayList<>();
        for(int i=0; i < 100000; i++) {
            threads.add(startThread());
        }
        System.out.println("Ending main");

        for(Thread thread: threads) thread.join();
    }

    public static Thread startThread() {
        return Thread.startVirtualThread(MaxVirtualThreads::handleUserRequest);
    }
}
