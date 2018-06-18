package ucla.nesl.notificationpreference.sensing;

import android.content.Context;
import android.os.PowerManager;

import ucla.nesl.notificationpreference.storage.loggers.ScreenStatusLogger;

/**
 * Created by timestring on 6/18/18.
 *
 * Get screen on/off status.
 */

public class ScreenStatusDataCollector {

    private PowerManager powermanager;

    private ScreenStatusLogger logger;

    public ScreenStatusDataCollector(Context context) {
        powermanager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        logger = ScreenStatusLogger.getInstance();
    }

    public String query() {
        String result = powermanager.isInteractive() ? "on" : "off";
        logger.log(result);
        return result;
    }

}
