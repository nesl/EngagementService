package ucla.nesl.notificationpreference.task.tasks;

import android.support.annotation.NonNull;

import java.util.concurrent.TimeUnit;

import ucla.nesl.notificationpreference.storage.database.NotificationResponseRecord;
import ucla.nesl.notificationpreference.task.tasks.template.MultipleChoiceTask;

/**
 * Created by timestring on 7/16/18.
 *
 * The questions are originated from
 * https://www.bhf.org.uk/~/media/files/publications/health-at-work/health-at-work-how-healthy-is-your-diet-questionnaire.pdf
 */

public class DietTask extends MultipleChoiceTask {

    public static final int TASK_ID = 7;

    private static final String[] questions = new String[] {
            // Eating habits
            "Do you skip breakfast more than once a week?",
            "Do you skip lunch more than once a week?",
            "Do you skip evening meals more than once a week?",
            "Do you skip meals and snack instead on most days?",
            // Fruit and vegetables
            "Do you eat more than 5 portions of fruit and/or vegetables every day? " +
                    "(a portion is about a handful)",
            "Do you eat more than 4 different varieties of fruit each week?",
            "Do you eat more than 4 different varieties of vegetables each week?",
            // Fat
            "Do you choose low-fat products when available?",
            "Do you choose baked, steamed or grilled options when available, " +
                    "rather than fried foods (such as crisps and snacks, or fish and chips)?",
            "Do you opt for lean cuts of meat or remove visible fat – for example, " +
                    "removing the skin on chicken or the rind on bacon?",
            "Did you eat any oily fish last week? Examples of oily fish include " +
                    "salmon, mackerel, herring, sardines, trout, and fresh tuna.",
            "Do you include some unsalted nuts and seeds in your diet?",
            // Starchy foods
            "Do you base your main meals around starchy foods? " +
                    "For example, potatoes, pasta, rice or bread.",
            "Do you regularly choose wholemeal bread or rolls rather than white?",
            "Do you regularly eat wholegrain cereals, with no added sugar?",
            "Do you regularly include pulses in your diet? For example, beans and lentils.",
            // Sugar
            "Do you regularly eat sugar-coated breakfast cereals " +
                    "or add sugar to your breakfast cereals?",
            "Do you add sugar to your drinks?",
            "Do you regularly drink sweet fizzy drinks?",
            "Do you regularly eat cakes, sweets, chocolate or biscuits at work?",
            // Salt
            "Do you regularly add salt to food during cooking?",
            "Do you regularly add salt to meals at the table?",
            "Do you regularly eat savoury snacks at work? For example, crisps or salted nuts.",
            "Do you regularly eat pre-prepared meals? For example, " +
                    "pre-prepared sandwiches, ready meals or canned soups.",
            "Do you regularly eat processed meats such as ham or bacon or smoked fish?",
            "Has your general practitioner advised you that you have high blood pressure?",
            // Drinks and alcohol
            "Do you drink plenty of fluids at regular intervals during, the working day?",
            "Do you opt for a variety of different drinks, including water, at work?",
            "Do you avoid sugary fizzy drinks?",
            "Do you drink no more than 3 units of alcohol a day?",
    };


    private int questionSeed;

    public DietTask(int notificationID, int seed) {
        super(notificationID);
        questionSeed = seed;
    }

    @Override
    public int getTypeID() {
        return TASK_ID;
    }

    public static int sampleQuestionSeedIfCreatedNow(NotificationResponseRecord lastRecord) {
        if (lastRecord == null) {
            return 0;
        } else {
            return (lastRecord.subQuestionType + 1) % questions.length;
        }
    }

    public static long getCoolDownTime() {
        return TimeUnit.HOURS.toMillis(1);
    }

    @NonNull
    @Override
    public String getPrimaryQuestionStatement() {
        return questions[questionSeed];
    }

    @NonNull
    @Override
    protected String[] getOptions() {
        return new String[]{"Yes", "No"};
    }
}
