package ucla.nesl.notificationpreference.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.android.gms.location.ActivityRecognitionResult;

import ucla.nesl.notificationpreference.alarm.AlarmEventManager;
import ucla.nesl.notificationpreference.notification.NotificationHelper;
import ucla.nesl.notificationpreference.sensing.MotionActivityDataCollector;
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

    private MotionActivityDataCollector motionActivityDataCollector;


    @Override
    public void onCreate() {
        taskScheduler = new PeriodicTaskScheduler(1 * 60);  // 10 minutes

        notificationHelper = new NotificationHelper(this, true);

        motionActivityDataCollector = new MotionActivityDataCollector(this, motionActivityCallback);

        AlarmEventManager alarmEventManager = new AlarmEventManager(this);
        alarmEventManager.registerWorker(new TaskDispatchWorker(taskScheduler, notificationHelper));
        alarmEventManager.registerWorker(new TaskPlanningWorker(taskScheduler));

        motionActivityDataCollector.start();
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

    private MotionActivityDataCollector.Callback motionActivityCallback
            = new MotionActivityDataCollector.Callback() {
        @Override
        public void onMotionActivityResult(ActivityRecognitionResult result) {

        }
    };
}
