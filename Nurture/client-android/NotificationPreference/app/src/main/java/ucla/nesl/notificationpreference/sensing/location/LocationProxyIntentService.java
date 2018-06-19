package ucla.nesl.notificationpreference.sensing.location;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;

import ucla.nesl.notificationpreference.utils.Utils;

/**
 * Created by timestring on 6/19/18.
 */
public class LocationProxyIntentService extends IntentService {

    private static final String TAG = LocationProxyIntentService.class.getSimpleName();

    public LocationProxyIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent passedIntent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(passedIntent);

        // Handling errors
        if (geofencingEvent == null) {
            Log.e(TAG, "empty geofencing event");
            return;
        }
        Log.i("LocationProxyItService", "get event:" + geofencingEvent.toString());
        if (geofencingEvent.hasError()) {
            String errorMsg = getErrorString(geofencingEvent.getErrorCode());
            Log.e(TAG, errorMsg);
            return;
        }

        Intent forwardedIntent = new Intent(
                LocationDataCollector.INTENT_FORWARD_LOCATION_RESULT);
        forwardedIntent.putExtra(LocationDataCollector.INTENT_EXTRA_NAME_GEO_TRANSITION,
                geofencingEvent.getGeofenceTransition());
        forwardedIntent.putExtra(LocationDataCollector.INTENT_EXTRA_NAME_GEO_PLACES,
                extractGeofencingLabels(geofencingEvent));
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(forwardedIntent);
    }

    private String extractGeofencingLabels(@NonNull GeofencingEvent geofencingEvent) {
        ArrayList<String> geofenceLabelList = new ArrayList<>();
        for (Geofence geofence : geofencingEvent.getTriggeringGeofences()) {
            geofenceLabelList.add(geofence.getRequestId());
        }
        return Utils.stringJoin(LocationDataCollector.GEO_PLACES_DELIMITER, geofenceLabelList);
    }

    private static String getErrorString(int errorCode) {
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "GeoFence not available";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "Too many GeoFences";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "Too many pending intents";
            default:
                return "Unknown error.";
        }
    }
}
