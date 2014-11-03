package com.test.gcm_test;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class EmptyActivity extends Activity {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static String SENDER_ID = null;
    private static String URL = null;

    private GoogleCloudMessaging _gcm;
    private String _regId;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        try {
            SENDER_ID = getApplicationContext().getPackageManager().getApplicationInfo(
                    getApplicationContext().getPackageName(), PackageManager.GET_META_DATA).metaData.getString("PROJECT_NUMBER");
            URL = getApplicationContext().getPackageManager().getApplicationInfo(
                    getApplicationContext().getPackageName(), PackageManager.GET_META_DATA).metaData.getString("URL");
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_empty);

        if (checkPlayServices())
        {
            _gcm = GoogleCloudMessaging.getInstance(this);
            _regId = getRegistrationId();



            if (TextUtils.isEmpty(_regId))
                registerInBackground();
        }
        else
        {
            Log.i("EmptyActivity.java | onCreate", "|Google Play ain't no available|");
        }
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);

        String msg = intent.getStringExtra("msg");
        Log.i("EmptyActivity.java | onNewIntent", "|" + msg + "|");
    }

    private boolean checkPlayServices()
    {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS)
        {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
            {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            else
            {
                Log.i("EmptyActivity.java | checkPlayService", "|This device is as old as yo mama so can't do the thing|");
                finish();
            }
            return false;
        }
        return true;
    }

    private String getRegistrationId()
    {
        return _regId;
    }

    private int getAppVersion()
    {
        try
        {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return packageInfo.versionCode;
        }
        catch (NameNotFoundException e)
        {
            // should never happen
            throw new RuntimeException("Could not get the package name: " + e);
        }
    }

    private void registerInBackground()
    {
        final TextView text = (TextView)findViewById(R.id.text);
        new AsyncTask<Void, Void, String>()
        {
            @Override
            protected String doInBackground(Void... params)
            {
                String msg = "";
                try
                {
                    if (_gcm == null)
                    {
                        _gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
//                    _gcm.unregister();
                    _regId = _gcm.register(SENDER_ID);

                    msg = "Device registered, Sender ID=" + SENDER_ID + ", registration ID=" + _regId;

                    text.post(new Runnable() {
                        @Override
                        public void run() {
                            text.setText("senderId:" + SENDER_ID + ",regId:" + _regId);
                        }
                    });

                    try {
                        HttpClient hc = new DefaultHttpClient();
                        HttpGet req = new HttpGet();
                        URI website = new URI(URL + "?reg_id=" + _regId);
                        req.setURI(website);
                        hc.execute(req);
                    } catch (URISyntaxException e) {
                        msg += "(Error:Server URL is wrong)";
                    }

                }
                catch (IOException ex)
                {
                    msg = "Error :" + ex.getMessage();
                }

                return msg;
            }

            @Override
            protected void onPostExecute(String msg)
            {
                Log.i("EmptyActivity.java | onPostExecute", "|" + msg + "|");
            }
        }.execute(null, null, null);
    }
}
