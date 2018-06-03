package ucla.nesl.notificationpreference.task.tasks;

import android.support.annotation.NonNull;

import ucla.nesl.notificationpreference.task.tasks.template.FreeTextTask;

/**
 * Created by timestring on 5/31/18.
 *
 * Ask how many people are there around the user.
 */

public class NumberPeopleTask extends FreeTextTask {

    public static final int TASK_ID = 3;


    public NumberPeopleTask(int notificationID) {
        super(notificationID);
    }

    public int getTypeID() {
        return TASK_ID;
    }

    @NonNull
    public String getPrimaryQuestionStatement() {
        return "How many people are there around you?";
    }
}
