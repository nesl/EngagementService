package ucla.nesl.notificationpreference.ui;

import android.app.Activity;
import android.content.Intent;
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
import ucla.nesl.notificationpreference.utils.Utils;

/**
 * Created by timestring on 6/20/18.
 */

public class LayoutConfigurePlaceSuite {

    private static final String TAG = LayoutConfigurePlaceSuite.class.getSimpleName();

    public static final int MODE_INITIALIZE = 1;
    public static final int MODE_UPDATE = 2;

    public static final int PLACE_PICKER_HOME_REQUEST_CODE = 1;
    public static final int PLACE_PICKER_WORK_REQUEST_CODE = 2;

    private static final double BOUND_RANGE_LAT = 0.002;

    private SharedPreferenceHelper keyValueStore;

    private Activity activity;


    public LayoutConfigurePlaceSuite(Activity _activity, int mode,
                                     View.OnClickListener confirmButtonOnClickListener) {
        activity = _activity;

        if (!Utils.in(mode, MODE_INITIALIZE, MODE_UPDATE)) {
            throw new IllegalArgumentException(
                    String.format(Locale.getDefault(), "Unrecognized mode (Got %d)", mode));
        }

        keyValueStore = new SharedPreferenceHelper(activity);

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

        TextView openingText = activity.findViewById(R.id.textOpening);
        openingText.setText(openingStatement);

        Button setHomeButton = activity.findViewById(R.id.buttonSetHome);
        setHomeButton.setOnClickListener(setHomePlaceOnClickListener);

        Button setWorkButton = activity.findViewById(R.id.buttonSetWork);
        setWorkButton.setOnClickListener(setWorkPlaceOnClickListener);

        Button confirmButton = activity.findViewById(R.id.buttonConfirmPlaces);
        confirmButton.setText(confirmButtonText);
        confirmButton.setOnClickListener(confirmButtonOnClickListener);
    }

    public void onResume() {
        TextView homePlaceText = activity.findViewById(R.id.textHomeAnswer);
        homePlaceText.setText(
                If.nullThen(keyValueStore.getUserHomePlaceDescription(), "(Not set yet)"));

        TextView workPlaceText = activity.findViewById(R.id.textWorkAnswer);
        workPlaceText.setText(
                If.nullThen(keyValueStore.getUserWorkPlaceDescription(), "(Not set yet)"));
    }
    //endregion

    //region Section: Activity result returned callbacks
    // =============================================================================================
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PLACE_PICKER_HOME_REQUEST_CODE:
                processPlacePickerResultForHome(resultCode, data);
                return true;
            case PLACE_PICKER_WORK_REQUEST_CODE:
                processPlacePickerResultForWork(resultCode, data);
                return true;
        }
        return false;
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
            activity.startActivityForResult(intentBuilder.build(activity), requestCode);
        } catch (Exception e) {
            Log.i(TAG, "Got exception", e);
        }
    }
    //endregion

    //region Section: Helpers for processing the results from PlacePicker
    // =============================================================================================
    private void processPlacePickerResultForHome(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Place place = PlacePicker.getPlace(activity, data);
            keyValueStore.setUserHomePlaceDescription(place.getName().toString());
            LatLng latLng = place.getLatLng();
            keyValueStore.setUserHomeLatitude((float) latLng.latitude);
            keyValueStore.setUserHomeLongitude((float) latLng.longitude);
        }
    }

    private void processPlacePickerResultForWork(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Place place = PlacePicker.getPlace(activity, data);
            keyValueStore.setUserWorkPlaceDescription(place.getName().toString());
            LatLng latLng = place.getLatLng();
            keyValueStore.setUserWorkLatitude((float) latLng.latitude);
            keyValueStore.setUserWorkLongitude((float) latLng.longitude);
        }
    }
    //endregion
}
