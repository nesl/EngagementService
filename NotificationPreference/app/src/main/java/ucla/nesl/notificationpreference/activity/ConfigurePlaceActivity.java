package ucla.nesl.notificationpreference.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.Locale;

import ucla.nesl.notificationpreference.R;
import ucla.nesl.notificationpreference.storage.SharedPreferenceHelper;
import ucla.nesl.notificationpreference.utils.If;
import ucla.nesl.notificationpreference.utils.In;

public class ConfigurePlaceActivity extends AppCompatActivity {

    private static final String TAG = ConfigurePlaceActivity.class.getSimpleName();

    public static final int MODE_INITIALIZE = 1;
    public static final int MODE_UPDATE = 2;

    private static final String INTENT_EXTRA_NAME_MODE = "mode";

    private static final int PLACE_PICKER_HOME_REQUEST_CODE = 1;
    private static final int PLACE_PICKER_WORK_REQUEST_CODE = 2;

    private static final double BOUND_RANGE_LAT = 0.002;

    private SharedPreferenceHelper keyValueStore;

    private int mode;


    //region Section: Activity cycle callbacks
    // =============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_place);

        mode = getIntent().getIntExtra(INTENT_EXTRA_NAME_MODE, 0);
        if (!In.ints(mode, MODE_INITIALIZE, MODE_UPDATE)) {
            throw new IllegalArgumentException(
                    String.format(Locale.getDefault(), "Unrecognized mode (Got %d)", mode));
        }

        keyValueStore = new SharedPreferenceHelper(this);

        String openingStatement = "";
        String confirmButtonText = "";
        switch (mode) {
            case MODE_INITIALIZE:
                openingStatement = "Now, please set your home and your workplace:";
                confirmButtonText = "Confirm";
                break;
            case MODE_UPDATE:
                openingStatement = "Please enter a new home or workplace:";
                confirmButtonText = "Update";
                break;
        }

        TextView openingText = findViewById(R.id.textOpening);
        openingText.setText(openingStatement);

        Button setHomeButton = findViewById(R.id.buttonSetHome);
        setHomeButton.setOnClickListener(setHomePlaceOnClickListener);

        Button setWorkButton = findViewById(R.id.buttonSetWork);
        setWorkButton.setOnClickListener(setWorkPlaceOnClickListener);

        Button confirmButton = findViewById(R.id.buttonConfirm);
        confirmButton.setText(confirmButtonText);
        confirmButton.setOnClickListener(confirmButtonOnClickListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

        TextView homePlaceText = findViewById(R.id.textHomeAnswer);
        homePlaceText.setText(
                If.nullThen(keyValueStore.getUserHomePlaceDescription(), "(Not set yet)"));

        TextView workPlaceText = findViewById(R.id.textWorkAnswer);
        workPlaceText.setText(
                If.nullThen(keyValueStore.getUserWorkPlaceDescription(), "(Not set yet)"));
    }
    //endregion

    //region Section: Activity result returned callbacks
    // =============================================================================================
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PLACE_PICKER_HOME_REQUEST_CODE:
                processPlacePickerResultForHome(resultCode, data);
                break;
            case PLACE_PICKER_WORK_REQUEST_CODE:
                processPlacePickerResultForWork(resultCode, data);
                break;
        }
    }
    //endregion

    //region Section: Button event listeners
    // =============================================================================================
    private View.OnClickListener setHomePlaceOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            LatLng center = null;
            if (keyValueStore.getUserHomePlaceDescription() != null) {
                center = new LatLng(
                        keyValueStore.getUserHomeLatitude(), keyValueStore.getUserHomeLongitude());
            }
            startPlacePickerActivity(PLACE_PICKER_HOME_REQUEST_CODE, center);
        }
    };

    private View.OnClickListener setWorkPlaceOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            LatLng center = null;
            if (keyValueStore.getUserWorkPlaceDescription() != null) {
                center = new LatLng(
                        keyValueStore.getUserWorkLatitude(), keyValueStore.getUserWorkLongitude());
            }
            startPlacePickerActivity(PLACE_PICKER_WORK_REQUEST_CODE, center);
        }
    };

    private View.OnClickListener confirmButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            finish();
        }
    };
    //endregion

    //region Section: Helpers for initiating PlacePicker activity
    // =============================================================================================
    private void startPlacePickerActivity(int requestCode, LatLng center) {
        PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();

        if (center != null) {
            LatLngBounds bound = LatLngBounds.builder()
                    .include(new LatLng(center.latitude - BOUND_RANGE_LAT, center.longitude))
                    .include(new LatLng(center.latitude + BOUND_RANGE_LAT, center.longitude))
                    .build();
            intentBuilder.setLatLngBounds(bound);
        }

        try {
            startActivityForResult(intentBuilder.build(this), requestCode);
        } catch (Exception e) {
            Log.i(TAG, "Got exception", e);
        }
    }
    //endregion

    //region Section: Helpers for processing the results from PlacePicker
    // =============================================================================================
    private void processPlacePickerResultForHome(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Place place = PlacePicker.getPlace(this, data);
            keyValueStore.setUserHomePlaceDescription(place.getName().toString());
            LatLng latLng = place.getLatLng();
            keyValueStore.setUserHomeLatitude((float) latLng.latitude);
            keyValueStore.setUserHomeLongitude((float) latLng.longitude);
        }
    }

    private void processPlacePickerResultForWork(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Place place = PlacePicker.getPlace(this, data);
            keyValueStore.setUserWorkPlaceDescription(place.getName().toString());
            LatLng latLng = place.getLatLng();
            keyValueStore.setUserWorkLatitude((float) latLng.latitude);
            keyValueStore.setUserWorkLongitude((float) latLng.longitude);
        }
    }
    //endregion

    //region Section: Helpers for starting ConfigurePlaceActivity
    // =============================================================================================
    public static Intent getIntentForStartActivity(Context context, int mode) {
        Intent intent = new Intent(context, ConfigurePlaceActivity.class);
        intent.putExtra(INTENT_EXTRA_NAME_MODE, mode);
        return intent;
    }
    //endregion
}
