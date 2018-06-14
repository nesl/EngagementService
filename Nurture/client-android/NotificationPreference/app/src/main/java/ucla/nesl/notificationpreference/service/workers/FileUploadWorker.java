package ucla.nesl.notificationpreference.service.workers;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.util.concurrent.TimeUnit;

import ucla.nesl.notificationpreference.alarm.AlarmWorker;
import ucla.nesl.notificationpreference.alarm.NextTrigger;
import ucla.nesl.notificationpreference.network.HttpsPostRequest;
import ucla.nesl.notificationpreference.storage.loggers.ILogger;

/**
 * Created by timestring on 6/13/18.
 *
 * `FileUploadWorker` handles uploading one log file. The policy is that when the WiFi is available,
 * it tries to upload every 20 hours.
 */

public class FileUploadWorker extends AlarmWorker {

    private static final String TAG = FileUploadWorker.class.getSimpleName();


    private File file;
    private ConnectivityManager connectivityManager;
    private String logType;
    private long uploadDeadline;

    public FileUploadWorker(ConnectivityManager _connectivityManager, String type, ILogger logger) {
        connectivityManager = _connectivityManager;
        file = logger.getFile();
        logType = type;
        uploadDeadline = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1);
    }

    @NonNull
    @Override
    protected NextTrigger onPlan() {
        try {
            if (isOnWifi()) {
                new HttpsPostRequest()
                        .setDestinationPage("mobile/upload-log-file")
                        .setParam("code", "49940")  //TODO
                        .setParam("type", logType)
                        .setParamWithFile("content", file)
                        .setCallback(fileUploadedCallback)
                        .execute();
            }
        } catch (Exception e) {
            Log.e(TAG, "Got exception", e);
        }

        long waitTime = Math.max(
                uploadDeadline - System.currentTimeMillis(),
                TimeUnit.MINUTES.toMillis(30)
        );

        return new NextTrigger(waitTime, TimeUnit.MINUTES.toMillis(15));
    }

    private boolean isOnWifi() {
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        if (activeNetwork == null) {
            return false;
        }
        if (!activeNetwork.isConnectedOrConnecting()) {
            return false;
        }

        return activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
    }

    private HttpsPostRequest.Callback fileUploadedCallback = new HttpsPostRequest.Callback() {
        @Override
        public void onResult(String result) {
            Log.i("FileUploadWorker", "get result: " + result);
            if (result != null && result.equals("Ok")) {
                uploadDeadline = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(20);
            }
        }
    };
}
