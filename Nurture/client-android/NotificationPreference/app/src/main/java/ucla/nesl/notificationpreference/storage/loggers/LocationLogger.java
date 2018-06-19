package ucla.nesl.notificationpreference.storage.loggers;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.location.Geofence;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Locale;

/**
 * Created by timestring on 6/19/18.
 *
 * Log location geofences.
 *
 * Note that this class implements the singleton pattern. Please note that after the logger is
 * created, the class will not accept the request of getting the instance with explicit file
 * location.
 */

public class LocationLogger implements ILogger {

    private static final String TAG = LocationLogger.class.getSimpleName();

    private static final File DEFAULT_FILE = new File(
            Environment.getExternalStorageDirectory(), "location.event.txt");

    private static LocationLogger instance;

    public static LocationLogger getInstance() {
        if (instance == null) {
            instance = new LocationLogger(DEFAULT_FILE);
        }
        return instance;
    }

    public static LocationLogger getInstance(@NonNull String filePath) {
        if (instance != null) {
            throw new IllegalStateException("A logger has existed");
        }
        instance = new LocationLogger(new File(filePath));
        return instance;
    }


    private File file;

    private LocationLogger(@NonNull File _file) {
        file = _file;
    }

    @Override
    public File getFile() {
        return file;
    }

    public void log(int geofenceTransitionType, @NonNull String geofenceCompoundPlaceLabels) {
        try {
            PrintWriter out = new PrintWriter(new FileOutputStream(file, true));
            out.println(String.format(
                    Locale.getDefault(),
                    "%d,%s,%s",
                    System.currentTimeMillis(),
                    getGeofenceTransitionTypeName(geofenceTransitionType),
                    geofenceCompoundPlaceLabels
            ));
            out.close();
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    private String getGeofenceTransitionTypeName(int geofenceTransitionType) {
        switch (geofenceTransitionType) {
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                return "dwell";
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return "enter";
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return "exit";
            default:
                return "unknown";
        }
    }
}
