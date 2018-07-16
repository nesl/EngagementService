package ucla.nesl.notificationpreference.task.tasks;

import android.support.annotation.NonNull;

import ucla.nesl.notificationpreference.task.tasks.template.MultipleChoiceTask;

/**
 * Created by timestring on 5/28/18.
 *
 * Ask whether the user likes this notification or not.
 */

public class AvailabilityTask extends MultipleChoiceTask {

    public static final int TASK_ID = 0;

    public AvailabilityTask(int notificationID) {
        super(notificationID);
    }

    public int getTypeID() {
        return TASK_ID;
    }

    @Override
    @NonNull
    public String getPrimaryQuestionStatement() {
        return "Is it a good time to reach out you via sending this notification?";
    }

    @Override
    @NonNull
    protected String[] getOptions() {
        return new String[] {"Yes", "No"};
    }
}
