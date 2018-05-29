package ucla.nesl.notificationpreference.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
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

    private static final String INTENT_EXTRA_NAME_NOTIFICATION_ID = "notificationID";
    private static final String INTENT_EXTRA_NAME_RESPONSE = "response";
    private static final int OFFSET = 100000;

    static final String INTENT_FORWARD_NOTIFICATION_RESPONSE_ACTION = "intent.forward.notification.response.action";

    public enum Type {
        FOREGROUND_SERVICE(1),
        LOCATION_CHANGED(12346),
        ACTIVITY_CHANGED(12347);

        private final int notificationID;

        Type(int id) {
            notificationID = id;
        }

        public int getID() {
            return notificationID;
        }
    }


    private static final String CHANNEL_ID = "channel_0";

    private static final String CHANNEL_GROUP_ID = "group_0";
    private static final String CHANNEL_GROUP_NAME = "group_0_name";


    private NotificationManager notificationManager;
    private Context mContext;

    private SparseArray<Notification> cache = new SparseArray<>();

    private NotificationResponseRecordDatabase responseDatabase;
    private NotificationInteractionEventLogger interactionLogger;


    public NotificationHelper(Context context) {
        mContext = context;

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // create a notification channel as Android O requires
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //notificationManager.createNotificationChannelGroup(
            //        new NotificationChannelGroup(CHANNEL_GROUP_ID, CHANNEL_GROUP_NAME)
            //);

            CharSequence name = mContext.getString(R.string.app_name);
            NotificationChannel mChannel = new NotificationChannel(
                    CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);
            mChannel.enableLights(true);
            mChannel.enableVibration(true);
            mChannel.setLightColor(Color.GREEN);
            //mChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
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

    public Notification getNotification(Type type) {
        // If the notification has created before, then return it
        Notification notification = cache.get(type.getID());
        if (notification != null) {
            return notification;
        }

        // If not, then create it based on the type
        /*
        PendingIntent activityPendingIntent = PendingIntent.getActivity(mContext, 0,
                new Intent(mContext, TaskActivity.class), 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(activityPendingIntent);
                //.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);

        switch (type) {
            case FOREGROUND_SERVICE:
                builder.setContentTitle("Location and Google Activity Service")
                        .setContentText("Monitoring location and user activity")
                        .setTicker("Monitoring location and user activity")
                        .setOngoing(true);
                Log.i("notification helper", "try to make foreground service");
                break;
            case LOCATION_CHANGED:
                builder.setAutoCancel(true)
                        .setContentTitle("TimeString test Notifications")
                        .setContentText("Would like to see a popup notification")
                        .setTicker("Please update your activity information")
                        //.setVisibility(Notification.VISIBILITY_PUBLIC)
                        .addAction(android.R.drawable.ic_menu_view, "View details", activityPendingIntent)
                        .setDefaults(Notification.DEFAULT_VIBRATE)
                        .setVibrate(new long[]{500,500,500,500,500})
                        .setAutoCancel(true);
                break;
            case ACTIVITY_CHANGED:
                builder.setAutoCancel(true)
                        .setContentTitle("Motion status Changed")
                        .setContentText("Please update your activity information")
                        .setTicker("Please update your activity information")
                        .setPriority(Notification.PRIORITY_DEFAULT);
                break;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID);
            builder.setPriority(NotificationCompat.PRIORITY_HIGH);
            Log.i("HEY", "set importance high");
        } else {
            builder.setPriority(Notification.PRIORITY_MAX);
        }
        */

        int notificationID = responseDatabase.createResponseRecord(0, 0);
        interactionLogger.logRegisterNotification(notificationID);
        Log.i(TAG, "just created a notification with ID " + notificationID);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID);

        ShortQuestionTask task = new MoodTask(this, notificationID);

        task.fillNotificationLayout(builder);

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID);
            builder.setPriority(NotificationCompat.PRIORITY_MAX);
        } else {
            builder.setPriority(Notification.PRIORITY_MAX);
        }

        Log.i("say", NotificationCompat.PRIORITY_MAX + "," + Notification.PRIORITY_MAX);

        // Save a copy back to cache
        cache.put(type.getID(), notification);

        notification = builder.build();

        return notification;
    }

    public void sendNotification(Type type) {
        notificationManager.notify(type.getID(), getNotification(type));
    }

    public void cancelNotification(Type type) {
        notificationManager.cancel(type.getID());
    }

    public void serviceNotifyStartingForeground(Service service, Type type) {
        service.startForeground(type.getID(), getNotification(type));
    }


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

    private int interpretIntentGetNotificationID(Intent intent) {
        return intent.getIntExtra(INTENT_EXTRA_NAME_NOTIFICATION_ID, -1);
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
