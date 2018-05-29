package ucla.nesl.notificationpreference.task;

import android.app.PendingIntent;
import android.support.v4.app.NotificationCompat;

import ucla.nesl.notificationpreference.notification.NotificationHelper;

/**
 * Created by timestring on 5/28/18.
 */

public abstract class ShortQuestionTask {

    private NotificationHelper notificationHelper;
    private int notificationID;

    public ShortQuestionTask(NotificationHelper _notificationHelper, int _notificationID) {
        notificationHelper = _notificationHelper;
        notificationID = _notificationID;
    }

    /**
     * `fillNotificationLayout()` takes care of the visual part of the notification, including
     * the content, style, and actions. This method shouldn't configure things such as title, when
     * to trigger the notification, interaction mode, priority, etc.
     *
     * @param builder: The notification builder to build the layout of the notification
     */
    public abstract void fillNotificationLayout(NotificationCompat.Builder builder);

    //TODO: abstract void getViewLayoutInActivity();

    protected final PendingIntent getActionPendingIndent(int buttonID, String response) {
        return notificationHelper.makeActionPendingIndent(notificationID, buttonID, response);
    }
}
