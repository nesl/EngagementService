package ucla.nesl.notificationpreference.task.scheduler;

import ucla.nesl.notificationpreference.network.HttpsPostRequest;
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
 */

public class RLTaskScheduler extends TaskSchedulerBase {

    private static final String TAG = RLTaskScheduler.class.getSimpleName();

    SharedPreferenceHelper keyValueStore;

    public RLTaskScheduler(SharedPreferenceHelper _keyValueStore) {
        super();
        keyValueStore = _keyValueStore;
    }

    @Override
    protected int getInitialDecisionIntervalSec() {
        return 60;
    }

    @Override
    public void onPlan() {
        new HttpsPostRequest()
                .setDestinationPage("mobile/get-action")
                .setParam("code", keyValueStore.getUserCode())
                .setParam("observation", "[reward];[state];[continue]")
                .setCallback(getActionCallback)
                .execute();
    }

    private HttpsPostRequest.Callback getActionCallback = new HttpsPostRequest.Callback() {
        @Override
        public void onResult(String result) {
            if (result.startsWith("action-")) {
                String action = result.substring(7);
                if (action.equals("1")) {
                    sendTaskRightAway();
                }
            }
        }
    };

}
