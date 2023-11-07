package org.example.intro;

public class MainJacket {
    private static void handleUserRequest() {
        System.out.println("Starting thread: " + Thread.currentThread());
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Ending thread: " + Thread.currentThread());
    }

    public static void main(String[] args) {
        System.out.println("Starting main");
        for(int i=0; i < 100000; i++) {
            new Thread(MainJacket::handleUserRequest).start();
        }
        System.out.println("Ending main");
    }
}
