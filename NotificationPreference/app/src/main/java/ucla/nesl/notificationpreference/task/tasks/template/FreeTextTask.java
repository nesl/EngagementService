package ucla.nesl.notificationpreference.task.tasks.template;

import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import ucla.nesl.notificationpreference.R;
import ucla.nesl.notificationpreference.activity.TaskActivity;
import ucla.nesl.notificationpreference.notification.NotificationHelper;
import ucla.nesl.notificationpreference.utils.DP;

/**
 * Created by timestring on 5/31/18.
 */

public abstract class FreeTextTask extends ShortQuestionTask {

    public FreeTextTask(int notificationID) {
        super(notificationID);
    }

    public static final String KEY_TEXT_REPLY = "key.text.reply";


    @Override
    public void fillNotificationLayout(NotificationHelper notificationHelper,
                                       NotificationCompat.Builder builder) {

        super.fillNotificationLayout(notificationHelper, builder);

        RemoteInput remoteInput = new RemoteInput.Builder(KEY_TEXT_REPLY)
                .setLabel("Your response")
                .build();

        NotificationCompat.Action action = new NotificationCompat.Action.Builder(
                R.mipmap.ic_launcher_round, "Reply", getInlineTextActionPendingIndent())
                .addRemoteInput(remoteInput)
                .build();

        builder.setContentText(getPrimaryQuestionStatement())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(
                        getPrimaryQuestionStatement()))
                .addAction(action);
    }

    @Override
    public ViewGroup getViewLayoutInActivity(TaskActivity taskActivity) {

        LinearLayout layout = new LinearLayout(taskActivity);
        layout.setOrientation(LinearLayout.VERTICAL);

        // blank space
        TextView blankSpace = new TextView(taskActivity);
        LinearLayout.LayoutParams blankSpaceLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, DP.toPX(50));
        blankSpace.setLayoutParams(blankSpaceLayoutParams);
        blankSpace.setVisibility(View.INVISIBLE);
        layout.addView(blankSpace);

        // the edit-text field
        EditText answerEdit = new EditText(taskActivity);
        LinearLayout.LayoutParams answerLayoutParameter = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int dp50 = DP.toPX(50);
        answerLayoutParameter.setMargins(dp50, dp50, dp50, dp50);
        answerEdit.setLayoutParams(answerLayoutParameter);
        answerEdit.setSingleLine();
        answerEdit.setImeOptions(EditorInfo.IME_ACTION_DONE);
        answerEdit.setOnEditorActionListener(
                taskActivity.getOnEditorActionListenerForResponse(getNotificationID()));
        layout.addView(answerEdit);

        // the submit button
        Button submitButton = new Button(taskActivity);
        submitButton.setText("Submit");

        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(
                DP.toPX(200), LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonLayoutParams.gravity = Gravity.CENTER;
        submitButton.setLayoutParams(buttonLayoutParams);
        submitButton.setOnClickListener(taskActivity.getOnClickEventListenerForSubmittingEditText(
                getNotificationID(), answerEdit));
        submitButton.setGravity(Gravity.CENTER);
        layout.addView(submitButton);

        return layout;
    }
}
