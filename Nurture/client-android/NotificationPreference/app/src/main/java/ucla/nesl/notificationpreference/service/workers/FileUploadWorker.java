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
import ucla.nesl.notificationpreference.storage.SharedPreferenceHelper;

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
    private SharedPreferenceHelper keyValueStore;
    private String logType;
    private long uploadDeadline;

    public FileUploadWorker(
            ConnectivityManager _connectivityManager,
            SharedPreferenceHelper _keyValueStore,
            String type,
            File _file
    ) {
        connectivityManager = _connectivityManager;
        keyValueStore = _keyValueStore;
        logType = type;
        file = _file;
        uploadDeadline = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1);
    }

    @NonNull
    @Override
    protected NextTrigger onPlan() {
        long now = System.currentTimeMillis();

        if (now > uploadDeadline && isOnWifi()) {
            tryUploadFile();
        }

        long waitTime = Math.max(uploadDeadline - now, TimeUnit.MINUTES.toMillis(30));

        return new NextTrigger(waitTime, TimeUnit.MINUTES.toMillis(15));
    }

    @Override
    protected boolean requireBackgroundExecution() {
        return false;
    }

    private void tryUploadFile() {
        try {
            new HttpsPostRequest()
                    .setDestinationPage("mobile/upload-log-file")
                    .setParam("code", keyValueStore.getUserCode())
                    .setParam("type", logType)
                    .setParamWithFile("content", file)
                    .setCallback(fileUploadedCallback)
                    .execute();
        } catch (Exception e) {
            Log.e(TAG, "Got exception", e);
        }
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
            Log.i(TAG, "get result: " + result);
            if (result != null && result.equals("Ok")) {
                uploadDeadline = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(20);
            }
        }
    };
}
