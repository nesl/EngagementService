package ucla.nesl.notificationpreference.sensing.location;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import ucla.nesl.notificationpreference.storage.SharedPreferenceHelper;
import ucla.nesl.notificationpreference.storage.loggers.LocationLogger;
import ucla.nesl.notificationpreference.utils.ToastShortcut;
import ucla.nesl.notificationpreference.utils.Utils;

/**
 * Created by timestring on 6/19/18.
 *
 * To get location geofences (i.e., home and workplace) via Google API.
 *
 * Please note that currently, we assume that no two `MotionActivityDataCollector` objects will
 * co-exist at any given time.
 */

public class LocationDataCollector {

    private static final String TAG = LocationDataCollector.class.getSimpleName();

    //private static final float GEOFENCE_RADIUS_IN_METERS = 200f;
    private static final float GEOFENCE_RADIUS_IN_METERS = 100f;
    //private static final float GEOFENCE_RADIUS_IN_METERS = 50f;

    public static final String INTENT_FORWARD_LOCATION_RESULT = "intent.forward.location.result";
    public static final String INTENT_EXTRA_NAME_GEO_TRANSITION = "geoTransition";
    public static final String INTENT_EXTRA_NAME_GEO_PLACES = "geoPlaces";
    public static final String GEO_PLACES_DELIMITER = "-";

    public static final String PLACE_LABEL_HOME = "home";
    public static final String PLACE_LABEL_WORK = "work";

    private static final int PENDING_INTENT_REQUEST_CODE_HOME = 0;
    private static final int PENDING_INTENT_REQUEST_CODE_WORK = 1;

    private Context context;
    private Callback callback;

    private ToastShortcut debugToast;

    private GeofencingClient geofencingClient;
    private SharedPreferenceHelper keyValueStore;

    private LocationLogger logger;

    // for geofence request intermediate data
    private PendingIntent pendingIntentHome;
    private PendingIntent pendingIntentWork;


    public LocationDataCollector(Context _context, Callback _callback) {
        context = _context;
        callback = _callback;

        debugToast = new ToastShortcut(context);

        geofencingClient = LocationServices.getGeofencingClient(context);
        keyValueStore = new SharedPreferenceHelper(context);

        logger = LocationLogger.getInstance();

        LocalBroadcastManager.getInstance(context).registerReceiver(locationResponseReceiver,
                new IntentFilter(INTENT_FORWARD_LOCATION_RESULT));

        if (!checkPermission()) {
            throw new IllegalStateException("No location permission");
        }
    }

    public void start() {
        // we treat place code as request code of `PendingIndent` and they are unique.
        pendingIntentHome = registerOneGeofence(
                PLACE_LABEL_HOME,
                keyValueStore.getUserHomeLatitude(),
                keyValueStore.getUserHomeLongitude(),
                PENDING_INTENT_REQUEST_CODE_HOME
        );
        pendingIntentWork = registerOneGeofence(
                PLACE_LABEL_WORK,
                keyValueStore.getUserWorkLatitude(),
                keyValueStore.getUserWorkLongitude(),
                PENDING_INTENT_REQUEST_CODE_WORK
        );
    }

    public void stop() {
        if (pendingIntentHome != null) {
            geofencingClient.removeGeofences(pendingIntentHome);
            pendingIntentHome = null;
        }
        if (pendingIntentWork != null) {
            geofencingClient.removeGeofences(pendingIntentWork);
            pendingIntentWork = null;
        }
    }

    private PendingIntent registerOneGeofence(
            @NonNull String placeLabel,
            double latitude,
            double longitude,
            int pendingIntentRequestCode
    ) {
        Geofence geofence = new Geofence.Builder()
                .setRequestId(placeLabel)
                .setCircularRegion(latitude, longitude, GEOFENCE_RADIUS_IN_METERS)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
        GeofencingRequest request = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER |
                        GeofencingRequest.INITIAL_TRIGGER_EXIT)
                .addGeofence(geofence)
                .build();

        Intent intent = new Intent(context, LocationProxyIntentService.class);
        PendingIntent pendingIntent = PendingIntent.getService(
                context, pendingIntentRequestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (checkPermission()) {
            Log.i("LocationDataCollector", "added geofences");
            geofencingClient.addGeofences(request, pendingIntent);
        }

        return pendingIntent;
    }

    private boolean checkPermission() {
        return (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED );
    }

    private BroadcastReceiver locationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int geoTransition = intent.getIntExtra(INTENT_EXTRA_NAME_GEO_TRANSITION, -1);
            String geoCompoundPlaces = intent.getStringExtra(INTENT_EXTRA_NAME_GEO_PLACES);

            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(500);
            debugToast.showLong("Get Location: " + geoTransition + "," + geoCompoundPlaces);

            Log.i(TAG, "Get Location: " + geoTransition + "," + geoCompoundPlaces);

            if (geoTransition == -1 || geoCompoundPlaces == null) {
                Log.e(TAG, "Invalid location response");
                return;
            }

            logger.log(geoTransition, geoCompoundPlaces);

            for (String geofenceLabel : geoCompoundPlaces.split(GEO_PLACES_DELIMITER)) {
                if (Utils.in(geofenceLabel, PLACE_LABEL_HOME, PLACE_LABEL_WORK)) {
                    callback.onGeofenceResult(geoTransition, geofenceLabel);
                }
            }
        }
    };

    public interface Callback {
        void onGeofenceResult(int geofenceTransitionType, String place);
    }
}
