package ucla.nesl.notificationpreference.storage.loggers;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
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

public class ScreenStatusLogger implements ILogger {

    private static final String TAG = ScreenStatusLogger.class.getSimpleName();

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


    private File file;

    private ScreenStatusLogger(@NonNull File _file) {
        file = _file;
    }

    @Override
    public File getFile() {
        return file;
    }

    public void log(@NonNull String status) {
        try {
            PrintWriter out = new PrintWriter(new FileOutputStream(file, true));
            out.println(String.format(
                    Locale.getDefault(),
                    "%d,%s",
                    System.currentTimeMillis(),
                    status
            ));
            out.close();
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }
}
