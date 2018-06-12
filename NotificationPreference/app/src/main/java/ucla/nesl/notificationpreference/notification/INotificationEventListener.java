package ucla.nesl.notificationpreference.notification;

import ucla.nesl.notificationpreference.notification.enums.NotificationEventType;

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
     * @param event: Event type.
     */
    void onNotificationEvent(int notificationID, NotificationEventType event);
}
