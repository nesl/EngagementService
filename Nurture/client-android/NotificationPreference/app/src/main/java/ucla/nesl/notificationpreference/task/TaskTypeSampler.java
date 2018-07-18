package ucla.nesl.notificationpreference.task;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import ucla.nesl.notificationpreference.storage.database.NotificationResponseRecord;
import ucla.nesl.notificationpreference.storage.database.NotificationResponseRecordDatabase;
import ucla.nesl.notificationpreference.task.tasks.ArithmeticTask;
import ucla.nesl.notificationpreference.task.tasks.AvailabilityTask;
import ucla.nesl.notificationpreference.task.tasks.DietTask;
import ucla.nesl.notificationpreference.task.tasks.DrinkingWaterTask;
import ucla.nesl.notificationpreference.task.tasks.HowLoudTask;
import ucla.nesl.notificationpreference.task.tasks.ImageTask;
import ucla.nesl.notificationpreference.task.tasks.LocationTask;
import ucla.nesl.notificationpreference.task.tasks.MoodTask;
import ucla.nesl.notificationpreference.task.tasks.NumberPeopleTask;

/**
 * Created by timestring on 5/31/18.
 *
 * Pick a question type with a question content seed
 */

public class TaskTypeSampler {

    private static final int[] availableQuestionTypes = new int[] {
            /*AvailabilityTask.TASK_ID,
            MoodTask.TASK_ID,
            HowLoudTask.TASK_ID,
            NumberPeopleTask.TASK_ID,
            LocationTask.TASK_ID,
            DrinkingWaterTask.TASK_ID,
            ArithmeticTask.TASK_ID,
            DietTask.TASK_ID,*/
            ImageTask.TASK_ID,
    };

    private NotificationResponseRecordDatabase database;

    private int questionType = 0;
    private int subQuestionType = 0;

    public TaskTypeSampler(NotificationResponseRecordDatabase _database) {
        database = _database;
    }

    public void sample() {
        ArrayList<NotificationResponseRecord> records = database.getLastFiveRecords();
        HashSet<Integer> blackListQuestionTypes = new HashSet<>();

        for (NotificationResponseRecord record : records) {
            if (needMoreTimeCoolDown(record)) {
                blackListQuestionTypes.add(record.questionType);
            }
        }

        Random random = new Random();
        while (true) {
            int die = random.nextInt(availableQuestionTypes.length);
            questionType = availableQuestionTypes[die];
            if (!blackListQuestionTypes.contains(questionType)) {
                break;
            }
        }

        subQuestionType = getQuestionSeedNumber(questionType);
    }

    public int getQuestionType() {
        return questionType;
    }

    public int getSubQuestionType() {
        return subQuestionType;
    }


    private boolean needMoreTimeCoolDown(NotificationResponseRecord record) {
        long timeElapsed = System.currentTimeMillis() - record.createdTime;
        return timeElapsed < getTaskCoolDownTime(record.questionType);
    }

    private long getTaskCoolDownTime(int taskType) {
        switch (taskType) {
            case AvailabilityTask.TASK_ID:
                return AvailabilityTask.getCoolDownTime();
            case MoodTask.TASK_ID:
                return MoodTask.getCoolDownTime();
            case HowLoudTask.TASK_ID:
                return HowLoudTask.getCoolDownTime();
            case NumberPeopleTask.TASK_ID:
                return NumberPeopleTask.getCoolDownTime();
            case LocationTask.TASK_ID:
                return LocationTask.getCoolDownTime();
            case DrinkingWaterTask.TASK_ID:
                return DrinkingWaterTask.getCoolDownTime();
            case ArithmeticTask.TASK_ID:
                return ArithmeticTask.getCoolDownTime();
            case DietTask.TASK_ID:
                return DietTask.getCoolDownTime();
            case ImageTask.TASK_ID:
                return ImageTask.getCoolDownTime();
        }
        throw new IllegalArgumentException("Unrecognized question type");
    }

    private int getQuestionSeedNumber(int taskType) {
        switch (taskType) {
            case AvailabilityTask.TASK_ID:
                return AvailabilityTask.sampleQuestionSeedIfCreatedNow();
            case MoodTask.TASK_ID:
                return MoodTask.sampleQuestionSeedIfCreatedNow();
            case HowLoudTask.TASK_ID:
                return HowLoudTask.sampleQuestionSeedIfCreatedNow();
            case NumberPeopleTask.TASK_ID:
                return NumberPeopleTask.sampleQuestionSeedIfCreatedNow();
            case LocationTask.TASK_ID:
                return LocationTask.sampleQuestionSeedIfCreatedNow();
            case DrinkingWaterTask.TASK_ID:
                return DrinkingWaterTask.sampleQuestionSeedIfCreatedNow();
            case ArithmeticTask.TASK_ID:
                return ArithmeticTask.sampleQuestionSeedIfCreatedNow();
            case DietTask.TASK_ID:
                return DietTask.sampleQuestionSeedIfCreatedNow(
                        database.getLastRecordByType(DietTask.TASK_ID));
            case ImageTask.TASK_ID:
                return ImageTask.sampleQuestionSeedIfCreatedNow();
        }
        throw new IllegalArgumentException("Unrecognized question type");
    }
}
