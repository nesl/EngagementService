package ucla.nesl.notificationpreference.storage;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Locale;

/**
 * Created by timestring on 5/25/18.
 */

public class NotificationInteractionEventLogger {

    private static final String TAG = "NotiEventLogger";

    private static final File DEFAULT_FILE = new File(
            Environment.getExternalStorageDirectory(), "notification_interaction.evevt.txt");

    private static NotificationInteractionEventLogger instance;

    public static NotificationInteractionEventLogger getInstance() {
        if (instance == null) {
            instance = new NotificationInteractionEventLogger(DEFAULT_FILE);
        }
        return instance;
    }

    public static NotificationInteractionEventLogger getInstance(@NonNull String filePath) {
        if (instance != null) {
            throw new IllegalStateException("A logger has existed");
        }
        instance = new NotificationInteractionEventLogger(new File(filePath));
        return instance;
    }


    private File file;

    public NotificationInteractionEventLogger() {
        file = DEFAULT_FILE;
    }

    public NotificationInteractionEventLogger(@NonNull File _file) {
        file = _file;
    }

    public void logRegisterNotification(int notificationID) {
        log("SHOW", notificationID, "");
    }

    public void logClickNotification(int notificationID) {
        log("CLICK", notificationID, "");
    }

    public void logRespondInNotification(int notificationID, String response) {
        log("RESPOND_IN_NOTIFICATION", notificationID, response);
    }

    public void logRespondInApp(int notificationID, String response) {
        log("RESPOND_IN_APP", notificationID, response);
    }

    private void log(@NonNull String event, int notificationID, @NonNull String meta) {
        try {
            // to ease the effort of data analysis, we make sure `meta` does not span across
            // multiple lines
            meta = meta.replace("\n", " ").replace("\r", " ");

            PrintWriter out = new PrintWriter(new FileOutputStream(file, true));
            String message = String.format(Locale.getDefault(), "%d,%s,%d,%s",
                    System.currentTimeMillis(), event, notificationID, meta);
            out.println(message);
            out.close();
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }
}
