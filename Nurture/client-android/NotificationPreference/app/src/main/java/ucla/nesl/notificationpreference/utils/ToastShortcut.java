package ucla.nesl.notificationpreference.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by timestring on 2/15/18.
 *
 * Syntax sugar of Toast.
 */

public class ToastShortcut {
    private Context mContext;

    public ToastShortcut(Context context) {
        mContext = context;
    }

    public void showLong(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
    }

    public void showShort(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }
}
