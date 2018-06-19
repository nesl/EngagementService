package ucla.nesl.notificationpreference.service;

import android.util.Log;
import android.util.SparseIntArray;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.Geofence;

import java.util.HashMap;

import ucla.nesl.notificationpreference.sensing.RingerModeDataCollector;
import ucla.nesl.notificationpreference.sensing.ScreenStatusDataCollector;
import ucla.nesl.notificationpreference.sensing.location.LocationDataCollector;
import ucla.nesl.notificationpreference.sensing.motion.MotionActivityDataCollector;
import ucla.nesl.notificationpreference.task.scheduler.PeriodicTaskScheduler;
import ucla.nesl.notificationpreference.utils.HashUtils;
import ucla.nesl.notificationpreference.utils.If;
import ucla.nesl.notificationpreference.utils.Utils;

/**
 * Created by timestring on 6/14/18.
 */

public class SensorMaster {

    private static final String TAG = PeriodicTaskScheduler.class.getSimpleName();

    private MotionActivityDataCollector motionActivityDataCollector;
    private LocationDataCollector locationDataCollector;
    private RingerModeDataCollector ringerModeDataCollector;
    private ScreenStatusDataCollector screenDataCollector;

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

    // location geofence related
    private HashMap<String, Long> geofenceEnteredTimestamp = new HashMap<>();

    //region Section: Master-level control
    // =============================================================================================
    public SensorMaster(TaskSchedulingService service) {
        motionActivityDataCollector = new MotionActivityDataCollector(
                service, motionActivityCallback);
        locationDataCollector = new LocationDataCollector(service, geofenceCallback);
        ringerModeDataCollector = new RingerModeDataCollector(service);
        screenDataCollector = new ScreenStatusDataCollector(service);
    }

    public void start() {
        resetMotionScore();
        resetGeofenceStatus();

        motionActivityDataCollector.start();
        locationDataCollector.start();
    }

    public void stop() {
        motionActivityDataCollector.stop();
        locationDataCollector.stop();
    }

    public String getStateMessageAndReset() {
        String motionActivity = determineMotionTypeWithinCurrentWindow();
        String location = determineCurrentPlace();
        String ringerMode = ringerModeDataCollector.query();
        String screenStatus = screenDataCollector.query();

        resetMotionScore();

        return Utils.stringJoin(",", motionActivity, location, ringerMode, screenStatus);
    }
    //endregion

    //region Section: Callbacks
    // =============================================================================================
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

    private LocationDataCollector.Callback geofenceCallback = new LocationDataCollector.Callback() {
        @Override
        public void onGeofenceResult(int geofenceTransitionType, String placeCode) {
            switch (geofenceTransitionType) {
                case Geofence.GEOFENCE_TRANSITION_ENTER:
                    geofenceEnteredTimestamp.put(placeCode, System.currentTimeMillis());
                    break;
                case Geofence.GEOFENCE_TRANSITION_EXIT:
                    geofenceEnteredTimestamp.put(placeCode, null);
                    break;
            }
        }
    };
    //endregion

    //region Section: Motion activity computation
    // =============================================================================================
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
    //endregion

    //region Section: Motion activity computation
    // =============================================================================================
    private void resetGeofenceStatus() {
        geofenceEnteredTimestamp.put(LocationDataCollector.PLACE_LABEL_HOME, null);
        geofenceEnteredTimestamp.put(LocationDataCollector.PLACE_LABEL_WORK, null);
    }

    private String determineCurrentPlace() {
        return If.nullThen(HashUtils.argMax(geofenceEnteredTimestamp), "others");
    }
    //endregion
}
