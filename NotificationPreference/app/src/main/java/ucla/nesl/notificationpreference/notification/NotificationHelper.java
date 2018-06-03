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
import ucla.nesl.notificationpreference.notification.receiver.NotificationButtonActionProxyReceiver;
import ucla.nesl.notificationpreference.notification.receiver.NotificationInlineTextProxyReceiver;
import ucla.nesl.notificationpreference.storage.NotificationInteractionEventLogger;
import ucla.nesl.notificationpreference.storage.NotificationResponseRecord;
import ucla.nesl.notificationpreference.storage.NotificationResponseRecordDatabase;
import ucla.nesl.notificationpreference.task.tasks.template.ShortQuestionTask;
import ucla.nesl.notificationpreference.task.TaskFactory;
import ucla.nesl.notificationpreference.task.TaskTypeSampler;

/**
 * Originally created by timestring on 2/12/18.
 * Copied to the project on 5/18/18.
 *
 * `NotificationHelper` provides the following features/functionality:
 *    1) Register notifications in `NotificationService`
 *    2) Deliver notification events to the subscribed listeners (notification is sent, notification
 *       is responded)
 *
 * Receiving notification responses has to be done in an asynchronous manner, specifically, the
 * `NotificationManager` will broadcast the specified `Intent` and we have to register a
 * `BroadcastReceiver` or some other Android components to receive it. However, to complete the
 * feedback loop (i.e., to receive this response information to update the `NotificationHelper`
 * object), several notification proxy receivers are implemented (e.g.,
 * `NotificationButtonActionProxyReceiver`.)
 *
 * `NotificationHelper` also logs user responses and interaction events (e.g., notification is
 * responded). However, as the design of aforementioned message forwarding mechanism, all the
 * `NotificationHelper` instances will receive the `Intent`. Hence, the app developer needs to
 * assure that exactly one instance is instantiated in the logging enabled mode. Otherwise,
 * duplicated interaction events will be logged.
 */

public class NotificationHelper {

    private static final String TAG = NotificationHelper.class.getSimpleName();

    private static final String INTENT_CREATE_NOTIFICATION = "intent.create.notification";
    public static final String INTENT_FORWARD_NOTIFICATION_RESPONSE_ACTION = "intent.forward.notification.response.action";

    public static final int NOTIFICATION_ID_NOT_SET = -1;

    private static final String INTENT_EXTRA_NAME_NOTIFICATION_ID = "notificationID";
    private static final String INTENT_EXTRA_NAME_RESPONSE = "response";
    private static final int OFFSET = 100000;

    private static final String CHANNEL_ID = "channel_0";

    private static final String CHANNEL_GROUP_ID = "group_0";
    private static final String CHANNEL_GROUP_NAME = "group_0_name";

    private Context context;
    private boolean loggingEnabled;
    private INotificationEventListener eventListener;

    private NotificationManager notificationManager;

    private SparseArray<Notification> cache = new SparseArray<>();

    private NotificationResponseRecordDatabase responseDatabase;
    private NotificationInteractionEventLogger interactionLogger;


    //region Section: Initialization
    // =============================================================================================

    public NotificationHelper(Context _context, boolean _loggingEnabled) {
        this(_context, _loggingEnabled, null);
    }

    /**
     * Different components (activities, services) may instantiate a `NotificationHelper`. However,
     * as the fact of how the message passing mechanism is designed in `NotificationHelper`, when a
     * notification is responded, all the `NotificationHelper` instances will receive the signal.
     * The app developer needs to make sure who is the master instance and only that one should
     * enable logging.
     *
     * @param _context: For acquiring notification service from the OS
     * @param _loggingEnabled: Indicate whether this instance should log the events. There should
     *                         only be one instance which enables logging.
     * @param _eventListener: A callback triggered when a new notification is scheduled or a
     *                        response is received.
     */
    public NotificationHelper(Context _context, boolean _loggingEnabled,
                              INotificationEventListener _eventListener) {
        context = _context;
        loggingEnabled = _loggingEnabled;
        eventListener = _eventListener;

        notificationManager = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE);

        // create a notification channel as Android O requires
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //notificationManager.createNotificationChannelGroup(
            //        new NotificationChannelGroup(CHANNEL_GROUP_ID, CHANNEL_GROUP_NAME)
            //);

