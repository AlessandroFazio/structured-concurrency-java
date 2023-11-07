package org.example.module1;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class VirtualMethodsPlay {
    public static void main(String[] args) throws Exception {
        System.out.println("starting main");
        playWithExecutorService();
        System.out.println("ending main");
    }

    private static void handleUserRequest() {
        System.out.println("Starting thread " + Thread.currentThread());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Ending thread: " + Thread.currentThread());
    }

    private static void playWithVirtualBuilder() throws Exception {
        Thread vThread = Thread.startVirtualThread(() -> new Runnable(){
            @Override
            public void run() {
                System.out.println("Hello world!");
            }
        });

        Thread.Builder.OfVirtual vBuilder = Thread.ofVirtual()
                .name("userthread", 0);

        Thread vThread1 = vBuilder.start(VirtualMethodsPlay::handleUserRequest);
        Thread vThread2 = vBuilder.start(VirtualMethodsPlay::handleUserRequest);

        vThread1.join();
        vThread2.join();
    }

    public static void playWithThreadFactory() throws InterruptedException {
        ThreadFactory factory = Thread.ofVirtual().name("userthread", 0).factory();

        Thread vThread1 = factory.newThread(VirtualMethodsPlay::handleUserRequest);
        vThread1.start();
        Thread vThread2 = factory.newThread(VirtualMethodsPlay::handleUserRequest);
        vThread2.start();

        vThread1.join();
        vThread2.join();
    }

    public static void playWithExecutorServiceFactory() {
        ThreadFactory factory = Thread.ofVirtual().name("userthread", 0).factory();
        try(ExecutorService svc = Executors.newThreadPerTaskExecutor(factory)) {
            svc.submit(VirtualMethodsPlay::handleUserRequest);
            svc.submit(VirtualMethodsPlay::handleUserRequest);
        }
    }

    public static void playWithExecutorService() {
        try(ExecutorService svc = Executors.newVirtualThreadPerTaskExecutor()) {
            svc.submit(VirtualMethodsPlay::handleUserRequest);
            svc.submit(VirtualMethodsPlay::handleUserRequest);
        }
    }
}
