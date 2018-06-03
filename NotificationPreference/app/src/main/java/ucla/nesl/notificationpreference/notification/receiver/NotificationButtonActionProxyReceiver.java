package ucla.nesl.notificationpreference.notification.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.RemoteInput;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import ucla.nesl.notificationpreference.notification.NotificationHelper;
import ucla.nesl.notificationpreference.task.tasks.MoodTask;

/**
 * Created by timestring on 5/26/18.
 *
 * `NotificationButtonActionProxyReceiver` handles tedious flow forwarding between
 * `NotificationService` and `NotificationHelper`. It takes in charge of the response from a button.
 * Please see `NotificationHelper` for more information.
 */

public class NotificationButtonActionProxyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent passedIntent) {
        Log.i("NotificationProxy", "got intent. -> " + passedIntent.getStringExtra("response"));

        Bundle remoteInput = RemoteInput.getResultsFromIntent(passedIntent);
        if (remoteInput != null){
            CharSequence seq = remoteInput.getCharSequence(MoodTask.KEY_TEXT_REPLY);
            if (seq == null) {
                Log.i("NotificationProxy", "sequence is null");
            } else {
                Log.i("NotificationProxy", "Yes! we get " + seq.toString());
            }
        } else {
            Log.i("NotificationProxy", "Sounds like an empty bundle");
        }

        Bundle remoteInput2 = RemoteInput.getResultsFromIntent(passedIntent);
        if (remoteInput2 != null){
            CharSequence seq = remoteInput2.getCharSequence(MoodTask.KEY_TEXT_REPLY);
            if (seq == null) {
                Log.i("NotificationProxy", "sequence is null");
            } else {
                Log.i("NotificationProxy", "Yes! we get " + seq.toString());
            }
        } else {
            Log.i("NotificationProxy", "Sounds like an empty bundle");
        }

        Log.i("NotificationProxy", "intent:" + passedIntent.toString());
        Log.i("NotificationProxy", "extras:" + passedIntent.getExtras().toString());

        Intent forwardedIntent = new Intent(
                NotificationHelper.INTENT_FORWARD_NOTIFICATION_RESPONSE_ACTION);
        forwardedIntent.putExtras(passedIntent);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
        localBroadcastManager.sendBroadcast(forwardedIntent);
    }
}
