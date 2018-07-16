package ucla.nesl.notificationpreference.task.tasks;

import android.support.annotation.NonNull;

import ucla.nesl.notificationpreference.task.tasks.template.MultipleChoiceTask;

/**
 * Created by timestring on 7/11/18.
 *
 * Questions for the location of the user in the past, at present, or in the future
 */

public class LocationTask extends MultipleChoiceTask {

    public static final int TASK_ID = 4;


    private int questionSeed;

    public LocationTask(int notificationID, int seed) {
        super(notificationID);
        questionSeed = seed;
    }

    @Override
    public int getTypeID() {
        return TASK_ID;
    }

    @NonNull
    @Override
    public String getPrimaryQuestionStatement() {
        switch (questionSeed % 3) {
            case 0:
                return "Where were you before you came here?";
            case 1:
                return "Where are you now?";
            case 2:
                return "Where are you going after you leave here?";
        }
        return "";  // make compiler happy
    }

    @NonNull
    @Override
    protected String[] getOptions() {
        return new String[]{"Home", "Workplace", "Others"};
    }
}
