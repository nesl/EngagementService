package ucla.nesl.notificationpreference.utils;

import android.support.annotation.NonNull;
import android.text.format.DateFormat;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by timestring on 05/15/18.
 *
 * Provide methods cover
 *   - Common objects to strings
 *   - String related utility
 */

public class Utils {

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
