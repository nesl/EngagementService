package ucla.nesl.notificationpreference.task;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

import ucla.nesl.notificationpreference.utils.ArrayUtils;

/**
 * Created by timestring on 5/17/18.
 *
 * `TaskSchedulerBase` handles when to deliver tasks to users
 */

public abstract class TaskSchedulerBase {

    private int decisionIntervalSec = 0;
    private PriorityQueue<Long> eventTimestamps;

    private static Comparator<Long> comparator = new Comparator<Long>() {
        @Override
        public int compare(Long a, Long b) {
            return Long.compare(a, b);
        }
    };

    public TaskSchedulerBase() {
        eventTimestamps = new PriorityQueue<>(comparator);
    }

    protected abstract int getInitialDecisionIntervalSec();

    /**
     * After the ??WHAT?? service calls `onPlan()`, it checks whether the decision interval is
     * updated or not via this method.
     *
     * @return an interval length in seconds
     */
    public final int checkDecisionIntervalSec() {
        // if this is the first time being called, get the initial interval
        if (decisionIntervalSec == 0)
            decisionIntervalSec = getInitialDecisionIntervalSec();
        
        return decisionIntervalSec;
    }

    /**
     * For the subclass to update decision interval.
     *
     * @param seconds: The length of decision interval
     */
    protected final void updateDecisionIntervalSec(int seconds) {
        decisionIntervalSec = seconds;
    }

    /**
     * The ??WHAT?? service will call `onPlan()` based on the configured intervals (see
     * `checkDecisionIntervalSec()`. This gives the scheduler a chance to plan out what the actions
     * the scheduler should perform, e.g., send a task right away, send a task in a couple minutes.
     */
    public abstract void onPlan();

    protected void sendTaskRightAway() {
        sendTaskDelayedSec(0);
    }

    protected void sendTaskDelayedSec(int seconds) {
        eventTimestamps.offer(System.currentTimeMillis() + seconds * 1000L);
    }

    protected void clearAllTasks() {
        eventTimestamps.clear();
    }

    protected long[] getAllTasks() {
        Long[] tmp = new Long[eventTimestamps.size()];
        eventTimestamps.toArray(tmp);
        long[] result = ArrayUtils.toPrimitive(tmp);
        Arrays.sort(result);
        return result;
    }

    public Long getFirstTask() {
        return eventTimestamps.peek();
    }

    public Long removeFirstTask() {
        return eventTimestamps.poll();
    }
}
