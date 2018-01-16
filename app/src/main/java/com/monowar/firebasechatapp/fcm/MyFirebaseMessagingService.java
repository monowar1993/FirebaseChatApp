package com.monowar.firebasechatapp.fcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.monowar.firebasechatapp.MainActivity;
import com.monowar.firebasechatapp.R;
import com.monowar.firebasechatapp.UserProfileActivity;

import java.util.Map;

/**
 * Created by NgocTri on 8/9/2016.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";

    private PendingIntent pendingIntent;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.w(TAG, "FROM:" + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0) {
            Log.w(TAG, "Message data: " + remoteMessage.getData());
            sendNotification(remoteMessage.getData());
        }
    }

    /**
     * Display the notification
     *
     * @param data
     */
    private void sendNotification(Map<String, String> data) {

        int notificationId = (int) System.currentTimeMillis();

        if (data.get("type").equalsIgnoreCase("request")) {
            Intent intent = new Intent(this, UserProfileActivity.class);
            intent.putExtra("EXTRA_USER_ID", data.get("from_user_id"));
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(UserProfileActivity.class);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(intent);
            pendingIntent = stackBuilder.getPendingIntent(notificationId, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(data.get("title"))
                .setContentText(data.get("body"))
                .setAutoCancel(true)
                .setSound(notificationSound)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, notificationBuilder.build());
    }
}
