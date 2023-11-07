package org.example.module3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;

public class STaskScopeOnFailureExecutor {
    public static <V extends Callable<U>, U> Map<String, U> execute(List<V> tasks)
            throws InterruptedException, ExecutionException {

        List<StructuredTaskScope.Subtask<U>> subtasks = new ArrayList<>();
        Map<String, U> resultMap = new HashMap<>();

        try(var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            for(V task: tasks) subtasks.add(scope.fork(task));
            scope.join();
            scope.throwIfFailed(ExecutionException::new);
            int numTasks = 0;
            for(StructuredTaskScope.Subtask<U> subtask: subtasks) {
                resultMap.put(String.format("task-%d",numTasks++), subtask.get());
            }
            return resultMap;
        }
    }

    public static class ExecutionException extends Exception {
        public ExecutionException(Throwable t) {
            super(t);
        }
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        // Create the tasks
        var dbTask   = new LongRunningTask("dataTask",  3,  "row1", true);
        var restTask = new LongRunningTask("restTask", 10, "json2", false);
        var extTask  = new LongRunningTask("extTask",   5, "json2", false);

        Map<String,TaskResponse> result
                = STaskScopeOnFailureExecutor.execute(List.of(dbTask, extTask, restTask));

        result.forEach((k,v) -> {
            System.out.printf("%s : %s\n", k, v);
        });
    }
}
