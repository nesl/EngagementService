package ucla.nesl.notificationpreference.task;

import android.support.v4.app.NotificationCompat;

import ucla.nesl.notificationpreference.notification.NotificationHelper;

/**
 * Created by timestring on 5/28/18.
 */

public class MoodTask extends ShortQuestionTask {

    private static final int BUTTON_ID_YES = 1;
    private static final int BUTTON_ID_NO = 2;

    public MoodTask(int notificationID) {
        super(notificationID);
    }

    @Override
    public void fillNotificationLayout(NotificationHelper notificationHelper,
                                       NotificationCompat.Builder builder) {

        builder.setContentText("Please answer the following survey question")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Is it a good time to reach out you via sending this notification?"))
                .addAction(android.R.drawable.checkbox_on_background, "Yes",
                        getActionPendingIndent(notificationHelper, BUTTON_ID_YES, "Yes"))
                .addAction(android.R.drawable.checkbox_on_background, "No",
                        getActionPendingIndent(BUTTON_ID_NO, "No"));
    }
}
