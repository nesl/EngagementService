package ucla.nesl.notificationpreference.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

/**
 * Created by timestring on 6/5/18.
 *
 * Serve as a light weight key-value store
 */

public class SharedPreferenceHelper {

    public static final int APP_STATUS_NOT_INITIALIZED = 0;
    public static final int APP_STATUS_ACTIVE = 1;
    public static final int APP_STATUS_INACTIVE = 2;

    private static final String KEY_APP_STATUS = "key.app.status";
    private static final String KEY_USER_CODE = "key.user.code";
    private static final String KEY_USER_HOME_PLACE_DESCRIPTION = "key.user.home.place.description";
    private static final String KEY_USER_HOME_LATITUDE = "key.user.home.latitude";
    private static final String KEY_USER_HOME_LONGITUDE = "key.user.home.longitude";
    private static final String KEY_USER_WORK_PLACE_DESCRIPTION = "key.user.work.place.description";
    private static final String KEY_USER_WORK_LATITUDE = "key.user.work.latitude";
    private static final String KEY_USER_WORK_LONGITUDE = "key.user.work.longitude";


    private SharedPreferences mSharedPreferences;


    public SharedPreferenceHelper(Context context) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public int getAppStatus() {
        return mSharedPreferences.getInt(KEY_APP_STATUS, APP_STATUS_NOT_INITIALIZED);
    }

    public void setAppStatus(int value) {
        mSharedPreferences.edit().putInt(KEY_APP_STATUS, value).apply();
    }

    public String getUserCode() {
        return mSharedPreferences.getString(KEY_USER_CODE, null);
    }

    public void setUserCode(@NonNull String value) {
        mSharedPreferences.edit().putString(KEY_USER_CODE, value).apply();
    }

    public String getUserHomePlaceDescription() {
        return mSharedPreferences.getString(KEY_USER_HOME_PLACE_DESCRIPTION, null);
    }

    public void setUserHomePlaceDescription(String value) {
        mSharedPreferences.edit().putString(KEY_USER_HOME_PLACE_DESCRIPTION, value).apply();
    }

    public double getUserHomeLatitude() {
        return mSharedPreferences.getFloat(KEY_USER_HOME_LATITUDE, 0f);
    }

    public void setUserHomeLatitude(float value) {
        mSharedPreferences.edit().putFloat(KEY_USER_HOME_LATITUDE, value).apply();
    }

    public double getUserHomeLongitude() {
        return mSharedPreferences.getFloat(KEY_USER_HOME_LONGITUDE, 0f);
    }

    public void setUserHomeLongitude(float value) {
        mSharedPreferences.edit().putFloat(KEY_USER_HOME_LONGITUDE, value).apply();
    }

    public String getUserWorkPlaceDescription() {
        return mSharedPreferences.getString(KEY_USER_WORK_PLACE_DESCRIPTION, null);
    }

    public void setUserWorkPlaceDescription(String value) {
        mSharedPreferences.edit().putString(KEY_USER_WORK_PLACE_DESCRIPTION, value).apply();
    }

    public double getUserWorkLatitude() {
        return mSharedPreferences.getFloat(KEY_USER_WORK_LATITUDE, 0f);
    }

    public void setUserWorkLatitude(float value) {
        mSharedPreferences.edit().putFloat(KEY_USER_WORK_LATITUDE, value).apply();
    }

    public double getUserWorkLongitude() {
        return mSharedPreferences.getFloat(KEY_USER_WORK_LONGITUDE, 0f);
    }

    public void setUserWorkLongitude(float value) {
        mSharedPreferences.edit().putFloat(KEY_USER_WORK_LONGITUDE, value).apply();
    }
}