package ucla.nesl.notificationpreference.utils;

import android.content.res.Resources;

/**
 * Created by timestring on 5/30/18.
 */

public class DP {
    public static int toPX(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }
}
