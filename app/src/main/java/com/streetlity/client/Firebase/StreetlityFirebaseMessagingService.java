package com.streetlity.client.Firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.streetlity.client.MyApplication;
import com.streetlity.client.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class StreetlityFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "[Firebase]";
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        ((MyApplication) getApplication()).setDeviceToken(token);
        getSharedPreferences("firebase", MODE_PRIVATE).edit().putString("fb", token).apply();
        Log.println(Log.INFO,TAG,"Token: " + token );
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Log.d("msg", "onMessageReceived: " + remoteMessage.getData().get("message"));
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        String intentName = notification.getClickAction();
        //Log.e(TAG, "onMessageReceived: " + notification.getClickAction());
        Intent intent = new Intent(intentName);
        Map<String, String> data = remoteMessage.getData();
        //Log.e(TAG, "onMessageReceived: " + data);
        intent.putExtra("id", data.get("id"));
        intent.putExtra("common_user", data.get("common_user"));
        intent.putExtra("reason", data.get("reason"));
        intent.putExtra("note", data.get("note"));
        intent.putExtra("maintenance_user", data.get("maintenance_user"));
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(intent);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        for (String keys : data.keySet())
        {
            System.out.println(keys + ":"+ data.get(keys));
        }

        if(intentName.equals("MaintenanceAcceptNotification")){
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            return;
        }

            String channelId = "Default";
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(remoteMessage.getNotification().getTitle())
                    .setContentText(remoteMessage.getNotification().getBody()).setAutoCancel(true).setContentIntent(pendingIntent)
                    .setPriority(NotificationManager.IMPORTANCE_HIGH)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    //.setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                    .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setSound(alarmSound);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId, "Default channel", NotificationManager.IMPORTANCE_HIGH);
                manager.createNotificationChannel(channel);
            }
            manager.notify(0, builder.build());
//        try {
//            Map<String, String> data = remoteMessage.getData();
//            //Log.e(TAG, "onMessageReceived: " + data);
//            NormalOrderObject object = new NormalOrderObject(Integer.parseInt(remoteMessage.getData().get("id")), remoteMessage.getData().get("user"),
//                    remoteMessage.getData().get("reason"));
//            object.setNote(remoteMessage.getData().get("note"));
//            getSharedPreferences("orderInfo", MODE_PRIVATE).edit().putString("orderInfo", new Gson().toJson(object)).apply();
//        }catch (Exception e){
//            e.printStackTrace();
//        }
    }


    public static String getToken(Context context) {
        return context.getSharedPreferences("firebase", MODE_PRIVATE).getString("fb", "empty");
    }
}
