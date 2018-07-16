package ucla.nesl.notificationpreference.task;

import java.util.Random;

/**
 * Created by timestring on 5/31/18.
 *
 * Pick a question type with a question content seed
 */

public class TaskTypeSampler {
    private int questionType = 0;
    private int subQuestionType = 0;

    public void sample() {
        Random random = new Random();
        //questionType = random.nextInt(7);
        questionType = 6;
        subQuestionType = random.nextInt(1000000);
    }

    public int getQuestionType() {
        return questionType;
    }

    public int getSubQuestionType() {
        return subQuestionType;
    }
}
