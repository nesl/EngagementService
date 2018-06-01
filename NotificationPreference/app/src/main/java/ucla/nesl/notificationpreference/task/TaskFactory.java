package ucla.nesl.notificationpreference.task;

import android.util.Log;

import ucla.nesl.notificationpreference.storage.NotificationResponseRecord;
import ucla.nesl.notificationpreference.task.tasks.AvailabilityTask;
import ucla.nesl.notificationpreference.task.tasks.MoodTask;
import ucla.nesl.notificationpreference.task.tasks.template.HowLoudTask;
import ucla.nesl.notificationpreference.task.tasks.template.ShortQuestionTask;

/**
 * Created by timestring on 5/29/18.
 */

public class TaskFactory {

    public static ShortQuestionTask retrieveExistingTask(NotificationResponseRecord record) {
        return TaskFactory.getTask(record.questionType, record.subQuestionType, record.getID());
    }

    public static ShortQuestionTask getTask(
            int questionType, int subQuestionType, int notificationID) {
        Log.i("TaskFactory", "Debug:" + questionType + " " + notificationID);
        switch (questionType) {
            case AvailabilityTask.TASK_ID:
                return new AvailabilityTask(notificationID);
            case MoodTask.TASK_ID:
                return new MoodTask(notificationID);
            case HowLoudTask.TASK_ID:
                return new HowLoudTask(notificationID);
        }
        throw new IllegalArgumentException("Unrecognized question type");
    }

    // disable the constructor
    private TaskFactory() {}
}
