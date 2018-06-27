package ucla.nesl.notificationpreference.boot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ucla.nesl.notificationpreference.utils.Utils;

public class BootEventReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Utils.startTaskSchedulingService(context);
    }
}
