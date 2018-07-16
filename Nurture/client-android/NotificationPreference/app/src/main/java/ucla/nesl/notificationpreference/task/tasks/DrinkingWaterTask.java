package ucla.nesl.notificationpreference.task.tasks;

import android.support.annotation.NonNull;

import ucla.nesl.notificationpreference.task.tasks.template.MultipleChoiceTask;

/**
 * Created by timestring on 7/11/18.
 *
 * Ask when was the last time the user drank water.
 */

public class DrinkingWaterTask extends MultipleChoiceTask {

    public static final int TASK_ID = 5;


    public DrinkingWaterTask(int notificationID) {
        super(notificationID);
    }

    @Override
    public int getTypeID() {
        return TASK_ID;
    }

    @NonNull
    @Override
    public String getPrimaryQuestionStatement() {
        return "How long ago did you drink water? Within ______";
    }

    @NonNull
    @Override
    protected String[] getOptions() {
        return new String[]{"1 hour", "2 hours", "Longer"};
    }
}
