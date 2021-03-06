package ucla.nesl.notificationpreference.task.tasks.template;

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
import ucla.nesl.notificationpreference.utils.DP;

/**
 * Created by timestring on 5/31/18.
 *
 * `MultipleChoiceTask` provides templates for both the notification part and the view for
 * `TaskActivity`. It requires the subclass to provide the options in `getOptions` and it will
 * take care of the rest of the rendering jobs.
 *
 * Note button ID (or option ID) has to be 1-index to be compatible with
 * `NotificationHelper.makeButtonActionPendingIndent()`.
 */

public abstract class MultipleChoiceTask extends ShortQuestionTask {

    public MultipleChoiceTask(int notificationID) {
        super(notificationID);
    }

    @NonNull
    protected abstract String[] getOptions();

    @Override
    public void fillNotificationLayout(NotificationHelper notificationHelper,
                                       NotificationCompat.Builder builder) {

        super.fillNotificationLayout(notificationHelper, builder);

        // primary problem statement
        builder.setContentText(getPrimaryQuestionStatement())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(
                        getPrimaryQuestionStatement()));

        // choices
        String[] options = getOptions();
        for (int i = 0; i < options.length; i++) {
                builder.addAction(android.R.drawable.checkbox_on_background, options[i],
                    getButtonActionPendingIndent(i + 1, options[i]));
        }
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

        String[] options = getOptions();
        for (int i = 0; i < options.length; i++) {
            String option = options[i];
            int optionID = i + 1;  // optionID is 1-index to be compatible
            Button button = new Button(taskActivity);
            button.setText(option);
            button.setLayoutParams(buttonLayoutParams);
            button.setOnClickListener(taskActivity.getOnClickEventListenerForResponse(
                    getNotificationID(), option, optionID));
            button.setGravity(Gravity.CENTER);
            layout.addView(button);
        }

        return layout;
    }
}
