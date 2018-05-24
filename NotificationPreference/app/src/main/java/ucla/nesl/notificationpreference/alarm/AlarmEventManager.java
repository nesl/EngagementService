package ucla.nesl.notificationpreference.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.SparseArray;

import java.util.HashMap;

/**
 * Created by timestring on 5/21/18.
 *
 * `AlarmEventManager` hosts a couple of `AlarmWorker` objects, and an `AlarmWorker` schedules a
 * sequence of events. `AlarmEventManager` interfaces with `AlarmManager` and handles the timing
 * signals. When a signal is intercepted, it notifies the corresponding `AlarmWorker`.
 *
 * I was hoping that there's some sort of API in Android that I can specify "wake me up after X
 * seconds". Unfortunately, from whatever references I found, `AlarmManager` can only signal
 * an Android component (typically a `BroadcastReceiver`) rather than notify the sender. Hence,
 * My workaround is that I redirect the `Intent` from `AlarmManager` to `AlarmProxyReceiver`,
 * and then `AlarmProxyReceiver` will send an `Intent` back to `AlarmEventManager`. (Sign....)
 */

public class AlarmEventManager {

    private static final String TAG = AlarmEventManager.class.getSimpleName();

    static final String INTENT_FORWARD_ALARM_EVENT_ACTION = "intent.forward.alarm.event.action";

    private static final String WORKER_CODE = "worker_code";

    private Context context;
    private AlarmManager alarmManager;

    private HashMap<AlarmWorker, Integer> workerToCode;
    private SparseArray<AlarmWorker> codeToWorker;

    private int nextWorkerCode = 0;


    public AlarmEventManager(Context _context) {
        context = _context;
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        workerToCode = new HashMap<>();
        codeToWorker = new SparseArray<>();

        IntentFilter intentFilter = new IntentFilter(INTENT_FORWARD_ALARM_EVENT_ACTION);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
        localBroadcastManager.registerReceiver(eventReceiver, intentFilter);
    }

    public void registerWorker(AlarmWorker worker) {
        workerToCode.put(worker, nextWorkerCode);
        codeToWorker.put(nextWorkerCode, worker);

        Log.i(TAG, "schedule worker");

        // fire the first event of the registered worker in 5 seconds
        scheduleWorker(worker, new NextTrigger(5000L, 1000L));

        nextWorkerCode++;
    }

    private void scheduleWorker(AlarmWorker worker, NextTrigger trigger) {
        Intent intent = new Intent(context, AlarmProxyReceiver.class);
        int workerCode = workerToCode.get(worker);
        intent.putExtra(WORKER_CODE, workerCode);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, workerCode, intent, 0);
        long firedTime = SystemClock.elapsedRealtime() + trigger.timeIntervalMs;
        //alarmManager.setWindow(AlarmManager.ELAPSED_REALTIME_WAKEUP,
        //        firedTime, trigger.toleranceMs, pendingIntent);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, firedTime    , pendingIntent);
        Log.i(TAG, "A event is going to be fired in " + trigger.timeIntervalMs);
    }

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int workerCode = intent.getIntExtra(WORKER_CODE, -1);
            Log.i(TAG, "Get event " + workerCode);
            AlarmWorker worker = codeToWorker.get(workerCode, null);
            if (worker == null) {
                Log.e(TAG, "Fail to retrieve worker (" + workerCode + ")");
                return;
            }

            NextTrigger trigger = worker.onPlan();
            scheduleWorker(worker, trigger);
        }
    };
}
