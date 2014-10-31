package com.test.gcm_test;


import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class EmptyActivity extends Activity {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static String SENDER_ID = null;

    private GoogleCloudMessaging _gcm;
    private String _regId;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        try {
            SENDER_ID = ""+getApplicationContext().getPackageManager().getApplicationInfo(
                    getApplicationContext().getPackageName(), PackageManager.GET_META_DATA).metaData.getInt("PROJECT_NUMBER");
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
                    _regId = _gcm.register(SENDER_ID);

                    text.post(new Runnable() {
                        @Override
                        public void run() {
                            text.setText("regId:" + _regId);
                        }
                    });

                    msg = "Device registered, registration ID=" + _regId;
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
