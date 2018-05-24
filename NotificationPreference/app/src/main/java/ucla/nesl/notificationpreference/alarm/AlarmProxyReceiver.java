package ucla.nesl.notificationpreference.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by timestring on 5/24/18.
 *
 * `AlarmProxyReceiver` handles tedious flow forwarding between `AlarmManager` and
 * `AlarmEventManager`. Please see `AlarmEventManager for more information.
 */

public class AlarmProxyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent passedIntent) {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
        Intent forwardedIntent = new Intent(AlarmEventManager.INTENT_FORWARD_ALARM_EVENT_ACTION);
        forwardedIntent.putExtras(passedIntent);
        localBroadcastManager.sendBroadcast(forwardedIntent);
    }
}
