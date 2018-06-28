package ucla.nesl.notificationpreference.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import ucla.nesl.notificationpreference.R;
import ucla.nesl.notificationpreference.ui.LayoutConfigurePlaceSuite;

public class ConfigurePlaceActivity extends AppCompatActivity {

    public static final String INTENT_PLACE_CHANGED_SIGNAL = "intent.place.changed.signal";

    private LayoutConfigurePlaceSuite configurePlaceLayoutSuite;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_place);

        configurePlaceLayoutSuite = new LayoutConfigurePlaceSuite(
                this, LayoutConfigurePlaceSuite.MODE_UPDATE, confirmButtonOnClickListener);
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

    private View.OnClickListener confirmButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(INTENT_PLACE_CHANGED_SIGNAL);
            LocalBroadcastManager.getInstance(ConfigurePlaceActivity.this).sendBroadcast(intent);
            finish();
        }
    };

}
