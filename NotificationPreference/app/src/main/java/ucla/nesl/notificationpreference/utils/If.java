package ucla.nesl.notificationpreference.utils;

import android.support.annotation.NonNull;

/**
 * Created by timestring on 6/5/18.
 *
 * Syntax sugar for common branch conditions.
 */

public class If {

    @NonNull
    public static String nullThen(String original, @NonNull String replacedValue) {
        if (original != null)
            return original;
        return replacedValue;
    }

    private If() {}
}
