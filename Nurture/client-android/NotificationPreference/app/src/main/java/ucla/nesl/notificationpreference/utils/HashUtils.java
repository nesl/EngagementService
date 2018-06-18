package ucla.nesl.notificationpreference.utils;

import android.support.annotation.NonNull;
import android.util.SparseIntArray;

/**
 * Created by timestring on 6/14/18.
 *
 * Syntax sugar to do something similar to += operator in C++ map, like
 *
 *     map<int, int> m;
 *     m[3] += 5;
 */

public class HashUtils {

    public static void addAssign(@NonNull  SparseIntArray array, int key, int delta) {
        int newValue = array.get(key, 0) + delta;
        array.put(key, newValue);
    }

    public static int argMax(@NonNull SparseIntArray array) {
        if (array.size() == 0) {
            throw new IllegalStateException("No way to get arg-max if the hash is empty");
        }

        int maxVal = array.valueAt(0);
        int maxKey = array.keyAt(0);
        for (int i = 1; i < array.size(); i++) {
            int tv = array.valueAt(i);
            if (tv > maxVal) {
                maxVal = tv;
                maxKey = array.keyAt(i);
            }
        }
        return maxKey;
    }

    // disable instantiation
    private HashUtils() {}
}
