package ucla.nesl.notificationpreference.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by timestring on 5/26/18.
 *
 * `NotificationProxyReceiver` handles tedious flow forwarding between `NotificationService` and
 * `NotificationHelper`. Please see `NotificationHelper` for more information.
 */

public class NotificationProxyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent passedIntent) {
        Log.i("NotificationProxy", "got intent. -> " + passedIntent.getStringExtra("response"));
        Intent forwardedIntent = new Intent(
                NotificationHelper.INTENT_FORWARD_NOTIFICATION_RESPONSE_ACTION);
        forwardedIntent.putExtras(passedIntent);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
        localBroadcastManager.sendBroadcast(forwardedIntent);
    }
}
