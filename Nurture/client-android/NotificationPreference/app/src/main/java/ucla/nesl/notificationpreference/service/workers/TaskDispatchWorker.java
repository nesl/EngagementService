package ucla.nesl.notificationpreference.service.workers;

import android.support.annotation.NonNull;

import java.util.concurrent.TimeUnit;

import ucla.nesl.notificationpreference.alarm.AlarmWorker;
import ucla.nesl.notificationpreference.alarm.NextTrigger;
import ucla.nesl.notificationpreference.notification.NotificationHelper;
import ucla.nesl.notificationpreference.storage.database.NotificationResponseRecordDatabase;
import ucla.nesl.notificationpreference.task.scheduler.TaskSchedulerBase;

/**
 * Created by timestring on 5/24/18.
 *
 * `TaskDispatchWorker` schedules when to deliver tasks to users. The strategy is to check every
 * half minute (with half minute tolerance).
 */

public class TaskDispatchWorker extends AlarmWorker {

    private static final long NOTIFICATION_EXPIRATION_TIME_SPAN = TimeUnit.HOURS.toMillis(1);

    private static final int NOTIFICATION_NOT_SET = -1;

    private TaskSchedulerBase taskScheduler;
    private NotificationHelper notificationHelper;
    NotificationResponseRecordDatabase database;

    private int previousNotificationID = NOTIFICATION_NOT_SET;
    private long previousNotificationCreatedTime;


    public TaskDispatchWorker(TaskSchedulerBase _taskScheduler,
                              NotificationHelper _notificationHelper,
                              NotificationResponseRecordDatabase _database) {
        taskScheduler = _taskScheduler;
        notificationHelper = _notificationHelper;
        database = _database;

        taskScheduler.feedImmediateTaskHandler(immediateTaskHandler);
    }

    @NonNull
    @Override
    protected NextTrigger onPlan() {
        long now = System.currentTimeMillis();

        // expire old notifications
        if (previousNotificationID != NOTIFICATION_NOT_SET &&
                now - previousNotificationCreatedTime > NOTIFICATION_EXPIRATION_TIME_SPAN) {
            clearPreviousNotification();
        }

        // fire new task
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

    @Override
    protected boolean requireBackgroundExecution() {
        return true;
    }

    private void clearPreviousAndSendNewNotification() {
        // clear
        clearPreviousNotification();
        database.expireOutDatedNotifications(0L);

        // create
        previousNotificationID = notificationHelper.createAndSendTaskNotification();
        previousNotificationCreatedTime = System.currentTimeMillis();
    }

    private void clearPreviousNotification() {
        notificationHelper.cancelNotification(previousNotificationID);
        database.expireOneNotification(previousNotificationID);
        previousNotificationID = NOTIFICATION_NOT_SET;
    }

    private TaskSchedulerBase.ImmediateTaskHandler immediateTaskHandler
            = new TaskSchedulerBase.ImmediateTaskHandler() {
        public void handleImmediateTask() {
            clearPreviousAndSendNewNotification();
        }
    };
}
