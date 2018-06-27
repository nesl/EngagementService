package ucla.nesl.notificationpreference.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import ucla.nesl.notificationpreference.alarm.AlarmEventManager;
import ucla.nesl.notificationpreference.notification.INotificationEventListener;
import ucla.nesl.notificationpreference.notification.NotificationHelper;
import ucla.nesl.notificationpreference.notification.enums.NotificationEventType;
import ucla.nesl.notificationpreference.service.workers.DatabaseUploadWorker;
import ucla.nesl.notificationpreference.service.workers.FileUploadWorker;
import ucla.nesl.notificationpreference.service.workers.TaskDispatchWorker;
import ucla.nesl.notificationpreference.service.workers.TaskPlanningWorker;
import ucla.nesl.notificationpreference.storage.SharedPreferenceHelper;
import ucla.nesl.notificationpreference.storage.database.NotificationResponseRecordDatabase;
import ucla.nesl.notificationpreference.storage.loggers.LocationLogger;
import ucla.nesl.notificationpreference.storage.loggers.MotionActivityLogger;
import ucla.nesl.notificationpreference.storage.loggers.NotificationInteractionEventLogger;
import ucla.nesl.notificationpreference.storage.loggers.RingerModeLogger;
import ucla.nesl.notificationpreference.storage.loggers.ScreenStatusLogger;
import ucla.nesl.notificationpreference.task.scheduler.RLTaskScheduler;
import ucla.nesl.notificationpreference.task.scheduler.TaskSchedulerBase;

/**
 * Created by timestring on 5/27/18.
 *
 * `TaskSchedulingService` is the backbone background service for sending notifications, sensor
 * data collection, and all the other peripheral features.
 */
public class TaskSchedulingService extends Service implements INotificationEventListener {

    private final String TAG = TaskSchedulingService.class.getSimpleName();

    // Binder
    private final IBinder binder = new LocalBinder();

    private ConnectivityManager connectivityManager;
    private NotificationHelper notificationHelper;

    private SensorMaster sensorMaster;
    private RewardMaster rewardMaster;

    private AlarmEventManager alarmEventManager;

    private SharedPreferenceHelper keyValueStore;


    private long serviceCreatedTimestamp;

    @Override
    public void onCreate() {

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        notificationHelper = new NotificationHelper(this, true, this);

        sensorMaster = new SensorMaster(this);
        rewardMaster = new RewardMaster();

        keyValueStore = new SharedPreferenceHelper(this);

        if (keyValueStore.getAppStatus() == SharedPreferenceHelper.APP_STATUS_ACTIVE) {
            startSchedulingAndSensing();
        } else {
            notificationHelper.registerAsForegroundService(this, "Off");
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

        //TaskSchedulerBase taskScheduler = new PeriodicTaskScheduler(
        //        (int) TimeUnit.MINUTES.toSeconds(30));
        TaskSchedulerBase taskScheduler = new RLTaskScheduler(keyValueStore, sensorMaster, rewardMaster);

        alarmEventManager = new AlarmEventManager(this);
        alarmEventManager.registerWorker(new TaskDispatchWorker(taskScheduler, notificationHelper));
        alarmEventManager.registerWorker(new TaskPlanningWorker(taskScheduler));
        alarmEventManager.registerWorker(new FileUploadWorker(
                connectivityManager,
                keyValueStore,
                "notification-interaction",
                NotificationInteractionEventLogger.getInstance().getFile()
        ));
        alarmEventManager.registerWorker(new FileUploadWorker(
                connectivityManager,
                keyValueStore,
                "motion",
                MotionActivityLogger.getInstance().getFile()
        ));
        alarmEventManager.registerWorker(new FileUploadWorker(
                connectivityManager,
                keyValueStore,
                "location",
                LocationLogger.getInstance().getFile()
        ));
        alarmEventManager.registerWorker(new FileUploadWorker(
                connectivityManager,
                keyValueStore,
                "ringer-mode",
                RingerModeLogger.getInstance().getFile()
        ));
        alarmEventManager.registerWorker(new FileUploadWorker(
                connectivityManager,
                keyValueStore,
                "screen-status",
                ScreenStatusLogger.getInstance().getFile()
        ));
        alarmEventManager.registerWorker(new DatabaseUploadWorker(
                connectivityManager,
                keyValueStore,
                "task-response",
                NotificationResponseRecordDatabase.getAppDatabase(this)
        ));

        sensorMaster.start();

        notificationHelper.registerAsForegroundService(this, "On");
    }

    private void stopSchedulingAndSensing() {
        if (alarmEventManager == null) {
            Log.i(TAG, "service operation has been stopped");
            return;
        }

        alarmEventManager.terminate();
        alarmEventManager = null;

        sensorMaster.stop();

        notificationHelper.registerAsForegroundService(this, "Off");
    }

    @Override
    public void onNotificationEvent(int notificationID, NotificationEventType event) {
        rewardMaster.feed(event);
    }
}
