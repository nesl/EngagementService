package ucla.nesl.notificationpreference.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import ucla.nesl.notificationpreference.R;
import ucla.nesl.notificationpreference.notification.NotificationHelper;
import ucla.nesl.notificationpreference.storage.NotificationInteractionEventLogger;
import ucla.nesl.notificationpreference.storage.NotificationResponseRecord;
import ucla.nesl.notificationpreference.storage.NotificationResponseRecordDatabase;
import ucla.nesl.notificationpreference.task.ShortQuestionTask;
import ucla.nesl.notificationpreference.task.TaskFactory;

public class TaskActivity extends AppCompatActivity {

    private NotificationResponseRecordDatabase responseDatabase;
    private NotificationInteractionEventLogger interactionLogger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        int notificationID = NotificationHelper.interpretIntentGetNotificationID(getIntent());
        if (notificationID == NotificationHelper.NOTIFICATION_ID_NOT_SET) {
            throw new IllegalArgumentException("Invalid notification ID");
        }

        responseDatabase = NotificationResponseRecordDatabase.getAppDatabase(this);
        interactionLogger = NotificationInteractionEventLogger.getInstance();

        NotificationResponseRecord record = responseDatabase.getRecordByID(notificationID);
        ShortQuestionTask task = TaskFactory.retrieveExistingTask(record);

        // render the question
        TextView primaryQuestionText = findViewById(R.id.textQuestion);
        primaryQuestionText.setText(task.getPrimaryQuestionStatement());

        ViewGroup questionContentLayout = task.getViewLayoutInActivity(this);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        LinearLayout mainLayout = findViewById(R.id.layoutMain);
        mainLayout.addView(questionContentLayout, layoutParams);
    }

    public View.OnClickListener getOnClickEventListenerForResponse(
            final int notificationID, final String responseValue) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                responseDatabase.fillAnswer(notificationID, responseValue);
                interactionLogger.logRespondInApp(notificationID, responseValue);
                finish();
            }
        };
    }
}
