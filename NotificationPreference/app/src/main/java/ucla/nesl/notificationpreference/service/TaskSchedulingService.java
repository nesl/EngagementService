package ucla.nesl.notificationpreference.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import ucla.nesl.notificationpreference.alarm.AlarmEventManager;
import ucla.nesl.notificationpreference.task.PeriodicTaskScheduler;
import ucla.nesl.notificationpreference.task.TaskSchedulerBase;

public class TaskSchedulingService extends Service {

    // Binder
    //private final IBinder mBinder = new LocalBinder();


    private TaskSchedulerBase taskScheduler;


    public TaskSchedulingService() {
    }

    @Override
    public void onCreate() {
        taskScheduler = new PeriodicTaskScheduler(3 * 60);  // 3 minutes

        AlarmEventManager alarmEventManager = new AlarmEventManager(this);
        alarmEventManager.registerWorker(new TaskDispatchWorker(taskScheduler));
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

    public class TaskSchedulingAlarmReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            // For our recurring task, we'll just display a message
            Toast.makeText(context, "I'm running", Toast.LENGTH_SHORT).show();
        }
    }
}
