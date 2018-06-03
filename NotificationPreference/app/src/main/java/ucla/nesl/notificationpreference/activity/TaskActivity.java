package ucla.nesl.notificationpreference.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import ucla.nesl.notificationpreference.R;
import ucla.nesl.notificationpreference.notification.INotificationEventListener;
import ucla.nesl.notificationpreference.notification.NotificationHelper;
import ucla.nesl.notificationpreference.storage.NotificationInteractionEventLogger;
import ucla.nesl.notificationpreference.storage.NotificationResponseRecord;
import ucla.nesl.notificationpreference.storage.NotificationResponseRecordDatabase;
import ucla.nesl.notificationpreference.task.TaskFactory;
import ucla.nesl.notificationpreference.task.tasks.template.ShortQuestionTask;

public class TaskActivity extends AppCompatActivity implements INotificationEventListener {

    private int notificationID;

    private NotificationResponseRecordDatabase responseDatabase;
    private NotificationInteractionEventLogger interactionLogger;
    private NotificationHelper notificationHelper;

    //region Section: Activity cycle callbacks
    // =============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        notificationID = NotificationHelper.interpretIntentGetNotificationID(getIntent());
        if (notificationID == NotificationHelper.NOTIFICATION_ID_NOT_SET) {
            throw new IllegalArgumentException("Invalid notification ID");
        }

        responseDatabase = NotificationResponseRecordDatabase.getAppDatabase(this);
        interactionLogger = NotificationInteractionEventLogger.getInstance();
        notificationHelper = new NotificationHelper(this, false, this);

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
    //endregion

    //region Section: UI component event callback factory
    // =============================================================================================
    public View.OnClickListener getOnClickEventListenerForResponse(
            final int notificationID, final String responseValue) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logAndTerminate(notificationID, responseValue);
            }
        };
    }

    public TextView.OnEditorActionListener getOnEditorActionListenerForResponse(
            final int notificationID) {
        return new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView tv, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    logAndTerminate(notificationID, tv.getText().toString());
                    handled = true;
                }
                return handled;
            }
        };
    }

    public View.OnClickListener getOnClickEventListenerForSubmittingEditText(
            final int notificationID, final EditText editText) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logAndTerminate(notificationID, editText.getText().toString());
            }
        };
    }
    //endregion

    //region Section: Log and terminate
    // =============================================================================================
    private void logAndTerminate(int notificationID, @NonNull String responseValue) {
        responseDatabase.fillAnswer(notificationID, responseValue);
        interactionLogger.logRespondInApp(notificationID, responseValue);
        notificationHelper.cancelNotification(notificationID);
        finish();
    }
    //endregion

    //region Section: Notification event listener
    // =============================================================================================
    @Override
    public void onNotificationEvent(int targetNotificationID, int eventID) {
        if (targetNotificationID == notificationID
                && eventID == NotificationResponseRecord.STATUS_RESPONDED) {
            finish();
        }
    }
    //endregion
}
