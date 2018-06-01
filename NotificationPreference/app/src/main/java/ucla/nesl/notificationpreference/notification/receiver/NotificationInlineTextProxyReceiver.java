package ucla.nesl.notificationpreference.notification.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.RemoteInput;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import ucla.nesl.notificationpreference.notification.NotificationHelper;
import ucla.nesl.notificationpreference.task.MoodTask;

/**
 * Created by timestring on 5/31/18.
 */

public class NotificationInlineTextProxyReceiver extends BroadcastReceiver {

    private static final String TAG = "NotificationLineProxy";

    @Override
    public void onReceive(Context context, Intent passedIntent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(passedIntent);
        if (remoteInput == null) {
            Log.e(TAG, "Sounds like an empty bundle");
            return;
        }

        CharSequence sequence = remoteInput.getCharSequence(MoodTask.KEY_TEXT_REPLY);
        if (sequence == null) {
            Log.e(TAG, "char-sequence is null");
            return;
        }

        int notificationID = NotificationHelper.interpretIntentGetNotificationID(passedIntent);
        String response = sequence.toString();

        Intent forwardedIntent = new Intent(
                NotificationHelper.INTENT_FORWARD_NOTIFICATION_RESPONSE_ACTION);
        NotificationHelper.overloadIDAndResponseOnIntent(forwardedIntent, notificationID, response);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
        localBroadcastManager.sendBroadcast(forwardedIntent);
    }
}
