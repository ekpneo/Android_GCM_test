package com.test.gcm_test;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GCMIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;

    public GCMIntentService() {
        super("GCMIntentService");
    }
    @Override
    protected void onHandleIntent(Intent intent)
    {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty())
        { // has effect of unparcelling Bundle
      /*
       * Filter messages based on message type. Since it is likely that GCM will
       * be extended in the future with new message types, just ignore any
       * message types you're not interested in, or that you don't recognize.
       */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType))
            {
                sendNotification("1", "Send Error", extras.toString());
            }
            else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType))
            {
                sendNotification("1", "Messages Deleted", extras.toString());
                // If it's a regular GCM message, do some work.
            }
            else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType))
            {
	            Log.i("GCMIntentService.java | onHandleIntent", "Received: " + extras.toString());
	            String type = intent.getStringExtra("type");
	            if (type != null && type.equals("cancel")) {
		            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		            mNotificationManager.cancel(NOTIFICATION_ID);
	            } else {
		            String icon = intent.getStringExtra("icon");

		            String title = "Message";
		            if (intent.hasExtra("title")) {
			            intent.getStringExtra("title");
		            }

		            String msg = intent.getStringExtra("msg");
		            sendNotification(icon, title, msg);
	            }
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GCMBroadcastReceiver.completeWakefulIntent(intent);
    }


    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String icon, String title, String msg)
    {
	    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(getApplicationContext(), EmptyActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("msg", msg);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

	    int iicon = this.getResources().getIdentifier("ic_stat_" + icon, "drawable", this.getPackageName());
	    if (iicon == 0) {
		    iicon = R.drawable.ic_stat_chat;
	    }

        NotificationCompat.Builder mBuilder =
	        new NotificationCompat.Builder(this)
		        .setDefaults(NotificationCompat.DEFAULT_ALL)
		        .setSmallIcon(iicon)
		        .setTicker(msg)
		        .setContentTitle(title).setStyle(
                    new NotificationCompat
	                    .BigTextStyle()
	                    .bigText(msg))
	            .setContentText(msg)
	            .setAutoCancel(true);


        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
