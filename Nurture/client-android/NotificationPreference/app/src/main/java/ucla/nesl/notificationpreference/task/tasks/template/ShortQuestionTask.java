package ucla.nesl.notificationpreference.task.tasks.template;

import android.app.PendingIntent;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.view.ViewGroup;

import ucla.nesl.notificationpreference.activity.TaskActivity;
import ucla.nesl.notificationpreference.notification.NotificationHelper;

/**
 * Created by timestring on 5/28/18.
 *
 * `ShortQuestionTask` is the base of all kind of short questions, including the multiple choices
 * or the free text problem. The main goal of a task is three-fold:
 *
 *     1) Provide the primary problem statement
 *     2) Render a notification
 *     3) Render a view for `TaskActivity`
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
     * @param _notificationHelper: The notification helper instance
     * @param _builder: The notification builder to build the layout of the notification
     */
    @CallSuper
    public void fillNotificationLayout(NotificationHelper _notificationHelper,
                                       NotificationCompat.Builder _builder) {
        notificationHelper = _notificationHelper;
    }

    public abstract ViewGroup getViewLayoutInActivity(TaskActivity taskActivity);

    @NonNull
    public abstract String getPrimaryQuestionStatement();

    protected final int getNotificationID() {
        return notificationID;
    }

    protected final PendingIntent getButtonActionPendingIndent(int buttonID, String response) {
        return notificationHelper.makeButtonActionPendingIndent(notificationID, buttonID, response);
    }

    protected final PendingIntent getInlineTextActionPendingIndent() {
        return notificationHelper.makeInlineTextActionPendingIndent(notificationID);
    }
}
