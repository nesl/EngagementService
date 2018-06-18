package ucla.nesl.notificationpreference.sensing;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityRecognitionResult;

import java.util.concurrent.TimeUnit;

import ucla.nesl.notificationpreference.storage.loggers.MotionActivityLogger;

/**
 * Originally created by timestring on 2/15/18.
 * Modified on 6/14/18.
 *
 * To get motion activity via Google API.
 *
 * Please note that currently, we assume that no two `MotionActivityDataCollector` objects will
 * co-exist at any given time.
 */

public class MotionActivityDataCollector {

    private static final long MOTION_ACTIVITY_FETCH_FREQUENCY_MS = TimeUnit.SECONDS.toMillis(10);

    public static final String INTENT_FORWARD_MOTION_ACTIVITY_RESULT = "intent.forward.motion.activity.result";
    public static final String INTENT_EXTRA_NAME_ACTIVITY_RESULT = "activityResult";
    private static final int PENDING_INTENT_REQUEST_CODE = 0;

    private MotionActivityDataCollector.Callback motionActivityCallback;

    private ActivityRecognitionClient activityRecognitionClient;
    private PendingIntent activityRecognitionPendingIntent;

    private MotionActivityLogger logger;


    public MotionActivityDataCollector(
            Context context, MotionActivityDataCollector.Callback callback) {

        motionActivityCallback = callback;
        logger = MotionActivityLogger.getInstance();

        activityRecognitionClient = ActivityRecognition.getClient(context);

        Intent intent = new Intent(context, MotionActivityProxyIntentService.class);
        activityRecognitionPendingIntent = PendingIntent.getService(
                context, PENDING_INTENT_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        LocalBroadcastManager.getInstance(context).registerReceiver(motionActivityReceiver,
                new IntentFilter(INTENT_FORWARD_MOTION_ACTIVITY_RESULT));
    }

    public void start() {
        activityRecognitionClient.requestActivityUpdates(
                MOTION_ACTIVITY_FETCH_FREQUENCY_MS, activityRecognitionPendingIntent);
    }

    public void stop() {
        activityRecognitionClient.removeActivityUpdates(activityRecognitionPendingIntent);
    }


    private BroadcastReceiver motionActivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ActivityRecognitionResult activityRecognitionResult = intent.getParcelableExtra(
                    INTENT_EXTRA_NAME_ACTIVITY_RESULT);
            logger.log(activityRecognitionResult);
            motionActivityCallback.onMotionActivityResult(activityRecognitionResult);
        }
    };


    public interface Callback {
        void onMotionActivityResult(ActivityRecognitionResult result);
    }

}