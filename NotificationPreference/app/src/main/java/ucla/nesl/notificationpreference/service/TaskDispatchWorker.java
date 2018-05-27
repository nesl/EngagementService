package ucla.nesl.notificationpreference.service;

import android.support.annotation.NonNull;
import android.util.Log;

import ucla.nesl.notificationpreference.alarm.AlarmWorker;
import ucla.nesl.notificationpreference.alarm.NextTrigger;
import ucla.nesl.notificationpreference.notification.NotificationHelper;
import ucla.nesl.notificationpreference.task.TaskSchedulerBase;

/**
 * Created by timestring on 5/24/18.
 */

public class TaskDispatchWorker extends AlarmWorker {

    private TaskSchedulerBase taskScheduler;
    private NotificationHelper notificationHelper;


    public TaskDispatchWorker(TaskSchedulerBase _taskScheduler,
                              NotificationHelper _notificationHelper) {
        taskScheduler = _taskScheduler;
        notificationHelper = _notificationHelper;
    }

    @NonNull
    @Override
    protected NextTrigger onPlan() {
        Long nextEvent = taskScheduler.getFirstTask();
        Log.i("TaskDispatchWorker", "check event queue " + nextEvent + "/" + System.currentTimeMillis());
        if (nextEvent != null && System.currentTimeMillis() >= nextEvent) {
            taskScheduler.removeFirstTask();
            notificationHelper.sendNotification(NotificationHelper.Type.LOCATION_CHANGED);
            Log.i("TaskDispatchWorker", "hey we just fired a task!!");
        }

        // check if we need to fire a task every 30 seconds, but another 30 seconds tolerance
        return new NextTrigger(30000L, 30000L);
    }
}
