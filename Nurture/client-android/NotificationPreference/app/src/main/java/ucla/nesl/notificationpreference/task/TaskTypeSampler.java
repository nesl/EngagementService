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
            AvailabilityTask.TASK_ID,
            MoodTask.TASK_ID,
            HowLoudTask.TASK_ID,
            NumberPeopleTask.TASK_ID,
            LocationTask.TASK_ID,
            DrinkingWaterTask.TASK_ID,
            ArithmeticTask.TASK_ID,
            DietTask.TASK_ID,
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
        switch (record.questionType) {
            case AvailabilityTask.TASK_ID:
                return timeElapsed > AvailabilityTask.getCoolDownTime();
            case MoodTask.TASK_ID:
                return timeElapsed > MoodTask.getCoolDownTime();
            case HowLoudTask.TASK_ID:
                return timeElapsed > HowLoudTask.getCoolDownTime();
            case NumberPeopleTask.TASK_ID:
                return timeElapsed > NumberPeopleTask.getCoolDownTime();
            case LocationTask.TASK_ID:
                return timeElapsed > LocationTask.getCoolDownTime();
            case DrinkingWaterTask.TASK_ID:
                return timeElapsed > DrinkingWaterTask.getCoolDownTime();
            case ArithmeticTask.TASK_ID:
                return timeElapsed > ArithmeticTask.getCoolDownTime();
            case DietTask.TASK_ID:
                return timeElapsed > DietTask.getCoolDownTime();
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
        }
        throw new IllegalArgumentException("Unrecognized question type");
    }
}
