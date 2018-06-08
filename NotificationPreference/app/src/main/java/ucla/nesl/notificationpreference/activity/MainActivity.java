package ucla.nesl.notificationpreference.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import ucla.nesl.notificationpreference.R;
import ucla.nesl.notificationpreference.activity.history.ResponseHistoryActivity;
import ucla.nesl.notificationpreference.service.TaskSchedulingService;
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

    // notification related
    private ToastShortcut toastHelper;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, requiredPermissions, PERMISSIONS_REQUEST_CODE);

        toastHelper = new ToastShortcut(this);

        // UI
        Button buttonTaskList = findViewById(R.id.buttonTaskList);
        buttonTaskList.setOnClickListener(startTaskListEvent);

        Button buttonInputPlaces = findViewById(R.id.buttonInputPlace);
        buttonInputPlaces.setOnClickListener(enterPlacesEvent);

        Button buttonDataCollectionStatus = findViewById(R.id.buttonSensingSwitch);
        buttonDataCollectionStatus.setOnClickListener(toggleDataCollectionStatusEvent);

        // start service
        Intent serviceIntent = new Intent(this, TaskSchedulingService.class);
        startService(serviceIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();

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
            toastHelper.showShort("Under development. Not supported yet");
        }
    };

}
