package ucla.nesl.notificationpreference.task.tasks;

import android.support.annotation.NonNull;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import ucla.nesl.notificationpreference.task.tasks.template.MultipleChoiceTask;

/**
 * Created by timestring on 7/11/18.
 *
 * Questions for the location of the user in the past, at present, or in the future
 */

public class LocationTask extends MultipleChoiceTask {

    public static final int TASK_ID = 4;

    private static final String[] availableQuestionStatements = new String[] {
            "Where were you before you came here?",
            "Where are you now?",
            "Where are you going after you leave here?"
    };

    private int questionSeed;


    public LocationTask(int notificationID, int seed) {
        super(notificationID);
        questionSeed = seed;
    }

    @Override
    public int getTypeID() {
        return TASK_ID;
    }

    public static int sampleQuestionSeedIfCreatedNow() {
        return new Random().nextInt(availableQuestionStatements.length);
    }

    public static long getCoolDownTime() {
        return TimeUnit.HOURS.toMillis(6);
    }

    @NonNull
    @Override
    public String getPrimaryQuestionStatement() {
        return availableQuestionStatements[questionSeed];
    }

    @NonNull
    @Override
    protected String[] getOptions() {
        return new String[]{"Home", "Workplace", "Others"};
    }
}
