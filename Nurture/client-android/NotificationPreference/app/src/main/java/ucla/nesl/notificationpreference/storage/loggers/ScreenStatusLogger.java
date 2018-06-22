package ucla.nesl.notificationpreference.storage.loggers;

import android.os.Environment;
import android.support.annotation.NonNull;

import java.io.File;
import java.util.Locale;

/**
 * Created by timestring on 6/18/18.
 *
 * Log screen on/off status.
 *
 * Note that this class implements the singleton pattern. Please note that after the logger is
 * created, the class will not accept the request of getting the instance with explicit file
 * location.
 */

public class ScreenStatusLogger extends LocalLogger {

    private static final File DEFAULT_FILE = new File(
            Environment.getExternalStorageDirectory(), "screen_status.event.txt");

    private static ScreenStatusLogger instance;

    public static ScreenStatusLogger getInstance() {
        if (instance == null) {
            instance = new ScreenStatusLogger(DEFAULT_FILE);
        }
        return instance;
    }

    public static ScreenStatusLogger getInstance(@NonNull String filePath) {
        if (instance != null) {
            throw new IllegalStateException("A logger has existed");
        }
        instance = new ScreenStatusLogger(new File(filePath));
        return instance;
    }


    private ScreenStatusLogger(@NonNull File file) {
        super(file);
    }

    public void log(@NonNull String status) {
        appendLine(String.format(
                Locale.getDefault(),
                "%d,%s",
                System.currentTimeMillis(),
                status
        ));
    }
}
