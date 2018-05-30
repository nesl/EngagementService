package ucla.nesl.notificationpreference.task;

import ucla.nesl.notificationpreference.storage.NotificationResponseRecord;

/**
 * Created by timestring on 5/29/18.
 */

public class TaskFactory {

    public static ShortQuestionTask getTask(
            int questionType, int subQuestionType, int notificationID) {
        switch (questionType) {
            case MoodTask.TASK_ID:
                return new MoodTask(notificationID);
        }
        throw new IllegalArgumentException("Unrecognized question type");
    }

    public static ShortQuestionTask retrieveExistingTask(NotificationResponseRecord record) {
        return TaskFactory.getTask(record.questionType, record.subQuestionType, record.getID());
    }

    private TaskFactory() {}
}
