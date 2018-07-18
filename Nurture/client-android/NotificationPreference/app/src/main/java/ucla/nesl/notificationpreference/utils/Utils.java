package ucla.nesl.notificationpreference.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;
import android.util.Log;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

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

    private static final String TAG = Utils.class.getSimpleName();

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

    public static String stringJoin(CharSequence delimiter, List<? extends CharSequence> args) {
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

    public static String stringJoinInts(CharSequence delimiter, List<Integer> args) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (Integer i : args) {
            if (i == null) {
                continue;
            }

            if (first) {
                first = false;
            } else {
                builder.append(delimiter);
            }
            builder.append(i);
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

    //region Section: Retrieving image asset
    // =============================================================================================
    public static Bitmap getImageFromAsset(Context context, String path) {
        try {
            InputStream stream = context.getAssets().open(path);
            Bitmap bitmap = BitmapFactory.decodeStream(stream);
            stream.close();
            return bitmap;
        } catch (Exception e) {
            Log.e(TAG, "Got an exception", e);
        }

        return null;
    }

    public static Bitmap addMarginForNotification(Bitmap srcBitmap) {
        int height = srcBitmap.getHeight();
        int newWidth = height * 3;
        Bitmap dstBitmap = Bitmap.createBitmap(newWidth, height, srcBitmap.getConfig());
        Canvas canvas = new Canvas(dstBitmap);
        canvas.drawARGB(0, 255, 255, 255);
        int xoffset = (newWidth - srcBitmap.getWidth()) / 2;
        canvas.drawBitmap(srcBitmap, xoffset, 0, null);
        return dstBitmap;
    }
    //endregion

    // enforce using static methods
    private Utils() {}
}
