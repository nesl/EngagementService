package ucla.nesl.notificationpreference.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewAnimator;

import ucla.nesl.notificationpreference.R;
import ucla.nesl.notificationpreference.network.HttpsPostRequest;
import ucla.nesl.notificationpreference.storage.SharedPreferenceHelper;
import ucla.nesl.notificationpreference.ui.LayoutConfigurePlaceSuite;
import ucla.nesl.notificationpreference.utils.Utils;

public class OpeningActivity extends AppCompatActivity {

    // permissions
    private static final String[] requiredPermissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };

    private static final int REQUEST_CODE_PERMISSIONS = 1;

    private SharedPreferenceHelper keyValueStore;

    private ViewAnimator layoutContainer;

    private LayoutConfigurePlaceSuite configurePlaceLayoutSuite;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opening);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        keyValueStore = new SharedPreferenceHelper(this);

        // for the master container
        layoutContainer = findViewById(R.id.container);

        Animation in = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);

        layoutContainer.setInAnimation(in);
        layoutContainer.setOutAnimation(out);

        // for welcome view
        Button startSetupButton = findViewById(R.id.buttonStartSetup);
        startSetupButton.setOnClickListener(startSetupOnClickListener);

        // for permission request view
        Button getPermissionButton = findViewById(R.id.buttonGetPermission);
        getPermissionButton.setOnClickListener(getPermissionOnClickListener);

        // for place configuration view
        configurePlaceLayoutSuite = new LayoutConfigurePlaceSuite(this,
                LayoutConfigurePlaceSuite.MODE_INITIALIZE, confirmPlaceButtonOnClickListener);

        // for user code view
        Button confirmUserCodeButton = findViewById(R.id.buttonConfirmUserCode);
        confirmUserCodeButton.setOnClickListener(confirmUserCodeOnClickListener);

        // for log file status view
        Button backupLogFilesButton = findViewById(R.id.buttonResetLogFiles);
        backupLogFilesButton.setOnClickListener(backupLogFilesOnClickListener);

        TextView keepLogFilesTextButton = findViewById(R.id.textButtonKeepLogFiles);
        keepLogFilesTextButton.setOnClickListener(keepLogFilesOnClickListener);

        // for all set view
        Button allSetButton = findViewById(R.id.buttonFinishSetup);
        allSetButton.setOnClickListener(allSetOnClickListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        configurePlaceLayoutSuite.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        configurePlaceLayoutSuite.onActivityResult(requestCode, resultCode, data);
    }

    //region Section: Welcome view - UI events
    // =============================================================================================
    private final View.OnClickListener startSetupOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            enterWelcomeView();
        }
    };
    //endregion

    //region Section: Permission request view - UI events and permission stuff
    // =============================================================================================
    private void enterWelcomeView() {
        layoutContainer.showNext();

        // fast-forward if permission has already been set
        if (hasAllPermissions()) {
            enterPlaceConfigurationView();
        }
    }

    private final View.OnClickListener getPermissionOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ActivityCompat.requestPermissions(
                    OpeningActivity.this, requiredPermissions, REQUEST_CODE_PERMISSIONS);
        }
    };

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSIONS:
                if (hasAllPermissions()) {
                    layoutContainer.showNext();
                }
                break;
        }
    }

    public boolean hasAllPermissions() {
        for (String permission : requiredPermissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    //endregion

    //region Section: Place configuration view - UI events
    // =============================================================================================
    private void enterPlaceConfigurationView() {
        layoutContainer.showNext();
    }

    private View.OnClickListener confirmPlaceButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            enterUserCodeView();

        }
    };
    //endregion

    //region Section: User code view - UI events
    // =============================================================================================
    private void enterUserCodeView() {
        layoutContainer.showNext();

        if (keyValueStore.getUserCode() != null) {
            enterLogFileStatusView();
        }

        requestUserCodeFromServer();
    }

    private void requestUserCodeFromServer() {
        new HttpsPostRequest()
                .setDestinationPage("mobile/get-user-code")
                .setCallback(getUserCodeCallback)
                .execute();
    }

    private HttpsPostRequest.Callback getUserCodeCallback = new HttpsPostRequest.Callback() {
        @Override
        public void onResult(String result) {
            if (!Utils.tryUpdateUserCode(result, keyValueStore)) {
                requestUserCodeFromServer();
                return;
            }

            TextView userCodeText = OpeningActivity.this.findViewById(R.id.textUserCode);
            userCodeText.setText(result);

            TextView statusText = OpeningActivity.this.findViewById(R.id.textUserCodeStatus);
            statusText.setVisibility(View.GONE);

            Button button = OpeningActivity.this.findViewById(R.id.buttonConfirmUserCode);
            button.setVisibility(View.VISIBLE);
        }
    };

    private View.OnClickListener confirmUserCodeOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            enterLogFileStatusView();
        }
    };
    //endregion

    //region Section: User code view - UI events
    // =============================================================================================
    private void enterLogFileStatusView() {
        layoutContainer.showNext();

        if (!Utils.hasAnyStaleLogFile()) {
            enterAllSetView();
        }
    }

    private View.OnClickListener backupLogFilesOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Utils.backupAllStaleLogFiles();
            enterAllSetView();
        }
    };

    private View.OnClickListener keepLogFilesOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            enterAllSetView();
        }
    };
    //endregion

    //region Section: All set view - UI events
    // =============================================================================================
    private void enterAllSetView() {
        layoutContainer.showNext();
    }

    private View.OnClickListener allSetOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            setResult(Activity.RESULT_OK);
            finish();
        }
    };
    //endregion
}
