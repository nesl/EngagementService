package ucla.nesl.notificationpreference.utils;

import android.content.res.Resources;

/**
 * Created by timestring on 5/30/18.
 *
 * A helper class to convert DP (density-independent pixel) to PX (pixel). It is encouraged by
 * Google that to use DP when possible, but some old Android API only accepts PX, hence a shortcut
 * for conversion.
 */

public class DP {
    /**
     * The implementation is based on https://stackoverflow.com/a/38841669/4713342
     */
    public static int toPX(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    // enforce using static methods
    private DP() {}
}
