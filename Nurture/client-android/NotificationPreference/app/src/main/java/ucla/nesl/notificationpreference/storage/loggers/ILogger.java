package ucla.nesl.notificationpreference.storage.loggers;

import java.io.File;

/**
 * Created by timestring on 6/13/18.
 *
 * Interface of loggers. Need to return the log file path.
 */

public interface ILogger {
    File getFile();
}
