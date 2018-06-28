package ucla.nesl.notificationpreference.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;

import java.util.ArrayList;
import java.util.Date;

import ucla.nesl.notificationpreference.service.TaskSchedulingService;
import ucla.nesl.notificationpreference.storage.SharedPreferenceHelper;
import ucla.nesl.notificationpreference.storage.loggers.LocalLogger;
import ucla.nesl.notificationpreference.storage.loggers.LocationLogger;
import ucla.nesl.notificationpreference.storage.loggers.MotionActivityLogger;
import ucla.nesl.notificationpreference.storage.loggers.NotificationInteractionEventLogger;
import ucla.nesl.notificationpreference.storage.loggers.RingerModeLogger;
import ucla.nesl.notificationpreference.storage.loggers.ScreenStatusLogger;

/**
 * Created by timestring on 05/15/18.
 *
 * Provide methods cover
 *   - Common objects to strings
 *   - String related utility
 */

public class Utils {

    //region Section: User code
    // =============================================================================================
    public static boolean tryUpdateUserCode(String code, SharedPreferenceHelper keyValueStore) {
        if (code != null && code.matches("[0-9]+")) {
            keyValueStore.setUserCode(code);
            return true;
        }
        return false;
    }
    //endregion

    //region Section: Loggers
    // =============================================================================================
    public static LocalLogger[] getAllLoggers() {
        return new LocalLogger[] {
                LocationLogger.getInstance(),
                MotionActivityLogger.getInstance(),
                NotificationInteractionEventLogger.getInstance(),
                RingerModeLogger.getInstance(),
                ScreenStatusLogger.getInstance()
        };
    }

    public static boolean hasAnyStaleLogFile() {
        for (LocalLogger logger : getAllLoggers()) {
            if (logger.fileExists()) {
                return true;
            }
        }
        return false;
    }

    public static void backupAllStaleLogFiles() {
        for (LocalLogger logger : getAllLoggers()) {
            logger.moveToBackup();
        }
    }
    //endregion

    //region Section: String utilities
    // =============================================================================================
    public static void startTaskSchedulingService(Context context) {
        Intent serviceIntent = new Intent(context, TaskSchedulingService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }
    //endregion

    //region Section: String utilities
    // =============================================================================================
    public static String stringJoin(CharSequence delimiter, CharSequence... args) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (CharSequence cs : args) {
            if (first) {
                first = false;
            } else {
                builder.append(delimiter);
            }
            builder.append(cs);
        }
        return builder.toString();
    }

    public static String stringJoin(CharSequence delimiter, ArrayList<? extends CharSequence> args) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (CharSequence cs : args) {
            if (first) {
                first = false;
            } else {
                builder.append(delimiter);
            }
            builder.append(cs);
        }
        return builder.toString();
    }
    //endregion

    //region Section: Date formatting utilities
    // =============================================================================================
    public static String formatDate(String format, long timestamp) {
        return DateFormat.format(format, new Date(timestamp)).toString();
    }
    //endregion

    //region Section: Syntax sugar if a value is in a pool
    // =============================================================================================
    public static boolean in(int target, int... values) {
        for (int v : values) {
            if (target == v) {
                return true;
            }
        }
        return false;
    }

    public static boolean in(@NonNull String target, String... values) {
        for (String v : values) {
            if (target.equals(v)) {
                return true;
            }
        }
        return false;
    }
    //endregion

    // enforce using static methods
    private Utils() {}
}
