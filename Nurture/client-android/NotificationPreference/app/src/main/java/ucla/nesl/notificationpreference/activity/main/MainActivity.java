package ucla.nesl.notificationpreference.activity.main;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import ucla.nesl.notificationpreference.R;
import ucla.nesl.notificationpreference.activity.ConfigurePlaceActivity;
import ucla.nesl.notificationpreference.activity.DebugActivity;
import ucla.nesl.notificationpreference.activity.OpeningActivity;
import ucla.nesl.notificationpreference.activity.history.ResponseHistoryActivity;
import ucla.nesl.notificationpreference.service.TaskSchedulingService;
import ucla.nesl.notificationpreference.storage.SharedPreferenceHelper;
import ucla.nesl.notificationpreference.utils.ToastShortcut;
import ucla.nesl.notificationpreference.utils.Utils;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_LAUNCH_OPENING_ACTIVITY = 1;



    // service
    private TaskSchedulingService taskService;
    private boolean isTaskServiceBound = false;

    // notification related
    private ToastShortcut toastHelper;

    // key-value store
    private SharedPreferenceHelper keyValueStore;


    //region Section: Activity life cycle
    // =============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        toastHelper = new ToastShortcut(this);

        keyValueStore = new SharedPreferenceHelper(this);

        // --**** FOR DEBUG PURPOSE ****--
        keyValueStore.setAppStatus(SharedPreferenceHelper.APP_STATUS_NOT_INITIALIZED);

        if (keyValueStore.getAppStatus() == SharedPreferenceHelper.APP_STATUS_NOT_INITIALIZED) {
            Intent intent = new Intent(this, OpeningActivity.class);
            startActivityForResult(intent, REQUEST_CODE_LAUNCH_OPENING_ACTIVITY);
        } else {
            startFullOperations();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (keyValueStore.getAppStatus() != SharedPreferenceHelper.APP_STATUS_NOT_INITIALIZED) {
            tryBindTaskService();
        }
        refreshDataCollectionButtonText();
    }

    @Override
    protected void onStop() {
        super.onStop();
        tryUnbindTaskService();
    }
    //endregion

    //region Section: Activity transition - result returned
    // =============================================================================================
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_LAUNCH_OPENING_ACTIVITY:
                if (resultCode == Activity.RESULT_OK) {
                    keyValueStore.setAppStatus(SharedPreferenceHelper.APP_STATUS_ACTIVE);
                    startFullOperations();
                    refreshDataCollectionButtonText();
                    return;
                }
                break;
        }
        finish();
    }
    //endregion

    //region Section: UI Option menu
    // =============================================================================================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_debug: {
                Intent intent = new Intent(getApplicationContext(), DebugActivity.class);
                startActivity(intent);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }
    //endregion

    //region Section: UI event listeners
    // =============================================================================================
    private View.OnClickListener startTaskListEvent = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(), ResponseHistoryActivity.class);
            startActivity(intent);
        }
    };

    private View.OnClickListener enterPlacesEvent = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, ConfigurePlaceActivity.class);
            startActivity(intent);
        }
    };

    private View.OnClickListener toggleDataCollectionStatusEvent = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (taskService == null) {
                toastHelper.showShort(
                        "Something goes wrong. Please kill the app and restart again.");
                return;
            }

            taskService.toggleOperationStatus();
            refreshDataCollectionButtonText();
        }
    };

    private View.OnLongClickListener userCodeLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            ChangeUserCodeDialogHelper.createAndShowDialog(MainActivity.this);
            return true;
        }
    };
    //endregion

    //region Section: Service connection
    // =============================================================================================
    private ServiceConnection taskServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            taskService = null;
            Log.i("MainActivity", "shouldn't be disconnected");
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TaskSchedulingService.LocalBinder myBinder =
                    (TaskSchedulingService.LocalBinder) service;
            taskService = myBinder.getService();

            Log.i("MainActivity", "get timestamp " + taskService.getCreatedTimestamp());
        }
    };

    private void tryBindTaskService() {
        if (!isTaskServiceBound) {
            Intent serviceIntent = new Intent(this, TaskSchedulingService.class);
            bindService(serviceIntent, taskServiceConnection, Context.BIND_AUTO_CREATE);
            isTaskServiceBound = true;
        }
    }

    private void tryUnbindTaskService() {
        if (isTaskServiceBound) {
            unbindService(taskServiceConnection);
            isTaskServiceBound = false;
        }
    }
    //endregion

    //region Section: UI refresh helper
    // =============================================================================================
    private void refreshDataCollectionButtonText() {
        Button button = findViewById(R.id.buttonSensingSwitch);
        switch (keyValueStore.getAppStatus()) {
            case SharedPreferenceHelper.APP_STATUS_ACTIVE:
                button.setText("Stop Data Collection");
                break;
            case SharedPreferenceHelper.APP_STATUS_INACTIVE:
                button.setText("Start Data Collection");
                break;
            default:
                button.setText("O____o");
        }
    }
    //endregion

    //region Section: User code updating helpers
    // =============================================================================================
    void tryUpdateUserCode(String code) {
        if (Utils.tryUpdateUserCode(code, keyValueStore)) {
            TextView textCode = MainActivity.this.findViewById(R.id.textUserCode);
            textCode.setText(code);
        } else {
            toastHelper.showLong("The user code is invalid");
        }
    }
    //endregion

    //region Section: Main operation entrance
    // =============================================================================================
    private void startFullOperations() {
        // event listeners
        Button buttonTaskList = findViewById(R.id.buttonTaskList);
        buttonTaskList.setOnClickListener(startTaskListEvent);

        Button buttonInputPlaces = findViewById(R.id.buttonInputPlace);
        buttonInputPlaces.setOnClickListener(enterPlacesEvent);

        Button buttonDataCollectionStatus = findViewById(R.id.buttonSensingSwitch);
        buttonDataCollectionStatus.setOnClickListener(toggleDataCollectionStatusEvent);

        // start service
        Intent serviceIntent = new Intent(this, TaskSchedulingService.class);
        startService(serviceIntent);

        // get user code if not set yet
        TextView textCode = MainActivity.this.findViewById(R.id.textUserCode);
        textCode.setOnLongClickListener(userCodeLongClickListener);
        textCode.setText(keyValueStore.getUserCode());
    }
    //endregion
}
