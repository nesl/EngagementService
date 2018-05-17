package ucla.nesl.notificationpreference.utils;

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
    //endregion
}
