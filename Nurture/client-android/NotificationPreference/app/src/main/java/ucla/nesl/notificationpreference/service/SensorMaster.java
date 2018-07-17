package ucla.nesl.notificationpreference.service;

import android.util.Log;
import android.util.SparseIntArray;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.Geofence;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import ucla.nesl.notificationpreference.notification.INotificationEventListener;
import ucla.nesl.notificationpreference.notification.NotificationHelper;
import ucla.nesl.notificationpreference.notification.enums.NotificationEventType;
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
 *
 * `SensorMaster` has a pool of data collectors and focus on sensor data handling, especially some
 * data collectors are push-based (a callback is required) while others are pull-based.
 * `SensorMaster` provides a nice interface to `TaskSchedulingService` to summarize what the current
 * state is.
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

    // notification status related
    private NotificationHelper notificationHelper;
    private long lastNotificationTime;

    //region Section: Master-level control
    // =============================================================================================
    public SensorMaster(TaskSchedulingService service) {
        motionActivityDataCollector = new MotionActivityDataCollector(
                service, motionActivityCallback);
        locationDataCollector = new LocationDataCollector(service, geofenceCallback);
        ringerModeDataCollector = new RingerModeDataCollector(service);
        screenDataCollector = new ScreenStatusDataCollector(service);

        // initialize notification status related
        notificationHelper = new NotificationHelper(service, false, notificationEventListener);
        lastNotificationTime = 0L;
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
        String percentageOfDay = String.valueOf(getPercentageOfTheDay());
        String percentageOfWeek = String.valueOf(getPercentageOfTheWeek());
        String motionActivity = determineMotionTypeWithinCurrentWindow();
        String location = determineCurrentPlace();
        String notificationTimeElapsed = String.valueOf(getLastNotificationElapsedTimeInMinute());
        String ringerMode = ringerModeDataCollector.query();
        String screenStatus = screenDataCollector.query();

        resetMotionScore();

        return Utils.stringJoin(",", percentageOfDay, percentageOfWeek, motionActivity,
                location, notificationTimeElapsed, ringerMode, screenStatus);
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

    private INotificationEventListener notificationEventListener
            = new INotificationEventListener() {
        @Override
        public void onNotificationEvent(int notificationID, NotificationEventType event) {
            if (event == NotificationEventType.CREATED) {
                lastNotificationTime = System.currentTimeMillis();
            }
        }
    };
    //endregion

    //region Section: Time/day computation
    // =============================================================================================
    private double getPercentageOfTheWeek() {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(new Date());
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
        return ((double) dayOfWeek + getPercentageOfTheDay()) / 7.0;
    }

    private double getPercentageOfTheDay() {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(new Date());
        long secondsOfTheDay = TimeUnit.HOURS.toSeconds(calendar.get(Calendar.HOUR_OF_DAY))
                + TimeUnit.MINUTES.toSeconds(calendar.get(Calendar.MINUTE))
                + calendar.get(Calendar.SECOND);
        return (double) secondsOfTheDay / TimeUnit.DAYS.toSeconds(1);
    }
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

    //region Section: Geofence status computation
    // =============================================================================================
    private void resetGeofenceStatus() {
        geofenceEnteredTimestamp.put(LocationDataCollector.PLACE_LABEL_HOME, null);
        geofenceEnteredTimestamp.put(LocationDataCollector.PLACE_LABEL_WORK, null);
    }

    private String determineCurrentPlace() {
        return If.nullThen(HashUtils.argMax(geofenceEnteredTimestamp), "others");
    }
    //endregion

    //region Section: Notification elapsed time
    // =============================================================================================
    private double getLastNotificationElapsedTimeInMinute() {
        double timeElapsed = (double) TimeUnit.MILLISECONDS.toMinutes(
                System.currentTimeMillis() - lastNotificationTime);
        return Math.min(Math.max(timeElapsed, 0.0), (double) TimeUnit.DAYS.toMinutes(1));
    }
    //endregion
}
