package ucla.nesl.notificationpreference.task;

import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import ucla.nesl.notificationpreference.notification.NotificationHelper;

/**
 * Created by timestring on 5/28/18.
 */

public class MoodTask extends ShortQuestionTask {

    public static final int TASK_ID = 0;

    private static final int BUTTON_ID_YES = 1;
    private static final int BUTTON_ID_NO = 2;

    private static final String QUESTION_STATEMENT =
            "Is it a good time to reach out you via sending this notification?";

    public MoodTask(int notificationID) {
        super(notificationID);
    }

    public int getTypeID() {
        return TASK_ID;
    }

    @NonNull
    public String getPrimaryQuestionStatement() {
        return QUESTION_STATEMENT;
    }

    @Override
    public void fillNotificationLayout(NotificationHelper notificationHelper,
                                       NotificationCompat.Builder builder) {

        builder.setContentText("Please answer the following survey question")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(QUESTION_STATEMENT))
                .addAction(android.R.drawable.checkbox_on_background, "Yes",
                        getActionPendingIndent(notificationHelper, BUTTON_ID_YES, "Yes"))
                .addAction(android.R.drawable.checkbox_on_background, "No",
                        getActionPendingIndent(BUTTON_ID_NO, "No"));
    }
}
