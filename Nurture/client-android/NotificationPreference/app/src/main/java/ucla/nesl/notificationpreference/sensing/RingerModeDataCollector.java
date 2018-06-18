package ucla.nesl.notificationpreference.sensing;

import android.content.Context;
import android.media.AudioManager;

import ucla.nesl.notificationpreference.storage.loggers.RingerModeLogger;

/**
 * Created by timestring on 6/14/18.
 */

public class RingerModeDataCollector {

    private AudioManager audioManager;

    private RingerModeLogger logger;

    public RingerModeDataCollector(Context context) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        logger = RingerModeLogger.getInstance();
    }

    public String query() {
        String result = getResult();
        logger.log(result);
        return result;
    }

    private String getResult() {
        switch (audioManager.getRingerMode()) {
            case AudioManager.RINGER_MODE_SILENT:
                return "silent";
            case AudioManager.RINGER_MODE_VIBRATE:
                return "vibrate";
            case AudioManager.RINGER_MODE_NORMAL:
                return "normal";
        }
        return "unknown";
    }
}
