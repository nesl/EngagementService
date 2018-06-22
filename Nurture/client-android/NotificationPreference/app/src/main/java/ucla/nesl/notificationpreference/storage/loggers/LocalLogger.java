package ucla.nesl.notificationpreference.storage.loggers;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by timestring on 6/13/18.
 *
 * Interface of loggers. Need to return the log file path.
 */

public abstract class LocalLogger {

    private static final String TAG = LocalLogger.class.getSimpleName();


    private File file;

    protected LocalLogger(@NonNull File _file) {
        file = _file;
    }

    public final File getFile() {
        return file;
    }

    public final boolean fileExists() {
        return file.exists();
    }

    public final boolean moveToBackup() {
        return moveToBackup(System.currentTimeMillis());
    }

    public final boolean moveToBackup(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault());
        String newName = String.format("Nurture-bk-%s-%s", formatter.format(date), file.getName());
        File newFile = new File(file.getParent(), newName);
        return file.renameTo(newFile);
    }

    protected void appendLine(String line) {
        try {
            PrintWriter out = new PrintWriter(new FileOutputStream(file, true));
            out.println(line);
            out.close();
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }
}
