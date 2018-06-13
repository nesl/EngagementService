package ucla.nesl.notificationpreference.alarm;

/**
 * Created by timestring on 5/21/18.
 *
 * `NextTrigger` is a data structure which specifies how an event is going to be scheduled. It
 * includes two pieces of information: The time interval that the event to be scheduled since now,
 * and the tolerant scheduling error. The notion of tolerance is important for being compatible with
 * current `AlarmManager` design - It discourages an event to be specified at an exact time and
 * encourages if there's a buffer of time so that events that are fired in a close time can be
 * scheduled together.
 */

public class NextTrigger {
    public long timeIntervalMs;
    public long toleranceMs;

    public NextTrigger(long _timeIntervalMs, long _toleranceMs) {
        timeIntervalMs = _timeIntervalMs;
        toleranceMs = _toleranceMs;
    }
}
