package org.example.module3;

import java.time.Duration;
import java.util.concurrent.*;

public class LongRunningTask implements Callable<TaskResponse> {
    private final String name;
    private final int time;
    private final String output;
    private final boolean fail;

    public LongRunningTask(String name, int time, String output, boolean fail) {
        this.name = name;
        this.time = time;
        this.output = output;
        this.fail = fail;
    }

    @Override
    public TaskResponse call() throws Exception {
        long start = System.currentTimeMillis();
        print("Started");

        int numSecs = 0;
        while(numSecs++ < time) {

            if(Thread.interrupted()) throwInterruptedException();
            print("Working for: " + numSecs);

            // process data which uses CPU for 0.2 secs

            try {
                Thread.sleep(Duration.ofSeconds(1));
                // new NetworkCaller(name).makeCall(1);
            }
            catch (InterruptedException e) {
                throwInterruptedException();
            }

            // process data which uses CPU for 0.2 secs
        }
        if(fail) throwExceptionOnFailure();
        print("Completed");
        long end = System.currentTimeMillis();
        return new TaskResponse(output, name, end - start);
    }

    public static void main(String[] args) throws Exception {
        LongRunningTask task = new LongRunningTask("myTask1", 10, "json-response1", true);

        System.out.println("> Main: Started!");
        try(ExecutorService svc = Executors.newFixedThreadPool(2)) {

            Future<TaskResponse> taskFuture = svc.submit(task);
            Thread.sleep(Duration.ofSeconds(5));
            taskFuture.cancel(true);
        }
        System.out.println("> Main: Completed!");
    }

    private void print(String message) {
        System.out.printf("> %s: %s\n", name, message);
    }

    private void throwExceptionOnFailure() {
        print("Failed");
        throw new RuntimeException(name + " : Failed");
    }

    private void throwInterruptedException() {
        print("Interrupted");
        throw new RuntimeException(name + ": Interrupted");
    }
}
