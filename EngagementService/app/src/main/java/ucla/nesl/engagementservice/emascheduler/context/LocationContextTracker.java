package ucla.nesl.engagementservice.emascheduler.context;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

/**
 * Created by timestring on 4/25/17.
 *
 * To monitor location.
 */
public class LocationContextTracker implements IContextTracker {
    private static final long LOCATION_REFRESH_TIME_MS = 1000L;
    private static final float LOCATION_REFRESH_DISTANCE_METER = 1L;

    private android.content.Context mAndroidContext;
    private LocationManager mLocationManager;

    public LocationContextTracker(android.content.Context androidContext) {
        mAndroidContext = androidContext;
        mLocationManager =
                (LocationManager) androidContext.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void requestContextUpdate() {
        boolean checkFineLocPermission = (ActivityCompat.checkSelfPermission(mAndroidContext,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        boolean checkCoarseLocPermission = (ActivityCompat.checkSelfPermission(mAndroidContext,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        if (!checkFineLocPermission && !checkCoarseLocPermission) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                LOCATION_REFRESH_TIME_MS, LOCATION_REFRESH_DISTANCE_METER, mLocationListener);
    }

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            //TODO: notify recipient
            //TODO: upon requestContextUpdate() is called, it should only return one result
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

}
