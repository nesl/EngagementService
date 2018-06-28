package ucla.nesl.notificationpreference.service.workers;

import android.support.annotation.NonNull;

import java.util.concurrent.TimeUnit;

import ucla.nesl.notificationpreference.alarm.AlarmWorker;
import ucla.nesl.notificationpreference.alarm.NextTrigger;
import ucla.nesl.notificationpreference.notification.NotificationHelper;
import ucla.nesl.notificationpreference.task.scheduler.TaskSchedulerBase;

/**
 * Created by timestring on 5/24/18.
 *
 * `TaskDispatchWorker` schedules when to deliver tasks to users. The strategy is to check every
 * half minute (with half minute tolerance).
 */

public class TaskDispatchWorker extends AlarmWorker {

    private TaskSchedulerBase taskScheduler;
    private NotificationHelper notificationHelper;
    private int previousNotificationID;


    public TaskDispatchWorker(TaskSchedulerBase _taskScheduler,
                              NotificationHelper _notificationHelper) {
        taskScheduler = _taskScheduler;
        notificationHelper = _notificationHelper;

        taskScheduler.feedImmediateTaskHandler(immediateTaskHandler);
    }

    @NonNull
    @Override
    protected NextTrigger onPlan() {
        long now = System.currentTimeMillis();
        while (taskScheduler.hasTasks() && now > taskScheduler.getFirstTask()) {
            taskScheduler.removeFirstTask();
            clearPreviousAndSendNewNotification();
        }

        // check if we need to fire a task every 30 seconds, but another 30 seconds tolerance
        return new NextTrigger(
                TimeUnit.SECONDS.toMillis(30),
                TimeUnit.SECONDS.toMillis(30)
        );
        //return new NextTrigger(5000L, 5000L);
    }

    private void clearPreviousAndSendNewNotification() {
        notificationHelper.cancelNotification(previousNotificationID);
        previousNotificationID = notificationHelper.createAndSendTaskNotification();
    }

    private TaskSchedulerBase.ImmediateTaskHandler immediateTaskHandler
            = new TaskSchedulerBase.ImmediateTaskHandler() {
        public void handleImmediateTask() {
            clearPreviousAndSendNewNotification();
        }
    };
}
