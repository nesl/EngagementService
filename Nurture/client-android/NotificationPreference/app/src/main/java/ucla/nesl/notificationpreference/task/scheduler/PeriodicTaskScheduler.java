package ucla.nesl.notificationpreference.task.scheduler;

/**
 * Created by timestring on 5/18/18.
 *
 * `PeriodicTaskScheduler` sends tasks in a fixed time interval.
 */

public class PeriodicTaskScheduler extends TaskSchedulerBase {

    private static final String TAG = PeriodicTaskScheduler.class.getSimpleName();

    private int decisionIntervalSeconds;

    public PeriodicTaskScheduler(int intervalSeconds) {
        super();
        decisionIntervalSeconds = intervalSeconds;
    }

    @Override
    protected int getInitialDecisionIntervalSec() {
        return decisionIntervalSeconds;
    }

    @Override
    public void onPlan() {
        sendTaskRightAway();
    }
}
