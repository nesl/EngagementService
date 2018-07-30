package ucla.nesl.notificationpreference.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import ucla.nesl.notificationpreference.R;
import ucla.nesl.notificationpreference.network.HttpsPostRequest;
import ucla.nesl.notificationpreference.storage.SharedPreferenceHelper;
import ucla.nesl.notificationpreference.storage.database.NotificationResponseRecordDatabase;
import ucla.nesl.notificationpreference.storage.loggers.LocationLogger;
import ucla.nesl.notificationpreference.storage.loggers.MotionActivityLogger;
import ucla.nesl.notificationpreference.storage.loggers.NotificationInteractionEventLogger;
import ucla.nesl.notificationpreference.storage.loggers.RingerModeLogger;
import ucla.nesl.notificationpreference.storage.loggers.ScreenStatusLogger;
import ucla.nesl.notificationpreference.utils.ToastShortcut;

public class DebugActivity extends AppCompatActivity {

    private static final String TAG = DebugActivity.class.getSimpleName();

    private ToastShortcut toastHelper;
    private SharedPreferenceHelper keyValueStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        Button uploadLogButton = findViewById(R.id.buttonImmediateUpload);
        uploadLogButton.setOnClickListener(uploadEvent);

        toastHelper = new ToastShortcut(this);
        keyValueStore = new SharedPreferenceHelper(this);
    }

    private View.OnClickListener uploadEvent = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String userCode = keyValueStore.getUserCode();

            // notification interaction
            try {
                new HttpsPostRequest()
                        .setDestinationPage("mobile/upload-log-file")
                        .setParam("code", userCode)
                        .setParam("type", "notification-interaction")
                        .setParamWithFile("content",
                                NotificationInteractionEventLogger.getInstance().getFile())
                        .execute();
            } catch (Exception e) {
                Log.e(TAG, "Got exception", e);
            }

            // motion
            try {
                new HttpsPostRequest()
                        .setDestinationPage("mobile/upload-log-file")
                        .setParam("code", userCode)
                        .setParam("type", "motion")
                        .setParamWithFile("content",
                                MotionActivityLogger.getInstance().getFile())
                        .execute();
            } catch (Exception e) {
                Log.e(TAG, "Got exception", e);
            }

            // location
            try {
                new HttpsPostRequest()
                        .setDestinationPage("mobile/upload-log-file")
                        .setParam("code", userCode)
                        .setParam("type", "location")
                        .setParamWithFile("content",
                                LocationLogger.getInstance().getFile())
                        .execute();
            } catch (Exception e) {
                Log.e(TAG, "Got exception", e);
            }

            // ringer mode
            try {
                new HttpsPostRequest()
                        .setDestinationPage("mobile/upload-log-file")
                        .setParam("code", userCode)
                        .setParam("type", "ringer-mode")
                        .setParamWithFile("content",
                                RingerModeLogger.getInstance().getFile())
                        .execute();
            } catch (Exception e) {
                Log.e(TAG, "Got exception", e);
            }

            // screen status
            try {
                new HttpsPostRequest()
                        .setDestinationPage("mobile/upload-log-file")
                        .setParam("code", userCode)
                        .setParam("type", "screen-status")
                        .setParamWithFile("content",
                                ScreenStatusLogger.getInstance().getFile())
                        .execute();
            } catch (Exception e) {
                Log.e(TAG, "Got exception", e);
            }

            // database - task response
            NotificationResponseRecordDatabase database =
                    NotificationResponseRecordDatabase.getAppDatabase(DebugActivity.this);
            database.dump(NotificationResponseRecordDatabase.DEFAULT_FILE);
            try {
                new HttpsPostRequest()
                        .setDestinationPage("mobile/upload-log-file")
                        .setParam("code", userCode)
                        .setParam("type", "task-response")
                        .setParamWithFile("content",
                                NotificationResponseRecordDatabase.DEFAULT_FILE)
                        .execute();
            } catch (Exception e) {
                Log.e(TAG, "Got exception", e);
            }
        }
    };
}
