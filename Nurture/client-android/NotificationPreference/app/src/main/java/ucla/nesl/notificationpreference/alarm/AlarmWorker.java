package ucla.nesl.notificationpreference.alarm;

import android.support.annotation.NonNull;

/**
 * Created by timestring on 5/21/18.
 *
 * An `AlarmWorker` schedules a series of events. To be specific, when `AlarmWorker` is created
 * and registered in `AlarmEventManager`, it will be queried and process the first event. When an
 * event is processed, `AlarmWorker` has to notify when the event is supposed to be scheduled
 * after the current event is processed. Note that in the current implementation, there's no way to
 * cancel a scheduled event.
 *
 * Please see `AlarmEventManager` class for more information.
 */

public abstract class AlarmWorker {

    /**
     * The function is called when the scheduled event is going to be processed. While processing
     * the event, it is anticipated to schedule the next event.
     *
     * @return `NextTrigger` for the time of the next event.
     */
    @NonNull
    protected abstract NextTrigger onPlan();

    /**
     * Indicate the task of this `AlarmWorker` is time sensitive or not. Since Android pushes the
     * phone to doze/idle mode after the screen is turned off, this method indicates the need of
     * overriding the mode and force the task to be executed.
     */
    protected abstract boolean requireBackgroundExecution();
}
