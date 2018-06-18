package ucla.nesl.notificationpreference.service;

import android.util.Log;
import android.util.SparseIntArray;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import ucla.nesl.notificationpreference.sensing.MotionActivityDataCollector;
import ucla.nesl.notificationpreference.sensing.RingerModeDataCollector;
import ucla.nesl.notificationpreference.task.scheduler.PeriodicTaskScheduler;
import ucla.nesl.notificationpreference.utils.HashUtils;
import ucla.nesl.notificationpreference.utils.Utils;

/**
 * Created by timestring on 6/14/18.
 */

public class SensorMaster {

    private static final String TAG = PeriodicTaskScheduler.class.getSimpleName();

    private MotionActivityDataCollector motionActivityDataCollector;
    private RingerModeDataCollector ringerModeDataCollector;

    // motion activity related
    private static final int[] MOTION_ACTIVITY_OF_INTEREST = {
            DetectedActivity.STILL,
            DetectedActivity.WALKING,
            DetectedActivity.RUNNING,
            DetectedActivity.IN_VEHICLE,
            DetectedActivity.ON_BICYCLE
    };
    private String lastMotionActivity = motionActivityTypeToString(DetectedActivity.STILL);
    private SparseIntArray motionConfidenceSum = new SparseIntArray();
    private int motionActivityCount;

    public SensorMaster(TaskSchedulingService service) {
        motionActivityDataCollector = new MotionActivityDataCollector(
                service, motionActivityCallback);
        ringerModeDataCollector = new RingerModeDataCollector(service);
    }

    public void start() {
        motionActivityDataCollector.start();
    }

    public void stop() {
        motionActivityDataCollector.stop();
    }

    public String getStateMessageAndReset() {
        String motionActivity = determineMotionTypeWithinCurrentWindow();
        String ringerMode = ringerModeDataCollector.query();

        resetMotionScore();

        return Utils.stringJoin(",", motionActivity, ringerMode);
    }

    private MotionActivityDataCollector.Callback motionActivityCallback
            = new MotionActivityDataCollector.Callback() {
        @Override
        public void onMotionActivityResult(ActivityRecognitionResult result) {
            for (int motionActivityType : MOTION_ACTIVITY_OF_INTEREST) {
                HashUtils.addAssign(motionConfidenceSum, motionActivityType,
                        result.getActivityConfidence(motionActivityType));
            }
            motionActivityCount++;
        }
    };

    private void resetMotionScore() {
        for (int motionActivityType : MOTION_ACTIVITY_OF_INTEREST) {
            motionConfidenceSum.put(motionActivityType, 0);
        }
        motionActivityCount = 0;
    }

    private String motionActivityTypeToString(int type) {
        switch (type) {
            case DetectedActivity.STILL:
                return "still";
            case DetectedActivity.WALKING:
                return "walking";
            case DetectedActivity.RUNNING:
                return "running";
            case DetectedActivity.IN_VEHICLE:
                return "driving";
            case DetectedActivity.ON_BICYCLE:
                return "biking";
            default:
                throw new IllegalArgumentException("Type not in list");
        }
    }

    private String determineMotionTypeWithinCurrentWindow() {
        if (motionActivityCount == 0) {
            Log.w(TAG, "No motion activity samples in the previous window");
            return lastMotionActivity;
        } else {
            return motionActivityTypeToString(HashUtils.argMax(motionConfidenceSum));
        }
    }
}
