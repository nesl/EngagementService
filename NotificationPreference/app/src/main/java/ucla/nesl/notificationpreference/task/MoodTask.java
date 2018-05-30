package ucla.nesl.notificationpreference.task;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import ucla.nesl.notificationpreference.activity.TaskActivity;
import ucla.nesl.notificationpreference.notification.NotificationHelper;
import ucla.nesl.notificationpreference.storage.NotificationInteractionEventLogger;
import ucla.nesl.notificationpreference.storage.NotificationResponseRecordDatabase;
import ucla.nesl.notificationpreference.utils.DP;

/**
 * Created by timestring on 5/28/18.
 */

public class MoodTask extends ShortQuestionTask {

    public static final int TASK_ID = 0;

    private static final int BUTTON_ID_YES = 1;
    private static final int BUTTON_ID_NO = 2;

    private static final String RESPONSE_TEXT_YES = "Yes";
    private static final String RESPONSE_TEXT_NO = "No";

    private static final String RESPONSE_VALUE_YES = "Yes";
    private static final String RESPONSE_VALUE_NO = "No";

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
                .addAction(android.R.drawable.checkbox_on_background, RESPONSE_TEXT_YES,
                        getActionPendingIndent(notificationHelper, BUTTON_ID_YES, RESPONSE_VALUE_YES))
                .addAction(android.R.drawable.checkbox_on_background, RESPONSE_TEXT_NO,
                        getActionPendingIndent(BUTTON_ID_NO, RESPONSE_VALUE_NO));
    }

    public ViewGroup getViewLayoutInActivity(TaskActivity taskActivity) {

        LinearLayout layout = new LinearLayout(taskActivity);
        layout.setOrientation(LinearLayout.VERTICAL);

        // blank space
        TextView blankSpace = new TextView(taskActivity);
        LinearLayout.LayoutParams blankSpaceLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, DP.toPX(80));
        blankSpace.setLayoutParams(blankSpaceLayoutParams);
        blankSpace.setVisibility(View.INVISIBLE);
        layout.addView(blankSpace);

        // buttons
        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(
                DP.toPX(200), LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonLayoutParams.gravity = Gravity.CENTER;

        Button yesButton = new Button(taskActivity);
        yesButton.setText(RESPONSE_TEXT_YES);
        yesButton.setLayoutParams(buttonLayoutParams);
        yesButton.setOnClickListener(taskActivity.getOnClickEventListenerForResponse(
                getNotificationID(), RESPONSE_VALUE_YES));
        //yesButton.setGravity(Gravity.CENTER);
        layout.addView(yesButton);

        Button noButton = new Button(taskActivity);
        noButton.setText(RESPONSE_TEXT_NO);
        noButton.setLayoutParams(buttonLayoutParams);
        noButton.setOnClickListener(taskActivity.getOnClickEventListenerForResponse(
                getNotificationID(), RESPONSE_VALUE_NO));
        //noButton.setGravity(Gravity.CENTER);
        layout.addView(noButton);

        return layout;
    }
}
