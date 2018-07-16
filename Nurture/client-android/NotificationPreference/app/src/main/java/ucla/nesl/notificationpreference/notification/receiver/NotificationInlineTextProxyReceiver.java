package ucla.nesl.notificationpreference.notification.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.RemoteInput;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import ucla.nesl.notificationpreference.notification.NotificationHelper;
import ucla.nesl.notificationpreference.task.tasks.template.FreeTextTask;

/**
 * `NotificationInlineTextProxyReceiver` handles tedious flow forwarding between
 * `NotificationService` and `NotificationHelper`. It takes in charge of the response from the
 * inline text-field in a notification. Please see `NotificationHelper` for more information.
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

        CharSequence sequence = remoteInput.getCharSequence(FreeTextTask.KEY_TEXT_REPLY);
        if (sequence == null) {
            Log.e(TAG, "char-sequence is null");
            return;
        }

        int notificationID = NotificationHelper.interpretIntentGetNotificationID(passedIntent);
        String response = sequence.toString();

        Intent forwardedIntent = new Intent(
                NotificationHelper.INTENT_FORWARD_NOTIFICATION_RESPONSE_ACTION);
        NotificationHelper.overloadIDAndResponseOnIntent(
                forwardedIntent, notificationID, response, 0);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
        localBroadcastManager.sendBroadcast(forwardedIntent);
    }
}
