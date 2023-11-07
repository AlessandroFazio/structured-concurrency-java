package org.example.module3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.StructuredTaskScope;

public class AverageWeatherTaskScope extends StructuredTaskScope<TaskResponse> {
    private final List<Subtask<? extends TaskResponse>> successSubTasks =
            Collections.synchronizedList(new ArrayList<>());

    protected void handleComplete(Subtask<? extends TaskResponse> subtask) {
        if (subtask.state().equals(Subtask.State.SUCCESS)) add(subtask);
    }

    private synchronized void add(Subtask<? extends TaskResponse> subtask) {
            successSubTasks.add(subtask);
            if(successSubTasks.size() == 2) this.shutdown();
    }

    public AverageWeatherTaskScope join() throws InterruptedException {
        super.join();
        return this;
    }

    public TaskResponse response() {
        super.ensureOwnerAndJoined();
        if(successSubTasks.size() != 2)
            throw new RuntimeException("At least two subtasks must be successfull");

        TaskResponse r1 = successSubTasks.get(0).get(), r2 = successSubTasks.get(1).get();
        Integer tmp1 = Integer.parseInt(r1.response());
        Integer tmp2 = Integer.parseInt(r2.response());
        return new TaskResponse(
                String.valueOf((tmp1 + tmp2) / 2),
                "Wheather",
                (r1.timeTaken() + r2.timeTaken()) / 2);
    }
}
