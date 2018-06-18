package ucla.nesl.notificationpreference.storage.loggers;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Locale;

/**
 * Created by timestring on 6/14/18.
 *
 * Log ringer mode.
 *
 * Note that this class implements the singleton pattern. Please note that after the logger is
 * created, the class will not accept the request of getting the instance with explicit file
 * location.
 */

public class RingerModeLogger implements ILogger {

    private static final String TAG = RingerModeLogger.class.getSimpleName();

    private static final File DEFAULT_FILE = new File(
            Environment.getExternalStorageDirectory(), "ringer_mode.event.txt");

    private static RingerModeLogger instance;

    public static RingerModeLogger getInstance() {
        if (instance == null) {
            instance = new RingerModeLogger(DEFAULT_FILE);
        }
        return instance;
    }

    public static RingerModeLogger getInstance(@NonNull String filePath) {
        if (instance != null) {
            throw new IllegalStateException("A logger has existed");
        }
        instance = new RingerModeLogger(new File(filePath));
        return instance;
    }


    private File file;

    private RingerModeLogger(@NonNull File _file) {
        file = _file;
    }

    @Override
    public File getFile() {
        return file;
    }

    public void log(@NonNull String mode) {
        try {
            PrintWriter out = new PrintWriter(new FileOutputStream(file, true));
            out.println(String.format(
                    Locale.getDefault(),
                    "%d,%s",
                    System.currentTimeMillis(),
                    mode
            ));
            out.close();
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }
}
