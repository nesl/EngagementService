package ucla.nesl.notificationpreference.network;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import ucla.nesl.notificationpreference.secret.Secret;
import ucla.nesl.notificationpreference.utils.Base64;
import ucla.nesl.notificationpreference.utils.ReadAll;

/**
 * Created by timestring on 6/12/18.
 *
 * A HTTPS helper to facilitate communications to the server. To make the communication as
 * lightweight as possible, we disable the process of validating certificates. We take the following
 * StackOverflow as the reference: https://stackoverflow.com/a/5297100/4713342
 */

public class HttpsPostRequest extends AsyncTask<String, Void, String> {

    private static final String TAG = HttpsPostRequest.class.getSimpleName();

    private static final long DEFAULT_LONG_TIMEOUT = TimeUnit.MINUTES.toMillis(1);
    private static final long DEFAULT_SHORT_TIMEOUT = TimeUnit.SECONDS.toMillis(10);

    private static boolean httpsIsInitialized = false;


    private boolean hasBeenExecuted = false;

    private long timeout;
    private Map<String, Object> params = new LinkedHashMap<>();
    private String destinationPage = null;
    private Callback callback = null;

    private boolean hasExplicitlySetTimeout = false;
    private boolean hasFileAttached = false;


    public HttpsPostRequest setDestinationPage(String page) {
        destinationPage = page;
        return this;
    }

    public HttpsPostRequest setCallback(Callback _callback) {
        callback = _callback;
        return this;
    }

    public HttpsPostRequest setParam(String key, String value) {
        params.put(key, value);
        return this;
    }

    public HttpsPostRequest setParamWithFile(String key, File file) throws IOException {
        hasFileAttached = true;
        return setParam(key, Base64.encodeToString(ReadAll.from(file).getBytes(), Base64.DEFAULT));
    }

    public HttpsPostRequest setTimeout(long timeSpanMSec) throws IOException {
        timeout = timeSpanMSec;
        hasExplicitlySetTimeout = true;
        return this;
    }


    protected String doInBackground(String... urls) {
        //setByPassSSLCertificate();

        checkStatusOrThrowException();

        try {
            URL url = new URL(Secret.getServerURLWithPage(destinationPage));

            if (!hasExplicitlySetTimeout) {
                timeout = (hasFileAttached ? DEFAULT_LONG_TIMEOUT : DEFAULT_SHORT_TIMEOUT);
            }

            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, Object> param : params.entrySet()) {
                if (postData.length() != 0)
                    postData.append('&');
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");

            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            conn.setDoOutput(true);
            conn.getOutputStream().write(postDataBytes);
            conn.setConnectTimeout((int) timeout);

            int responseCode = conn.getResponseCode();
            Log.i(TAG, "response code = " + responseCode + "(" + destinationPage + ")");

            return ReadAll.from(conn.getInputStream());

        } catch (Exception e) {
            Log.e(TAG, "got exception", e);
            return null;
        }
    }

    protected void onPostExecute(String status) {
        if (callback != null) {
            callback.onResult(status);
        }
    }


    public interface Callback {
        void onResult(String result);
    }

    private void checkStatusOrThrowException() {
        if (hasBeenExecuted) {
            throw new IllegalStateException("The task has been executed");
        }
        hasBeenExecuted = true;

        if (destinationPage == null) {
            throw new IllegalArgumentException("destination page is not set");
        }
    }

    /*
    private static void setByPassSSLCertificate() {
        if (httpsIsInitialized) {
            return;
        }

        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(
                    context.getSocketFactory());

            httpsIsInitialized = true;

        } catch (Exception e) {
            Log.e(TAG, "Got exception", e);
        }
    }
    */
}