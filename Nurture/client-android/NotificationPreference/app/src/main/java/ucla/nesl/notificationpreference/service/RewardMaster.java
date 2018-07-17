package ucla.nesl.notificationpreference.service;

import java.util.ArrayList;

import ucla.nesl.notificationpreference.notification.enums.NotificationEventType;
import ucla.nesl.notificationpreference.utils.Utils;

/**
 * Created by timestring on 6/17/18.
 */

public class RewardMaster {

    private boolean gotDismissed;
    private ArrayList<Integer> responseHistory = new ArrayList<>();

    public RewardMaster() {
        reset();
    }

    public void feed(NotificationEventType event) {
        if (event == NotificationEventType.DISMISSED) {
            gotDismissed = true;
        } else if (event == NotificationEventType.RESPONDED) {
            responseHistory.add(1);
        }
    }

    public String getRewardListAndReset() {
        ArrayList<Integer> rewards = new ArrayList<>();
        if (gotDismissed) {
            rewards.add(-5);
        }
        rewards.addAll(responseHistory);
        reset();
        return Utils.stringJoinInts(",", rewards);
    }

    private void reset() {
        gotDismissed = false;
        responseHistory.clear();;
    }
}
