package ucla.nesl.notificationpreference.task.scheduler;

import android.util.Log;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import ucla.nesl.notificationpreference.network.HttpsPostRequest;
import ucla.nesl.notificationpreference.service.RewardMaster;
import ucla.nesl.notificationpreference.service.SensorMaster;
import ucla.nesl.notificationpreference.storage.SharedPreferenceHelper;

/**
 * Created by timestring on 6/14/18.
 *
 * The task scheduler sends the reward of the previous step and the current state to the
 * reinforcement learning server. The server then returns an action to the client for letting the
 * task out or not.
 *
 *       state-1   action-1   reward-1   state-2   action-2   ...
 *                            <------ request cycle ------>
 *
 * The full communication protocol is in the following Google Doc:
 * https://docs.google.com/document/d/1YUBMl02jqsc6ChYNuLhf8mVD3tM-GUGq_BIwp8bkx4A/edit#heading=h.jq7n9reoji4t
 */

public class RLTaskScheduler extends TaskSchedulerBase {

    private static final String TAG = RLTaskScheduler.class.getSimpleName();

    private SharedPreferenceHelper keyValueStore;
    private SensorMaster sensorMaster;
    private RewardMaster rewardMaster;

    // the following variables keeps the state and reward where they left from the last request
    // failure attempt
    private String stateHolder = null;
    private int rewardHolder = 0;
    private long lastRequestFailureTimestamp = 0L;

    public RLTaskScheduler(SharedPreferenceHelper _keyValueStore, SensorMaster _sensorMaster,
                           RewardMaster _rewardMaster) {
        super();
        keyValueStore = _keyValueStore;
        sensorMaster = _sensorMaster;
        rewardMaster = _rewardMaster;

        resetStateRewardHolder();
    }

    @Override
    protected int getInitialDecisionIntervalSec() {
        return 60;
    }

    @Override
    public void onPlan() {
        String currentState = sensorMaster.getStateMessageAndReset();
        int currentReward = rewardMaster.getRewardAndReset();
        long now = System.currentTimeMillis();

        String observation;
        if (stateHolder == null) {
            // if the previous request is successful, then send reward1-state1-continue
            observation = String.format(Locale.getDefault(),
                    "[%d];[%s];[continue]", currentReward, currentState);
            stateHolder = currentState;
            rewardHolder = currentReward;
            lastRequestFailureTimestamp = now;
        } else {
            long timeElapsed = now - lastRequestFailureTimestamp;
            if (timeElapsed > TimeUnit.MINUTES.toMillis(5)) {
                // if the previous request failure is not too far away (i.e., within the past 5
                // minutes), we consider it as a transit error. The message to be sent is
                // reward1-stateN-continue.
                observation = String.format(Locale.getDefault(),
                        "[%d];[%s];[continue]", rewardHolder, currentState);
            } else {
                // if the system keeps trying but the message cannot get out after a certain time
                // threshold (i.e., longer than 5 minutes), we consider there's a network problem
                // and inform the server to restart a new episode.
                observation = String.format(Locale.getDefault(),
                        "[%d];[%s];[discontinue];[%s]", rewardHolder, stateHolder, currentState);
            }
        }

        Log.i(TAG, "Observation=" + observation);

        new HttpsPostRequest()
                .setDestinationPage("mobile/get-action")
                .setParam("code", keyValueStore.getUserCode())
                .setParam("observation", observation)
                .setCallback(getActionCallback)
                .execute();
    }

    private HttpsPostRequest.Callback getActionCallback = new HttpsPostRequest.Callback() {
        @Override
        public void onResult(String result) {
            if (result == null || !result.startsWith("action-")) {
                return;
            }

            // process the action
            String action = result.substring(7);
            if (action.equals("1")) {
                sendTaskRightAway();
            }

            // reset the state-reward holder
            resetStateRewardHolder();
        }
    };

    private void resetStateRewardHolder() {
        stateHolder = null;
        rewardHolder = 0;
        lastRequestFailureTimestamp = 0L;
    }
}
