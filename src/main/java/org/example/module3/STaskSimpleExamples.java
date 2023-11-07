package org.example.module3;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.StructuredTaskScope;

public class STaskSimpleExamples {
    public static void main(String[] args) throws Exception {
        System.out.println("Main : Started");
        // interruptMain();
        // exampleCompleteAllTasks();
        // exampleShutDownOnFailure();
        // exampleShutDownOnSuccess();
        exampleCustomTaskScope();
        System.out.println("Main : Completed");
    }

    private static void exampleCompleteAllTasks() throws Exception {
        try(var scope = new StructuredTaskScope<TaskResponse>()) {
            var expTask = new LongRunningTask("expedia-task", 3, "100$", true);
            var hotTask = new LongRunningTask("hotwire-task", 10, "100$", true);

            StructuredTaskScope.Subtask<TaskResponse> expSubTask = scope.fork(expTask);
            StructuredTaskScope.Subtask<TaskResponse> hotSubTask = scope.fork(hotTask);

            if(true) {
                Thread.sleep(Duration.ofSeconds(2));
                throw new RuntimeException("Some Exception");
            }
            scope.join();

            StructuredTaskScope.Subtask.State expTaskState = expSubTask.state();
            if(expTaskState.equals(StructuredTaskScope.Subtask.State.SUCCESS)) {
                System.out.println(expSubTask.get());
            } else if(expTaskState.equals(StructuredTaskScope.Subtask.State.FAILED)) {
                System.out.println(expSubTask.exception().toString());
            }

            StructuredTaskScope.Subtask.State hotTaskState = hotSubTask.state();
            if(hotTaskState.equals(StructuredTaskScope.Subtask.State.SUCCESS)) {
                System.out.println(hotSubTask.get());
            } else if (hotTaskState.equals(StructuredTaskScope.Subtask.State.FAILED)) {
                System.out.println(hotSubTask.exception().toString());
            }
        }
    }

    private static void interruptMain() {
        Thread mainThread = Thread.currentThread();
        Thread.ofPlatform().start(() -> {
            try {
                Thread.sleep(Duration.ofSeconds(2));
                System.out.println(
                        Thread.currentThread().getName() +
                                " is interrupting the " + mainThread.getName());
                mainThread.interrupt();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private static void exampleShutDownOnFailure() throws Exception {
        try(var scope = new StructuredTaskScope.ShutdownOnFailure()) {

            var dataTask = new LongRunningTask("dataTask", 3, "row1", true);
            var restTask = new LongRunningTask("restTask", 10, "json2", false);

            StructuredTaskScope.Subtask<TaskResponse> dataSubTask = scope.fork(dataTask);
            StructuredTaskScope.Subtask<TaskResponse> restSubTask = scope.fork(restTask);

            //Wait till first Child task fails, Send Cancellation to
            // all other children
            scope.join();
            scope.throwIfFailed(Exception::new);

            // Handle Success Task Results
            System.out.println(dataSubTask.get());
            System.out.println(restSubTask.get());
        }
    }

    private static void exampleShutDownOnSuccess() throws Exception {
        try(var scope = new StructuredTaskScope.ShutdownOnSuccess<TaskResponse>()) {
            var wthr1Task = new LongRunningTask("Weather-1", 3, "32", true);
            var wthr2Task = new LongRunningTask("Weather-2", 10, "30", true);

            StructuredTaskScope.Subtask<TaskResponse> wthr1SubTask = scope.fork(wthr1Task);
            StructuredTaskScope.Subtask<TaskResponse> wthr2SubTask = scope.fork(wthr2Task);

            scope.join();

            TaskResponse result = scope.result(Exception::new);
            System.out.println("result = " + result);
        }
    }

    private static void exampleCustomTaskScope() throws InterruptedException {
        try(var scope = new AverageWeatherTaskScope()) {

            // Create the tasks
            var wTask1 = new LongRunningTask("Weather-1", 3, "30", true);
            var wTask2 = new LongRunningTask("Weather-2", 4, "32", true);
            var wTask3 = new LongRunningTask("Weather-3", 6, "34", true);
            var wTask4 = new LongRunningTask("Weather-4", 5, "39", true);
            var wTask5 = new LongRunningTask("Weather-5", 10, "20", true);

            // Submit the Tasks and get the Subtask Object
            StructuredTaskScope.Subtask<TaskResponse> wSubTask1 = scope.fork(wTask1);
            StructuredTaskScope.Subtask<TaskResponse> wSubTask2 = scope.fork(wTask2);
            StructuredTaskScope.Subtask<TaskResponse> wSubTask3 = scope.fork(wTask3);
            StructuredTaskScope.Subtask<TaskResponse> wSubTask4 = scope.fork(wTask4);
            StructuredTaskScope.Subtask<TaskResponse> wSubTask5 = scope.fork(wTask5);

            // Wait for first 2 results
            scope.join();

            // Return the average of the first 2
            TaskResponse response = scope.response();
            System.out.println("response = " + response);
        }

    }
}
