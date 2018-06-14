package ucla.nesl.notificationpreference.storage.loggers;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Locale;

/**
 * Created by timestring on 6/4/18.
 *
 * `MotionActivityLogger` stores the `ActivityRecognitionResult` from Google API. Specifically,
 * it stores the confidence scores of the activity types in interest.
 *
 * Note that this class implements the singleton pattern. Please note that after the logger is
 * created, the class will not accept the request of getting the instance with explicit file
 * location.
 */

public class MotionActivityLogger implements ILogger {

    private static final String TAG = "MotionActivityLogger";

    private static final File DEFAULT_FILE = new File(
            Environment.getExternalStorageDirectory(), "motion_activity.event.txt");

    private static MotionActivityLogger instance;

    public static MotionActivityLogger getInstance() {
        if (instance == null) {
            instance = new MotionActivityLogger(DEFAULT_FILE);
        }
        return instance;
    }

    public static MotionActivityLogger getInstance(@NonNull String filePath) {
        if (instance != null) {
            throw new IllegalStateException("A logger has existed");
        }
        instance = new MotionActivityLogger(new File(filePath));
        return instance;
    }


    private File file;

    private MotionActivityLogger(@NonNull File _file) {
        file = _file;
    }

    @Override
    public File getFile() {
        return file;
    }

    public void log(@NonNull ActivityRecognitionResult result) {
        try {
            PrintWriter out = new PrintWriter(new FileOutputStream(file, true));
            out.println(String.format(
                    Locale.getDefault(),
                    "%d,%d,%d,%d,%d,%d",
                    System.currentTimeMillis(),
                    result.getActivityConfidence(DetectedActivity.STILL),
                    result.getActivityConfidence(DetectedActivity.WALKING),
                    result.getActivityConfidence(DetectedActivity.RUNNING),
                    result.getActivityConfidence(DetectedActivity.IN_VEHICLE),
                    result.getActivityConfidence(DetectedActivity.ON_BICYCLE)
            ));
            out.close();
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }
}
