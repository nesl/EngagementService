package ucla.nesl.notificationpreference.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import ucla.nesl.notificationpreference.alarm.AlarmEventManager;
import ucla.nesl.notificationpreference.notification.NotificationHelper;
import ucla.nesl.notificationpreference.task.scheduler.PeriodicTaskScheduler;
import ucla.nesl.notificationpreference.task.scheduler.TaskSchedulerBase;

/**
 * Created by timestring on 5/27/18.
 *
 * `TaskSchedulingService` is the backbone background service for sending notifications, sensor
 * data collection, and all the other peripheral features.
 */
public class TaskSchedulingService extends Service {

    // Binder
    //private final IBinder mBinder = new LocalBinder();

    private NotificationHelper notificationHelper;

    private TaskSchedulerBase taskScheduler;

    @Override
    public void onCreate() {
        taskScheduler = new PeriodicTaskScheduler(1 * 60);  // 10 minutes

        notificationHelper = new NotificationHelper(this, true);

        AlarmEventManager alarmEventManager = new AlarmEventManager(this);
        alarmEventManager.registerWorker(new TaskDispatchWorker(taskScheduler, notificationHelper));
        alarmEventManager.registerWorker(new TaskPlanningWorker(taskScheduler));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    //public class LocalBinder extends Binder {
    //    public TaskSchedulingService getService() {
    //        return TaskSchedulingService.this;
    //    }
    //}
}
