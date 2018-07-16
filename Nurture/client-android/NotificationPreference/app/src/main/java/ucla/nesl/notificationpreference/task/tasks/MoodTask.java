package ucla.nesl.notificationpreference.task.tasks;

import android.support.annotation.NonNull;
import android.util.Log;

import ucla.nesl.notificationpreference.task.tasks.template.MultipleChoiceTask;

/**
 * Created by timestring on 5/31/18.
 *
 * Mood question. The multiple choice options are derived from the following website:
 * http://quantifiedself.com/2012/12/how-is-mood-measured-get-your-mood-on-part-2/
 */

public class MoodTask extends MultipleChoiceTask {

    public static final int TASK_ID = 1;


    private int questionSeed;

    public MoodTask(int notificationID, int seed) {
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
        return "Which describe your current mood?";
    }

    @NonNull
    @Override
    protected String[] getOptions() {
        Log.i("Mood", "seed=" + questionSeed);
        switch (questionSeed % 8) {
            case 0:
                return new String[] {"Tense", "Neutral", "Calm"};
            case 1:
                return new String[] {"Calm", "Neutral", "Tense"};
            case 2:
                return new String[] {"Stressed", "Neutral", "Relaxed"};
            case 3:
                return new String[] {"Relaxed", "Neutral", "Stressed"};
            case 4:
                return new String[] {"Happy", "Neutral", "Sad"};
            case 5:
                return new String[] {"Sad", "Neutral", "Happy"};
            case 6:
                return new String[] {"Bored", "Neutral", "Excited"};
            case 7:
                return new String[] {"Excited", "Neutral", "Bored"};
        }
        return new String[0];  // make compiler happy
    }
}
