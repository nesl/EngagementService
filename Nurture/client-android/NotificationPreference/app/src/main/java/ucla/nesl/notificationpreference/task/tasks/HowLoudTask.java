package ucla.nesl.notificationpreference.task.tasks;


import android.support.annotation.NonNull;

import java.util.concurrent.TimeUnit;

import ucla.nesl.notificationpreference.task.tasks.template.MultipleChoiceTask;

/**
 * Created by timestring on 5/31/18.
 *
 * Multiple choice of how loud of the current place is
 */

public class HowLoudTask extends MultipleChoiceTask {

    public static final int TASK_ID = 2;

    public HowLoudTask(int notificationID) {
        super(notificationID);
    }

    public int getTypeID() {
        return TASK_ID;
    }

    public static int sampleQuestionSeedIfCreatedNow() {
        return 0;
    }

    public static long getCoolDownTime() {
        return TimeUnit.HOURS.toMillis(3);
    }

    @Override
    @NonNull
    public String getPrimaryQuestionStatement() {
        return "How loud is it at your location?";
    }

    @Override
    @NonNull
    protected String[] getOptions() {
        return new String[] {"Loud", "Moderate", "Quiet"};
    }
}
