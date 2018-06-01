package ucla.nesl.notificationpreference.task.tasks.template;


import android.support.annotation.NonNull;

/**
 * Created by timestring on 5/31/18.
 */

public class HowLoudTask extends MultipleChoiceTask {

    public static final int TASK_ID = 2;

    public HowLoudTask(int notificationID) {
        super(notificationID);
    }

    public int getTypeID() {
        return TASK_ID;
    }

    @NonNull
    public String getPrimaryQuestionStatement() {
        return "How loud is it at your location?";
    }

    @NonNull
    protected String[] getOptions() {
        return new String[] {"Loud", "Noisy", "Quiet"};
    }
}
