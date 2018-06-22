package ucla.nesl.notificationpreference.storage.loggers;

import android.os.Environment;
import android.support.annotation.NonNull;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.io.File;
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

public class MotionActivityLogger extends LocalLogger {

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


    private MotionActivityLogger(@NonNull File file) {
        super(file);
    }

    public void log(@NonNull ActivityRecognitionResult result) {
        appendLine(String.format(
                Locale.getDefault(),
                "%d,%d,%d,%d,%d,%d",
                System.currentTimeMillis(),
                result.getActivityConfidence(DetectedActivity.STILL),
                result.getActivityConfidence(DetectedActivity.WALKING),
                result.getActivityConfidence(DetectedActivity.RUNNING),
                result.getActivityConfidence(DetectedActivity.IN_VEHICLE),
                result.getActivityConfidence(DetectedActivity.ON_BICYCLE)
        ));
    }
}
