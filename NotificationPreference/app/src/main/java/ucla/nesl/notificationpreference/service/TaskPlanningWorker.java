package ucla.nesl.notificationpreference.service;

import android.support.annotation.NonNull;

import ucla.nesl.notificationpreference.alarm.AlarmWorker;
import ucla.nesl.notificationpreference.alarm.NextTrigger;
import ucla.nesl.notificationpreference.task.TaskSchedulerBase;

/**
 * Created by timestring on 5/24/18.
 */

public class TaskPlanningWorker extends AlarmWorker {

    private TaskSchedulerBase taskScheduler;

    public TaskPlanningWorker(TaskSchedulerBase _taskScheduler) {
        taskScheduler = _taskScheduler;
    }

    @NonNull
    @Override
    protected NextTrigger onPlan() {
        taskScheduler.onPlan();

        int intervalSec = taskScheduler.checkDecisionIntervalSec();
        int toleranceSec = Math.min(60, intervalSec);  // be no more than 1 minute
        return new NextTrigger(intervalSec * 1000L, toleranceSec * 1000L);
    }
}
