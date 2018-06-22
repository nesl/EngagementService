package ucla.nesl.notificationpreference.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import ucla.nesl.notificationpreference.R;
import ucla.nesl.notificationpreference.ui.LayoutConfigurePlaceSuite;

public class ConfigurePlaceActivity extends AppCompatActivity {

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
            //TODO: broadcast the result
            finish();
        }
    };

}
