package ucla.nesl.notificationpreference.utils;

/**
 * Created by timestring on 6/5/18.
 *
 * Syntax sugar to check if a value is in a pool of candidates.
 */

public class In {

    public static boolean ints(int target, int... values) {
        for (int v : values) {
            if (target == v) {
                return true;
            }
        }
        return false;
    }

    // enforce using static methods
    private In() {}
}
