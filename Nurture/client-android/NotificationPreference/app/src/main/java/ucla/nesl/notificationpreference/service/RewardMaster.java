package ucla.nesl.notificationpreference.service;

import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

import ucla.nesl.notificationpreference.utils.Utils;

/**
 * Created by timestring on 6/17/18.
 */

public class RewardMaster {

    private boolean gotDismissed;
    private ArrayList<Double> responseHistory = new ArrayList<>();

    public RewardMaster() {
        reset();
    }


    public void feedPunishment() {
        gotDismissed = true;
    }

    public void feedReward(double responseTime) {
        responseHistory.add(responseTime);
        Log.i("RewardMaster", "got reward");
    }

    public String getRewardListAndReset() {
        ArrayList<String> rewards = new ArrayList<>();
        if (gotDismissed) {
            rewards.add("-5");
        }
        for (double responseTime : responseHistory) {
            rewards.add(String.format(Locale.getDefault(), "1:%f", responseTime));
        }

        reset();
        return Utils.stringJoin(",", rewards);
    }

    private void reset() {
        gotDismissed = false;
        responseHistory.clear();;
    }
}
