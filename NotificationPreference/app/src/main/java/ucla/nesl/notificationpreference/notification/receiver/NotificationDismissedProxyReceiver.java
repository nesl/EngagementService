package ucla.nesl.notificationpreference.notification.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import ucla.nesl.notificationpreference.notification.NotificationHelper;

/**
 * Created by timestring on 6/4/18.
 *
 * `NotificationButtonActionProxyReceiver` handles tedious flow forwarding between
 * `NotificationService` and `NotificationHelper`. It takes in charge of notification dismissed
 * events.
 */

public class NotificationDismissedProxyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent passedIntent) {
        Intent forwardedIntent = new Intent(
                NotificationHelper.INTENT_FORWARD_NOTIFICATION_RESPONSE_ACTION);
        forwardedIntent.putExtras(passedIntent);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
        localBroadcastManager.sendBroadcast(forwardedIntent);
    }
}
