package ucla.nesl.notificationpreference.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.SparseArray;

import ucla.nesl.notificationpreference.R;
import ucla.nesl.notificationpreference.activity.TaskActivity;
import ucla.nesl.notificationpreference.storage.NotificationInteractionEventLogger;
import ucla.nesl.notificationpreference.storage.NotificationResponseRecordDatabase;
import ucla.nesl.notificationpreference.task.MoodTask;
import ucla.nesl.notificationpreference.task.ShortQuestionTask;

/**
 * Created by timestring on 2/12/18.
 *
 * Notification helps populate the content of the notifications. Now we assume all the notifications
 * are static.
 *
 * TODO: need to come back and revise the class assumption here
 */

public class NotificationHelper {

    private static final String TAG = NotificationHelper.class.getSimpleName();

    static final String INTENT_FORWARD_NOTIFICATION_RESPONSE_ACTION = "intent.forward.notification.response.action";

    public static final int NOTIFICATION_ID_NOT_SET = -1;

    private static final String INTENT_EXTRA_NAME_NOTIFICATION_ID = "notificationID";
    private static final String INTENT_EXTRA_NAME_RESPONSE = "response";
    private static final int OFFSET = 100000;

    private static final String CHANNEL_ID = "channel_0";

    private static final String CHANNEL_GROUP_ID = "group_0";
    private static final String CHANNEL_GROUP_NAME = "group_0_name";


    private NotificationManager notificationManager;
    private Context mContext;

    private SparseArray<Notification> cache = new SparseArray<>();

    private NotificationResponseRecordDatabase responseDatabase;
    private NotificationInteractionEventLogger interactionLogger;


    //region Section: Initialization
    // =============================================================================================
    public NotificationHelper(Context context) {
        mContext = context;

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // create a notification channel as Android O requires
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //notificationManager.createNotificationChannelGroup(
            //        new NotificationChannelGroup(CHANNEL_GROUP_ID, CHANNEL_GROUP_NAME)
            //);

            // the importance of notification channel has to be the highest possible so that users
            // can see the heads-up style notifications (only if the priority of the notifications
            // are also configured to be the highest possible)
            CharSequence name = mContext.getString(R.string.app_name);
            NotificationChannel mChannel = new NotificationChannel(
                    CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);
            mChannel.enableLights(true);
            mChannel.enableVibration(true);
            mChannel.setLightColor(Color.GREEN);
            notificationManager.createNotificationChannel(mChannel);
            //mChannel.setGroup(CHANNEL_GROUP_ID);
        }

        // register local broadcast receiver for notification response callback
        IntentFilter intentFilter = new IntentFilter(INTENT_FORWARD_NOTIFICATION_RESPONSE_ACTION);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
        localBroadcastManager.registerReceiver(responseReceiver, intentFilter);

        // acquire notification event loggers
        responseDatabase = NotificationResponseRecordDatabase.getAppDatabase(context);
        interactionLogger = NotificationInteractionEventLogger.getInstance();
    }
    //endregion

    //region Section: Main operations (send and cancel notifications)
    // =============================================================================================
    public int createAndSendTaskNotification() {

        // register a notification record in database
        int notificationID = responseDatabase.createResponseRecord(0, 0);
        interactionLogger.logRegisterNotification(notificationID);
        Log.i(TAG, "just created a notification with ID " + notificationID);

        // fill task-specific content in the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID);

        // TODO: may instantiate a different kind of task
        ShortQuestionTask task = new MoodTask(notificationID);

        task.fillNotificationLayout(this, builder);

        // configure non-UI part of the notification
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                //.setLargeIcon(R.mipmap.)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(makeActivityPendingIndent(notificationID))
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentTitle("Please answer short question")
                .setContentText("Please answer the following survey question")
                //.setTicker("Where is the ticker?")
                //.setVisibility(Notification.VISIBILITY_PUBLIC)
                //.setVibrate(new long[]{200,200,200,200,200})
                .setSound(defaultSoundUri)
                .setAutoCancel(false);

        // assign the corresponding channel and the notification priority (has to be the highest
        // to enable the heads-up style notification)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID);
            builder.setPriority(NotificationCompat.PRIORITY_MAX);
        } else {
            builder.setPriority(Notification.PRIORITY_MAX);
        }

        // deliver the notification to users
        Notification notification = builder.build();
        notificationManager.notify(notificationID, notification);

        return notificationID;
    }

    public void cancelNotification(int notificationID) {
        notificationManager.cancel(notificationID);
    }
    //endregion

    //region Section: Notification response receiver
    // =============================================================================================
    private final BroadcastReceiver responseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int notificationID = interpretIntentGetNotificationID(intent);
            String response = interpretIntentGetResponse(intent);
            Log.i("NotificationHelper", "Receive response:" + response);

            responseDatabase.fillAnswer(notificationID, response);
            interactionLogger.logRespondInNotification(notificationID, response);

            //TODO: remove the notification
        }
    };
    //endregion

    //region Section: PendingIntent factory and interpreter
    // =============================================================================================
    public PendingIntent makeActivityPendingIndent(int notificationID) {
        Intent intent = new Intent(mContext, TaskActivity.class);
        intent.putExtra(INTENT_EXTRA_NAME_NOTIFICATION_ID, notificationID);
        int requestCode = notificationID * OFFSET;
        return PendingIntent.getActivity(mContext, requestCode, intent, 0);
    }

    public PendingIntent makeActionPendingIndent(
            int notificationID, int buttonID, String response) {

        if (buttonID <= 0 || buttonID >= OFFSET) {
            throw new IllegalArgumentException("button ID has to be 1 ~ " + (OFFSET - 1));
        }

        Intent intent = new Intent(mContext, NotificationProxyReceiver.class);
        int requestCode = notificationID * OFFSET + buttonID;
        intent.putExtra(INTENT_EXTRA_NAME_NOTIFICATION_ID, notificationID);
        intent.putExtra(INTENT_EXTRA_NAME_RESPONSE, response);
        return PendingIntent.getBroadcast(mContext, requestCode, intent, 0);
    }

    public static int interpretIntentGetNotificationID(Intent intent) {
        return intent.getIntExtra(INTENT_EXTRA_NAME_NOTIFICATION_ID, NOTIFICATION_ID_NOT_SET);
    }

    @NonNull
    private String interpretIntentGetResponse(Intent intent) {
        String response = intent.getStringExtra("response");
        if (response == null)
            response = "**(undefined)**_";
        return response;
    }
    //endregion
}
