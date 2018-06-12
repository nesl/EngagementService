package ucla.nesl.notificationpreference.task.tasks;

import android.support.annotation.NonNull;

import ucla.nesl.notificationpreference.task.tasks.template.FreeTextTask;

/**
 * Created by timestring on 5/31/18.
 */

public class MoodTask extends FreeTextTask {

    public static final int TASK_ID = 1;


    public MoodTask(int notificationID) {
        super(notificationID);
    }

    public int getTypeID() {
        return TASK_ID;
    }

    @NonNull
    public String getPrimaryQuestionStatement() {
        return "Please your current mood in one or two words.";
    }
}
