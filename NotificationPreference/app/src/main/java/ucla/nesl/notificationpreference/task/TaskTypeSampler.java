package ucla.nesl.notificationpreference.task;

import java.util.Random;

/**
 * Created by timestring on 5/31/18.
 */

public class TaskTypeSampler {
    private int questionType = 0;
    private int subQuestionType = 0;

    public void sample() {
        Random random = new Random();
        questionType = random.nextInt(4);

        //questionType = 1;
    }

    public int getQuestionType() {
        return questionType;
    }

    public int getSubQuestionType() {
        return subQuestionType;
    }
}
