package ucla.nesl.notificationpreference.notification;

/**
 * Created by timestring on 6/2/18.
 *
 * A notification event callback handler.
 */

public interface INotificationEventListener {

    /**
     * The callback of coming notification events.
     *
     * @param notificationID: Notification ID.
     * @param eventID: State defined in `NotificationResponseRecord`. The ones to be reported are:
     *                     - STATUS_APPEAR
     *                     - STATUS_RESPONDED
     */
    void onNotificationEvent(int notificationID, int eventID);
}
