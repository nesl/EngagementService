package ucla.nesl.notificationpreference.service.workers;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Locale;

import ucla.nesl.notificationpreference.alarm.AlarmWorker;
import ucla.nesl.notificationpreference.alarm.NextTrigger;

/**
 * Created by timestring on 5/23/18.
 *
 * `TestAlarmWorker` provides a very simple implementation demonstrating the use case of
 * `AlarmWorker`. This class periodically print messages through LogCat as well as append the
 * specified message to a predefined file. I use this to test two things, which I've verified
 * both work:
 *
 *   1) Whether `AlarmManager` can schedule things as expected in `AlarmEventManager`, and
 *   2) If the events are scheduled if the screen goes off.
 */

public class TestAlarmWorker extends AlarmWorker {

    private static final String TAG = TestAlarmWorker.class.getSimpleName();

    private static final File LOG_FILE = new File(Environment.getExternalStorageDirectory(), "nurture.debug.txt");

    private String text;
    private long timeIntervalMs;
    private long toleranceMs;
    private int count = 0;

    public TestAlarmWorker(String _text, long _timeIntervalMs, long _toleranceMs) {
        text = _text;
        timeIntervalMs = _timeIntervalMs;
        toleranceMs = _toleranceMs;
    }

    @NonNull
    @Override
    protected NextTrigger onPlan() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

        String message = String.format(Locale.getDefault(), "[%s] %s (%d)",
                sdf.format(System.currentTimeMillis()), text, count);
        Log.i(TAG, "Msg:" + message);
        count++;

        try {
            PrintWriter out = new PrintWriter(new FileOutputStream(LOG_FILE, true));
            out.println(message);
            out.close();
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        return new NextTrigger(timeIntervalMs, toleranceMs);
    }

    @Override
    protected boolean requireBackgroundExecution() {
        return false;
    }
}
