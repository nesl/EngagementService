package ucla.nesl.notificationpreference.task.tasks;

import android.support.annotation.NonNull;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import ucla.nesl.notificationpreference.task.tasks.template.MultipleChoiceTask;

/**
 * Created by timestring on 5/31/18.
 *
 * Mood question. The multiple choice options are derived from the following website:
 * http://quantifiedself.com/2012/12/how-is-mood-measured-get-your-mood-on-part-2/
 */

public class MoodTask extends MultipleChoiceTask {

    public static final int TASK_ID = 1;

    private static final String[][] availableOptions = new String[][] {
        {"Tense", "Neutral", "Calm"},
        {"Calm", "Neutral", "Tense"},
        {"Stressed", "Neutral", "Relaxed"},
        {"Relaxed", "Neutral", "Stressed"},
        {"Happy", "Neutral", "Sad"},
        {"Sad", "Neutral", "Happy"},
        {"Bored", "Neutral", "Excited"},
        {"Excited", "Neutral", "Bored"}
    };

    private int questionSeed;


    public MoodTask(int notificationID, int seed) {
        super(notificationID);
        questionSeed = seed;
    }

    @Override
    public int getTypeID() {
        return TASK_ID;
    }

    public static int sampleQuestionSeedIfCreatedNow() {
        return new Random().nextInt(availableOptions.length);
    }

    public static long getCoolDownTime() {
        return TimeUnit.HOURS.toMillis(3);
    }

    @NonNull
    @Override
    public String getPrimaryQuestionStatement() {
        return "Which describe your current mood?";
    }

    @NonNull
    @Override
    protected String[] getOptions() {
        return availableOptions[questionSeed];
    }
}
