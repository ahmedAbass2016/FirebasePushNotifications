package abass.com.firebasepushnotifications.Request;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;
import abass.com.firebasepushnotifications.R;

/**
 * Created by ahmed on 14-Mar-18.
 */

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    NotificationManager mNotifyMgr;
    public Vector<NotificationManager> mNotificationsMgr = new Vector<>();
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String MessageTitle = remoteMessage.getNotification().getTitle();
        String MessageBody = remoteMessage.getNotification().getBody();

        String dataMessage = remoteMessage.getData().get("message");
        String dataFrom = remoteMessage.getData().get("from_user_id");
        String latitude = remoteMessage.getData().get("latitude");
        String longtitude = remoteMessage.getData().get("longtitude");
        String Domain = remoteMessage.getData().get("domain");
        String RequestID = remoteMessage.getData().get("request_id");
        String type = remoteMessage.getData().get("type");

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            setupChannels();
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String stringValue = sharedPreferences.getString("the_notification_ringtone",null);
        Uri defaultSoundUri = Uri.parse(stringValue);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, getString(R.string.default_notification_channel_id))
                .setSmallIcon(R.mipmap.lunch)
                .setAutoCancel(true)
                .setContentTitle(MessageTitle)
                .setContentText(MessageBody)
                .setSound(defaultSoundUri)
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        long  mNotification_id =  new Date().getTime();
        Intent intent = new Intent(this, NotificationActivity.class);
        intent.putExtra("message", dataMessage);
        intent.putExtra("from_user_id", dataFrom);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longtitude", longtitude);
        intent.putExtra("domain", Domain);
        intent.putExtra("request_id", RequestID);
        intent.putExtra("type", type);
        intent.setAction("abass.com.firebasepushnotifications"+mNotification_id);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        mBuilder.setContentIntent(pendingIntent);

        mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationsMgr.add(mNotifyMgr);
        mNotificationsMgr.lastElement().notify((int)mNotification_id,mBuilder.build());
        //mNotifyMgr.notify(mNotification_id, mBuilder.build());
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels() {
        CharSequence adminChannelName = getString(R.string.notifications_admin_channel_name);
        String adminChannelDescription = getString(R.string.notifications_admin_channel_description);

        NotificationChannel adminChannel;
        adminChannel = new NotificationChannel(getString(R.string.default_notification_channel_id), adminChannelName, NotificationManager.IMPORTANCE_HIGH);
        adminChannel.setDescription(adminChannelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.RED);
        adminChannel.enableVibration(true);
        if (mNotifyMgr != null) {
            mNotifyMgr.createNotificationChannel(adminChannel);
        }
    }

}
