package ucla.nesl.notificationpreference.task.tasks;

import android.support.annotation.NonNull;

import java.util.concurrent.TimeUnit;

import ucla.nesl.notificationpreference.task.tasks.template.MultipleChoiceTask;

/**
 * Created by timestring on 5/31/18.
 *
 * Ask how many people are there around the user.
 */

public class NumberPeopleTask extends MultipleChoiceTask {

    public static final int TASK_ID = 3;


    /**
     * Constructors for both creating and retrieving a task
     */
    public NumberPeopleTask(int notificationID) {
        super(notificationID);
    }

    @NonNull
    @Override
    protected String[] getOptions() {
        return new String[] {"0~5", "6~20", ">20"};
    }

    @Override
    public int getTypeID() {
        return TASK_ID;
    }

    public static int sampleQuestionSeedIfCreatedNow() {
        return 0;
    }

    public static long getCoolDownTime() {
        return TimeUnit.HOURS.toMillis(6);
    }

    @NonNull
    @Override
    public String getPrimaryQuestionStatement() {
        return "How many people are there around you?";
    }
}
