package ucla.nesl.notificationpreference.task;

import ucla.nesl.notificationpreference.storage.database.NotificationResponseRecord;
import ucla.nesl.notificationpreference.task.tasks.ArithmeticTask;
import ucla.nesl.notificationpreference.task.tasks.AvailabilityTask;
import ucla.nesl.notificationpreference.task.tasks.DietTask;
import ucla.nesl.notificationpreference.task.tasks.DrinkingWaterTask;
import ucla.nesl.notificationpreference.task.tasks.HowLoudTask;
import ucla.nesl.notificationpreference.task.tasks.LocationTask;
import ucla.nesl.notificationpreference.task.tasks.MoodTask;
import ucla.nesl.notificationpreference.task.tasks.NumberPeopleTask;
import ucla.nesl.notificationpreference.task.tasks.template.ShortQuestionTask;

/**
 * Created by timestring on 5/29/18.
 *
 * Pick the specified task.
 */

public class TaskFactory {

    public static ShortQuestionTask retrieveExistingTask(NotificationResponseRecord record) {
        return TaskFactory.getTask(record.questionType, record.subQuestionType, record.getID());
    }

    public static ShortQuestionTask getTask(
            int questionType, int subQuestionType, int notificationID) {
        switch (questionType) {
            case AvailabilityTask.TASK_ID:
                return new AvailabilityTask(notificationID);
            case MoodTask.TASK_ID:
                return new MoodTask(notificationID, subQuestionType);
            case HowLoudTask.TASK_ID:
                return new HowLoudTask(notificationID);
            case NumberPeopleTask.TASK_ID:
                return new NumberPeopleTask(notificationID);
            case LocationTask.TASK_ID:
                return new LocationTask(notificationID, subQuestionType);
            case DrinkingWaterTask.TASK_ID:
                return new DrinkingWaterTask(notificationID);
            case ArithmeticTask.TASK_ID:
                return new ArithmeticTask(notificationID, subQuestionType);
            case DietTask.TASK_ID:
                return new DietTask(notificationID, subQuestionType);
        }
        throw new IllegalArgumentException("Unrecognized question type");
    }

    // disable the constructor
    private TaskFactory() {}
}
