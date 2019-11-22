package com.example.smartoffice.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.smartoffice.MainActivity;
import com.example.smartoffice.R;
import com.example.smartoffice.media.Activity_Camera;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService
{
    private String TAG = "MyFirebaseMessagingService";

    public MyFirebaseMessagingService()
    {
    }

    @Override
    public void onNewToken(String token)
    {
        Log.e(TAG, "Refreshed token: " + token);
        //생성된 앱 토큰을 서버로 전송
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token)
    {
        //서버로 전송할 코드를 작성
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage)
    {

//        Log.e(TAG, "From: " + remoteMessage.getFrom() + "1");
//        Log.e(TAG, "onMessageReceived: " + remoteMessage);

        if (remoteMessage.getData().size() > 0)
        {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());


            if (true)
            {

            } else
            {
                handleNow();
            }

        }
        if (remoteMessage.getNotification() != null)
        {
//            sendNotification(remoteMessage.getNotification().getBody());
            Log.e(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            Log.e(TAG, "Message Notification Title: " + remoteMessage.getNotification().getTitle());
            Log.e(TAG, "Message Notification Data: " + remoteMessage.getData());

            Intent intent = null;
            if(remoteMessage.getNotification().getTitle().equals("초인종") || remoteMessage.getData().equals("bell"))
            {
                //노티피케이션 클릭시 이동할 액티비티
                intent = new Intent(this, Activity_Camera.class);
            }
            else
            {
                //노티피케이션 클릭시 이동할 액티비티
                intent = new Intent(this, MainActivity.class);
            }

            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            String channelId = "Channel ID";
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, channelId)
                            .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                            .setContentTitle(remoteMessage.getNotification().getTitle())
                            .setContentText(remoteMessage.getNotification().getBody())
                            .setAutoCancel(true)
                            .setSound(defaultSoundUri)
                            .setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                // Create channel to show notifications.
                String channelName = "Channel ID";
                NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(channel);
            }

            notificationManager.notify(0, notificationBuilder.build());
        }
    }

    private void handleNow()
    {
        Log.d(TAG, "Short lived task is done.");
    }
}


//    private void sendNotification(String messageBody)
//    {
//        String nickname[] = messageBody.split("님이 회원님");
//
////        Log.e(TAG, "sendNotification: messageBody:  " + messageBody );
//
//        //노티피케이션 클릭시 이동할 액티비티
//        Intent intent = new Intent(this, MainActivity.class);
////        intent.putExtra("boardno", boardno);
////        intent.putExtra("replyno", replyno);
////        intent.putExtra("rereplyno", rereplyno);
//
//
////        intent.putExtra("notification","notification_ReReply");
////        intent.putExtra("nickname",nickname[0]);
//        intent.setAction(Intent.ACTION_MAIN);
//        intent.addCategory(Intent.CATEGORY_LAUNCHER);
//
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
//                PendingIntent.FLAG_ONE_SHOT);
//
//        String channelId = "Channel ID";
////        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
////        NotificationChannel notificationChannel = new NotificationChannel("channel_id", "channel_name",NotificationManager.IMPORTANCE_DEFAULT);
//        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        NotificationCompat.Builder notificationBuilder =
//                new NotificationCompat.Builder(this, channelId)
//                        .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
//                        .setContentTitle("CCTV")
//                        .setContentText(messageBody)
//                        .setAutoCancel(true)
//                        .setSound(defaultSoundUri)
//                        .setContentIntent(pendingIntent);
//
//        NotificationManager notificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//        // Since android Oreo notification channel is needed.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
//        {
//            // Create channel to show notifications.
//            String channelName = "Channel ID";
//            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
//            notificationManager.createNotificationChannel(channel);
//        }
//
//        notificationManager.notify(0, notificationBuilder.build());
//    }
