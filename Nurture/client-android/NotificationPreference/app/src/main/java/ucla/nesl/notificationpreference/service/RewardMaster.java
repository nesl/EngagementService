package ucla.nesl.notificationpreference.service;

import ucla.nesl.notificationpreference.notification.enums.NotificationEventType;

/**
 * Created by timestring on 6/17/18.
 */

public class RewardMaster {

    private boolean gotDismissed;
    private int responseCount;

    public RewardMaster() {
        reset();
    }

    public void feed(NotificationEventType event) {
        if (event == NotificationEventType.DISMISSED) {
            gotDismissed = true;
        } else if (event == NotificationEventType.RESPONDED) {
            responseCount++;
        }
    }

    public int getRewardAndReset() {
        int reward = responseCount + (gotDismissed ? -5 : 0);
        reset();
        return reward;
    }

    private void reset() {
        gotDismissed = false;
        responseCount = 0;
    }
}
