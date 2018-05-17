package ucla.nesl.notificationpreference.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by timestring on 2/2/18.
 *
 * A helper class which allows activities or services to easily access shared preferences globally.
 */

public class SharedPreferenceHelper {
    //TODO: make the scope of the following keys private
    public static final String KEY_CAN_COLLECT_USER_DATA = "can_collect_data";
    public static final String KEY_SENDING_NOTIFICATION_ON_LOCATION_CHANGED = "notification_location_changed";
    public static final String KEY_LOCATION_UPDATE_INTERVAL = "location_update_interval";
    public static final String KEY_LOCATION_MINIMUM_DISPLACEMENT = "location_minimum_displacement";
    public static final String KEY_SENDING_NOTIFICATION_ON_MOTION_CHANGED = "notification_motion_changed";
    public static final String KEY_ACTIVITY_DETECTION_INTERVAL = "activity_detection_interval";

    private SharedPreferences mSharedPreferences;


    public SharedPreferenceHelper(Context context) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean getCanCollectUserData() {
        return mSharedPreferences.getBoolean(KEY_CAN_COLLECT_USER_DATA, true);
    }

    public void setCanCollectUserData(boolean value) {
        mSharedPreferences.edit().putBoolean(KEY_CAN_COLLECT_USER_DATA, value).apply();
    }

    public boolean getSendingNotificationOnLocationChanged() {
        return mSharedPreferences.getBoolean(KEY_SENDING_NOTIFICATION_ON_LOCATION_CHANGED, false);
    }

    public void setSendingNotificationOnLocationChanged(boolean value) {
        mSharedPreferences.edit().putBoolean(KEY_SENDING_NOTIFICATION_ON_LOCATION_CHANGED, value).apply();
    }

    public long getLocationUpdateIntervalMsec() {
        return mSharedPreferences.getLong(KEY_LOCATION_UPDATE_INTERVAL, 60000L);
    }

    public void setLocationUpdateIntervalMsec(long value) {
        mSharedPreferences.edit().putLong(KEY_LOCATION_UPDATE_INTERVAL, value).apply();
    }

    public float getLocationMinimumDisplacementMeter() {
        return mSharedPreferences.getFloat(KEY_LOCATION_MINIMUM_DISPLACEMENT, 50.f);
    }

    public void setLocationMinimumDisplacementMeter(float value) {
        mSharedPreferences.edit().putFloat(KEY_LOCATION_MINIMUM_DISPLACEMENT, value).apply();
    }

    public boolean getSendingNotificationOnMotionChanged() {
        return mSharedPreferences.getBoolean(KEY_SENDING_NOTIFICATION_ON_MOTION_CHANGED, false);
    }

    public void getSendingNotificationOnMotionChanged(boolean value) {
        mSharedPreferences.edit().putBoolean(KEY_SENDING_NOTIFICATION_ON_MOTION_CHANGED, value).apply();
    }

    public long getActivityDetetionIntervalMsec() {
        return mSharedPreferences.getLong(KEY_ACTIVITY_DETECTION_INTERVAL, 60000L);
    }

    public void setActivityDetectionIntervalMsec(long value) {
        mSharedPreferences.edit().putLong(KEY_ACTIVITY_DETECTION_INTERVAL, value).apply();
    }

}
