package ucla.nesl.notificationpreference.sensing;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;

/**
 * Created by timestring on 6/2/18.
 *
 * The `MotionActivityProxyIntentService` forwards the result from `ActivityRecognition` to
 * `MotionActivityDataCollector`
 */
public class MotionActivityProxyIntentService extends IntentService {
    private static final String TAG = MotionActivityProxyIntentService.class.getSimpleName();

    public MotionActivityProxyIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent passedIntent) {
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(passedIntent);

        Log.i(TAG, "activities detected: " + result.getMostProbableActivity().toString());

        Intent forwardedIntent = new Intent(
                MotionActivityDataCollector.INTENT_FORWARD_ACTIVITY_RESULT);
        forwardedIntent.putExtra(
                MotionActivityDataCollector.INTENT_EXTRA_NAME_ACTIVITY_RESULT, result);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(forwardedIntent);
    }
}
