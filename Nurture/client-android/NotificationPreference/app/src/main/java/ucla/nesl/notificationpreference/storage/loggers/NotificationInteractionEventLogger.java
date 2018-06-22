package ucla.nesl.notificationpreference.storage.loggers;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.util.Locale;

/**
 * Created by timestring on 5/25/18.
 *
 * `NotificationInteractionEventLogger` aims to collect all the interaction events no matter how
 * subtle they are. These events include showing, clicking, or responding a notification.
 *
 * Note that this class implements the singleton pattern. Please note that after the logger is
 * created, the class will not accept the request of getting the instance with explicit file
 * location.
 */

public class NotificationInteractionEventLogger extends LocalLogger {

    private static final String TAG = "NotificationEventLogger";

    private static final File DEFAULT_FILE = new File(
            Environment.getExternalStorageDirectory(), "notification_interaction.event.txt");

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


    private NotificationInteractionEventLogger(@NonNull File file) {
        super(file);
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

    public void logDismissNotification(int notificationID) {
        log("DISMISS", notificationID, "");
    }

    private void log(@NonNull String event, int notificationID, @NonNull String meta) {
        // to ease the effort of data analysis, we make sure `meta` does not span across
        // multiple lines
        meta = meta.replace("\n", " ").replace("\r", " ");

        String message = String.format(Locale.getDefault(), "%d,%s,%d,%s",
                System.currentTimeMillis(), event, notificationID, meta);
        Log.i(TAG, "going to log message: " + message);
        appendLine(message);
    }
}
