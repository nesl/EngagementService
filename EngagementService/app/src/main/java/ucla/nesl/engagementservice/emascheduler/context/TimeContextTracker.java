package ucla.nesl.engagementservice.emascheduler.context;

import java.util.Calendar;

/**
 * Created by timestring on 4/25/17.
 *
 * Time context tracker.
 */
public class TimeContextTracker implements IContextTracker {
    @Override
    public void requestContextUpdate() {
        Calendar now = Calendar.getInstance();
        //TODO: notify recipient now.get(Calendar.HOUR_OF_DAY);
    }
}