            // the importance of notification channel has to be the highest possible so that users
            // can see the heads-up style notifications (only if the priority of the notifications
            // are also configured to be the highest possible)
            CharSequence name = context.getString(R.string.app_name);
            NotificationChannel mChannel = new NotificationChannel(
                    CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);
            mChannel.enableLights(true);
            mChannel.enableVibration(true);
            mChannel.setLightColor(Color.GREEN);
            notificationManager.createNotificationChannel(mChannel);
            //mChannel.setGroup(CHANNEL_GROUP_ID);
        }

        // register local broadcast receiver for notification creating event and response callback
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
        localBroadcastManager.registerReceiver(responseReceiver,
                new IntentFilter(INTENT_FORWARD_NOTIFICATION_RESPONSE_ACTION));
        localBroadcastManager.registerReceiver(createEventReceiver,
                new IntentFilter(INTENT_CREATE_NOTIFICATION));

        // acquire notification event loggers if logging is enabled
        if (loggingEnabled) {
            responseDatabase = NotificationResponseRecordDatabase.getAppDatabase(context);
            interactionLogger = NotificationInteractionEventLogger.getInstance();
        }
    }
    //endregion

    //region Section: Main operations (send and cancel notifications)
    // =============================================================================================
    public int createAndSendTaskNotification() {

        if (!loggingEnabled) {
            throw new IllegalStateException(
                    "Sending notification is blocked if logging is disabled");
        }

        // decide task type
        TaskTypeSampler taskSampler = new TaskTypeSampler();
        taskSampler.sample();
        int questionType = taskSampler.getQuestionType();
        int subQuestionType = taskSampler.getSubQuestionType();

        // register a notification record in database
        int notificationID = responseDatabase.createResponseRecord(questionType, subQuestionType);
        interactionLogger.logRegisterNotification(notificationID);
        Log.i(TAG, "just created a notification with ID " + notificationID);

        // fill task-specific content in the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);

        ShortQuestionTask task = TaskFactory.getTask(questionType, subQuestionType, notificationID);

        task.fillNotificationLayout(this, builder);

        // configure non-UI part of the notification
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                //.setLargeIcon(R.mipmap.)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(makeActivityPendingIndent(notificationID))
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentTitle("Please answer short question")
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

        Intent creatingEventIntent = new Intent(
                NotificationHelper.INTENT_CREATE_NOTIFICATION);
        creatingEventIntent.putExtra(INTENT_EXTRA_NAME_NOTIFICATION_ID, notificationID);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
        localBroadcastManager.sendBroadcast(creatingEventIntent);

        return notificationID;
    }

    public void cancelNotification(int notificationID) {
        notificationManager.cancel(notificationID);
    }
    //endregion

    //region Section: Notification response receiver
    // =============================================================================================
    private final BroadcastReceiver createEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int notificationID = interpretIntentGetNotificationID(intent);
            notifyEventListener(notificationID, NotificationResponseRecord.STATUS_APPEAR);
        }
    };

    private final BroadcastReceiver responseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int notificationID = interpretIntentGetNotificationID(intent);
            String response = interpretIntentGetResponse(intent);
            Log.i("NotificationHelper", "Receive response:" + response);

            if (loggingEnabled) {
                responseDatabase.fillAnswer(notificationID, response);
                interactionLogger.logRespondInNotification(notificationID, response);
            }

            notifyEventListener(notificationID, NotificationResponseRecord.STATUS_RESPONDED);

            cancelNotification(notificationID);
        }
    };
    //endregion

    //region Section: PendingIntent factory and interpreter
    // =============================================================================================
    public static void overloadInfoOnIntentForActivity(Intent intent, int notificationID) {
        intent.putExtra(INTENT_EXTRA_NAME_NOTIFICATION_ID, notificationID);
    }

    public static void overloadIDAndResponseOnIntent(
            Intent intent, int notificationID, @NonNull String response) {
        intent.putExtra(INTENT_EXTRA_NAME_NOTIFICATION_ID, notificationID);
        intent.putExtra(INTENT_EXTRA_NAME_RESPONSE, response);
    }

    private PendingIntent makeActivityPendingIndent(int notificationID) {
        Intent intent = new Intent(context, TaskActivity.class);
        overloadInfoOnIntentForActivity(intent, notificationID);
        int requestCode = notificationID * OFFSET;
        return PendingIntent.getActivity(context, requestCode, intent, 0);
    }

    public PendingIntent makeButtonActionPendingIndent(
            int notificationID, int buttonID, String response) {

        if (buttonID <= 0 || buttonID >= OFFSET) {
            throw new IllegalArgumentException("button ID has to be 1 ~ " + (OFFSET - 1));
        }

        Intent intent = new Intent(context, NotificationButtonActionProxyReceiver.class);
        int requestCode = notificationID * OFFSET + buttonID;
        overloadIDAndResponseOnIntent(intent, notificationID, response);
        return PendingIntent.getBroadcast(context, requestCode, intent, 0);
    }

    public PendingIntent makeInlineTextActionPendingIndent(int notificationID) {
        Log.i(TAG, "in makeInlineTextActionPendingIndent()");
        Intent intent = new Intent(context, NotificationInlineTextProxyReceiver.class);
        int requestCode = notificationID * OFFSET;
        overloadInfoOnIntentForActivity(intent, notificationID);
        return PendingIntent.getBroadcast(context, requestCode, intent, 0);
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

    //region Section: Callback of event listener
    // =============================================================================================
    private void notifyEventListener(int notificationID, int eventID) {
        if (eventListener != null) {
            eventListener.onNotificationEvent(notificationID, eventID);
        }
    }
    //endregion
}
