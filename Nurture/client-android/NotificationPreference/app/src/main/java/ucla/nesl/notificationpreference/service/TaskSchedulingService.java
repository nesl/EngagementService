package ucla.nesl.notificationpreference.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;

import java.util.concurrent.TimeUnit;

import ucla.nesl.notificationpreference.alarm.AlarmEventManager;
import ucla.nesl.notificationpreference.notification.NotificationHelper;
import ucla.nesl.notificationpreference.sensing.MotionActivityDataCollector;
import ucla.nesl.notificationpreference.service.workers.FileUploadWorker;
import ucla.nesl.notificationpreference.service.workers.TaskDispatchWorker;
import ucla.nesl.notificationpreference.service.workers.TaskPlanningWorker;
import ucla.nesl.notificationpreference.storage.SharedPreferenceHelper;
import ucla.nesl.notificationpreference.storage.loggers.NotificationInteractionEventLogger;
import ucla.nesl.notificationpreference.task.scheduler.PeriodicTaskScheduler;
import ucla.nesl.notificationpreference.task.scheduler.TaskSchedulerBase;

/**
 * Created by timestring on 5/27/18.
 *
 * `TaskSchedulingService` is the backbone background service for sending notifications, sensor
 * data collection, and all the other peripheral features.
 */
public class TaskSchedulingService extends Service {

    private final String TAG = TaskSchedulingService.class.getSimpleName();

    // Binder
    private final IBinder binder = new LocalBinder();

    private ConnectivityManager connectivityManager;
    private NotificationHelper notificationHelper;

    private AlarmEventManager alarmEventManager;

    private MotionActivityDataCollector motionActivityDataCollector;

    private SharedPreferenceHelper keyValueStore;

    private long serviceCreatedTimestamp;

    @Override
    public void onCreate() {

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        notificationHelper = new NotificationHelper(this, true);

        motionActivityDataCollector = new MotionActivityDataCollector(this, motionActivityCallback);

        keyValueStore = new SharedPreferenceHelper(this);

        if (keyValueStore.getAppStatus() == SharedPreferenceHelper.APP_STATUS_ACTIVE) {
            startSchedulingAndSensing();
        }

        serviceCreatedTimestamp = System.currentTimeMillis();
        Log.i("TaskSchedulingService", "created timestamp = " + serviceCreatedTimestamp);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }


    public long getCreatedTimestamp() {
        return serviceCreatedTimestamp;
    }

    public void toggleOperationStatus() {
        switch (keyValueStore.getAppStatus()) {
            case SharedPreferenceHelper.APP_STATUS_ACTIVE:
                stopSchedulingAndSensing();
                keyValueStore.setAppStatus(SharedPreferenceHelper.APP_STATUS_INACTIVE);
                break;

            case SharedPreferenceHelper.APP_STATUS_INACTIVE:
                startSchedulingAndSensing();
                keyValueStore.setAppStatus(SharedPreferenceHelper.APP_STATUS_ACTIVE);
                break;
        }
    }

    public class LocalBinder extends Binder {
        public TaskSchedulingService getService() {
            return TaskSchedulingService.this;
        }
    }

    private void startSchedulingAndSensing() {
        if (alarmEventManager != null) {
            Log.i(TAG, "service operation has been started");
            return;
        }

        TaskSchedulerBase taskScheduler = new PeriodicTaskScheduler(
                (int) TimeUnit.MINUTES.toSeconds(30));

        alarmEventManager = new AlarmEventManager(this);
        alarmEventManager.registerWorker(new TaskDispatchWorker(taskScheduler, notificationHelper));
        alarmEventManager.registerWorker(new TaskPlanningWorker(taskScheduler));
        alarmEventManager.registerWorker(new FileUploadWorker(
                connectivityManager,
                keyValueStore,
                "notification-interaction",
                NotificationInteractionEventLogger.getInstance()
        ));

        motionActivityDataCollector.start();
    }

    private void stopSchedulingAndSensing() {
        if (alarmEventManager == null) {
            Log.i(TAG, "service operation has been stopped");
            return;
        }

        alarmEventManager.terminate();
        alarmEventManager = null;

        motionActivityDataCollector.stop();
    }

    private MotionActivityDataCollector.Callback motionActivityCallback
            = new MotionActivityDataCollector.Callback() {
        @Override
        public void onMotionActivityResult(ActivityRecognitionResult result) {

        }
    };
}
