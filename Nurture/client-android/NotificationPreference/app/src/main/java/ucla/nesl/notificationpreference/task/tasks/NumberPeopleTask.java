package ucla.nesl.notificationpreference.task.tasks;

import android.support.annotation.NonNull;

import ucla.nesl.notificationpreference.task.tasks.template.MultipleChoiceTask;

/**
 * Created by timestring on 5/31/18.
 *
 * Ask how many people are there around the user.
 */

public class NumberPeopleTask extends MultipleChoiceTask {

    public static final int TASK_ID = 3;


    public NumberPeopleTask(int notificationID) {
        super(notificationID);
    }

    @NonNull
    @Override
    protected String[] getOptions() {
        return new String[] {"0~5", "6~20", "20~50", ">50"};
    }

    @Override
    public int getTypeID() {
        return TASK_ID;
    }

    @NonNull
    @Override
    public String getPrimaryQuestionStatement() {
        return "How many people are there around you?";
    }
}
