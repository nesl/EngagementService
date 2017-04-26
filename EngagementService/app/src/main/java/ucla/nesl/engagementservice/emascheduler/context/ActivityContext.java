package ucla.nesl.engagementservice.emascheduler.context;

import com.google.android.gms.location.DetectedActivity;

/**
 * Created by timestring on 4/24/17.
 *
 * Activity such as walking, running, standing, etc.
 */
public class ActivityContext {
    private DetectedActivity activity;

    public DetectedActivity getActivity() {
        return activity;
    }
}
