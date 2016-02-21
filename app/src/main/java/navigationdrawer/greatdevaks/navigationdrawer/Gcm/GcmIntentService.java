package navigationdrawer.greatdevaks.navigationdrawer.Gcm;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.Random;

import navigationdrawer.greatdevaks.navigationdrawer.MainActivity;
import navigationdrawer.greatdevaks.navigationdrawer.R;

/**
 * Created by amaneureka on 30-Jan-16.
 */
public class GcmIntentService  extends IntentService
{
    public static int notifyID = 9001;
    NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty())
        {
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType))
            {
                if(extras.getString("message") != null) {
                    String _m = extras.getString("message");
                    Log.d("[gcm]", _m);
                    sendNotification(_m);
                }
            }
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(String msg) {
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.putExtra("msg", msg);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder mNotifyBuilder;
        NotificationManager mNotificationManager;

        String[] _msgs = msg.split("=");

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyBuilder = new NotificationCompat.Builder(this)
                    .setContentTitle(_msgs[0])
                    .setContentText(_msgs[1])
                    .setSmallIcon(R.drawable.all_contacts_icon);

        mNotifyBuilder.setContentIntent(resultPendingIntent);

        int defaults = 0;
        defaults = defaults | Notification.DEFAULT_LIGHTS;
        defaults = defaults | Notification.DEFAULT_VIBRATE;
        defaults = defaults | Notification.DEFAULT_SOUND;

        mNotifyBuilder.setAutoCancel(true);
        mNotificationManager.notify(++notifyID, mNotifyBuilder.build());
    }
}
