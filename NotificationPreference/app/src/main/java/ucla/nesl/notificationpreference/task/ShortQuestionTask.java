package ucla.nesl.notificationpreference.task;

import android.app.PendingIntent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.view.ViewGroup;

import ucla.nesl.notificationpreference.activity.TaskActivity;
import ucla.nesl.notificationpreference.notification.NotificationHelper;

/**
 * Created by timestring on 5/28/18.
 */

public abstract class ShortQuestionTask {

    private int notificationID;

    private NotificationHelper notificationHelper;

    public ShortQuestionTask(int _notificationID) {
        notificationID = _notificationID;
    }

    public abstract int getTypeID();

    /**
     * `fillNotificationLayout()` takes care of the visual part of the notification, including
     * the content, style, and actions. This method shouldn't configure things such as title, when
     * to trigger the notification, interaction mode, priority, etc.
     *
     * @param builder: The notification builder to build the layout of the notification
     */
    public abstract void fillNotificationLayout(NotificationHelper notificationHelper,
                                                NotificationCompat.Builder builder);

    public abstract ViewGroup getViewLayoutInActivity(TaskActivity taskActivity);

    protected final void setNotificationHelper(NotificationHelper _notificationHelper) {
        notificationHelper = _notificationHelper;
    }

    @NonNull
    public abstract String getPrimaryQuestionStatement();

    protected final int getNotificationID() {
        return notificationID;
    }

    protected final PendingIntent getActionPendingIndent(
            @NonNull NotificationHelper _notificationHelper, int buttonID, String response) {
        setNotificationHelper(_notificationHelper);
        return getActionPendingIndent(buttonID, response);
    }

    protected final PendingIntent getActionPendingIndent(int buttonID, String response) {
        if (notificationHelper == null) {
            throw new IllegalStateException("no notification helper is given");
        }
        return notificationHelper.makeActionPendingIndent(notificationID, buttonID, response);
    }
}
