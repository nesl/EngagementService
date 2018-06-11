package ucla.nesl.notificationpreference.activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import ucla.nesl.notificationpreference.R;
import ucla.nesl.notificationpreference.activity.history.ResponseHistoryActivity;
import ucla.nesl.notificationpreference.service.TaskSchedulingService;
import ucla.nesl.notificationpreference.storage.SharedPreferenceHelper;
import ucla.nesl.notificationpreference.utils.ToastShortcut;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 1;

    // permissions
    private static final String[] requiredPermissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };

    // service
    private TaskSchedulingService taskService;

    // notification related
    private ToastShortcut toastHelper;

    // key-value store
    private SharedPreferenceHelper keyValueStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, requiredPermissions, PERMISSIONS_REQUEST_CODE);

        toastHelper = new ToastShortcut(this);

        keyValueStore = new SharedPreferenceHelper(this);

        // event listeners
        Button buttonTaskList = findViewById(R.id.buttonTaskList);
        buttonTaskList.setOnClickListener(startTaskListEvent);

        Button buttonInputPlaces = findViewById(R.id.buttonInputPlace);
        buttonInputPlaces.setOnClickListener(enterPlacesEvent);

        Button buttonDataCollectionStatus = findViewById(R.id.buttonSensingSwitch);
        buttonDataCollectionStatus.setOnClickListener(toggleDataCollectionStatusEvent);

        // --**** FOR DEBUG PURPOSE ****--
        keyValueStore.setAppStatus(SharedPreferenceHelper.APP_STATUS_ACTIVE);

        // start service
        Intent serviceIntent = new Intent(this, TaskSchedulingService.class);
        startService(serviceIntent);



    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent serviceIntent = new Intent(this, TaskSchedulingService.class);
        bindService(serviceIntent, taskServiceConnection, Context.BIND_AUTO_CREATE);
        Log.i("MainActivity", "onStart()");

        refreshDataCollectionButtonText();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(taskServiceConnection);
        Log.i("MainActivity", "onStop()");
    }

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


    View.OnClickListener startTaskListEvent = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(), ResponseHistoryActivity.class);
            startActivity(intent);
        }
    };

    View.OnClickListener enterPlacesEvent = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(ConfigurePlaceActivity.getIntentForStartActivity(
                    MainActivity.this, ConfigurePlaceActivity.MODE_INITIALIZE));
        }
    };

    View.OnClickListener toggleDataCollectionStatusEvent = new View.OnClickListener() {
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
}
