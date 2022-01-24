package com.hms.pms;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

/**
 * Created by hgsky on 2018-07-01.
 */

public class EmsFirebaseMessagingService extends FirebaseMessagingService{

    private static String GROUP_KEY_WORK_PUSH = "";
    private static String CHANNEL_NAME = "EMS PUSH ALARM";
    private static String CHANNEL_ID = "EMS_PUSH_ALARM";
    private static int SUMMARY_ID = 191992317;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> pushDataMap = remoteMessage.getData();
        sendNotification(pushDataMap);
    }

    private void sendNotification(Map<String, String> dataMap) {

        Context context = getApplicationContext();
        GROUP_KEY_WORK_PUSH = context.getPackageName() + ".EMS_PUSH";

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel _channel = manager.getNotificationChannel(CHANNEL_ID);
            if (_channel == null) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[] {0, 2000, 500, 2000});
                channel.enableLights(true);
                manager.createNotificationChannel(channel);
            }
        }

        String audio1 = dataMap.get("audio1");      //폰이 무음일 때 0 : 무음, 1 : 진동, 2 : 벨소리 + 진동
        String audio2 = dataMap.get("audio2");      //폰이 진동일 때 0 : 무음, 1 : 진동, 2 : 벨소리 + 진동
        String audio3 = dataMap.get("audio3");      //폰이 벨소리일 때 0 : 무음, 1 : 진동, 2 : 벨소리 + 진동

        String repeatYN = dataMap.get("repeatYN");  //무한반복 여부  Y : 무한반복, N : 1번만
        String pushOpen = dataMap.get("pushOpen");  //푸시알림을 클릭한 후 처리방법

        if(repeatYN == null || repeatYN.equals("")){
            repeatYN = "Y";
        }

        if(pushOpen == null || pushOpen.equals("")){
            pushOpen = "Y";
        }

        String audioStr = "0";
        //소리, 진동, 무음모드 파악
        AudioManager aManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        switch (aManager.getRingerMode()){
            case AudioManager.RINGER_MODE_NORMAL:           //소리모드
                audioStr = audio3;
                break;
            case AudioManager.RINGER_MODE_VIBRATE:          //진동모드
                if(audio2 != null && audio2.equals("2")) {      //벨소리 올리기 위해서 올린다.
                    aManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_RAISE, 0);
                }
                audioStr = audio2;
                break;
            case AudioManager.RINGER_MODE_SILENT:           //무음모드
                if(audio1 != null && (audio1.equals("1") || audio1.equals("2"))) {          //진동이거나 벨소리일 경우
                    aManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_RAISE, 0);
                }
                if(audio1 != null && audio1.equals("2")) {          //벨소리일 경우 한번더
                    aManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_RAISE, 0);
                }
                audioStr = audio1;
                break;
        }

        NotificationCompat.Builder nBuilder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.logo)
                    .setContentTitle(dataMap.get("title"))
                    .setContentText(dataMap.get("msg"))
                    .setGroup(GROUP_KEY_WORK_PUSH);
        } else {
            nBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.logo)
                    .setContentTitle(dataMap.get("title"))
                    .setContentText(dataMap.get("msg"))
                    .setGroup(GROUP_KEY_WORK_PUSH);
        }

        //알림에 사운드 추가.
        if(audioStr != null && audioStr.equals("2")) {
            Uri defaultSoundUri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION);
            nBuilder.setSound(defaultSoundUri);
        }
        //알림에 진동 추가.
        if(audioStr != null && (audioStr.equals("1") || audioStr.equals("2"))) {
            nBuilder.setVibrate(new long[]{0, 3000, 100, 300});
        }
        //알림을 확인했을 때(알림창 클릭)
        Intent intent = new Intent(context, MainActivity.class);

        Bundle bundle = new Bundle();
        bundle.putString("pushOpen", pushOpen);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        //클릭할 때까지 액티비티 실행을 보류하고 있는 PendingIntent 객체 생성
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        nBuilder.setAutoCancel(true);     //클릭하면 자동으로 알림 삭제
        nBuilder.setContentIntent(contentIntent);   //PendingIntent 설정

        //Notification 객체 생성
        Notification notification= nBuilder.build();

        if(audioStr != null && (audioStr.equals("1") || audioStr.equals("2")) && repeatYN.equals("Y")) {
            notification.flags = Notification.FLAG_INSISTENT | Notification.FLAG_AUTO_CANCEL;  //알림확인할때까지 무한 반복, 클릭하면 자동으로 알림 삭제
        }

        manager.notify(new Random().nextInt(2147483600), notification);

        Notification summary;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            summary = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.logo)
                    .setStyle(new NotificationCompat.InboxStyle()
                            .setSummaryText("New Push Message"))
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setGroup(GROUP_KEY_WORK_PUSH)
                    .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN)
                    .setGroupSummary(true)
                    .build();
        } else {
            summary = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.logo)
                    .setStyle(new NotificationCompat.InboxStyle()
                            .setSummaryText("New Push Message"))
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setGroup(GROUP_KEY_WORK_PUSH)
                    .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN)
                    .setGroupSummary(true)
                    .build();
        }

        manager.notify(SUMMARY_ID, summary);
    }

    /* 원본 */
    /*private void sendNotification(Map<String, String> dataMap) {

        Context context = getApplicationContext();
        String GROUP_KEY_NOTIFICATION = context.getPackageName();
        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        String audio1 = dataMap.get("audio1");      //폰이 무음일 때 0 : 무음, 1 : 진동, 2 : 벨소리 + 진동
        String audio2 = dataMap.get("audio2");      //폰이 진동일 때 0 : 무음, 1 : 진동, 2 : 벨소리 + 진동
        String audio3 = dataMap.get("audio3");      //폰이 벨소리일 때 0 : 무음, 1 : 진동, 2 : 벨소리 + 진동

        String repeatYN = dataMap.get("repeatYN");  //무한반복 여부  Y : 무한반복, N : 1번만
        String pushOpen = dataMap.get("pushOpen");  //푸시알림을 클릭한 후 처리방법

        if(repeatYN == null || repeatYN.equals("")){
            repeatYN = "Y";
        }

        if(pushOpen == null || pushOpen.equals("")){
            pushOpen = "Y";
        }

        String audioStr = "0";
        //소리, 진동, 무음모드 파악
        AudioManager aManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        switch (aManager.getRingerMode()){
            case AudioManager.RINGER_MODE_NORMAL:           //소리모드
                audioStr = audio3;
                break;
            case AudioManager.RINGER_MODE_VIBRATE:          //진동모드
                if(audio2 != null && audio2.equals("2")) {      //벨소리 올리기 위해서 올린다.
                    aManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_RAISE, 0);
                }
                audioStr = audio2;
                break;
            case AudioManager.RINGER_MODE_SILENT:           //무음모드
                if(audio1 != null && (audio1.equals("1") || audio1.equals("2"))) {          //진동이거나 벨소리일 경우
                    aManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_RAISE, 0);
                }
                if(audio1 != null && audio1.equals("2")) {          //벨소리일 경우 한번더
                    aManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_RAISE, 0);
                }
                audioStr = audio1;
                break;
        }

        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(dataMap.get("title"))
                .setContentText(dataMap.get("msg"))
                .setGroup(GROUP_KEY_NOTIFICATION);

        //알림에 사운드 추가.
        if(audioStr != null && audioStr.equals("2")) {
            Uri defaultSoundUri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION);
            nBuilder.setSound(defaultSoundUri);
        }
        //알림에 진동 추가.
        if(audioStr != null && (audioStr.equals("1") || audioStr.equals("2"))) {
            nBuilder.setVibrate(new long[]{0, 3000, 100, 300});
        }
        //알림을 확인했을 때(알림창 클릭)
        Intent intent = new Intent(context, MainActivity.class);

        Bundle bundle = new Bundle();
        bundle.putString("pushOpen", pushOpen);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        //클릭할 때까지 액티비티 실행을 보류하고 있는 PendingIntent 객체 생성
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        nBuilder.setAutoCancel(true);     //클릭하면 자동으로 알림 삭제
        nBuilder.setContentIntent(contentIntent);   //PendingIntent 설정

        //Notification 객체 생성
        Notification notification= nBuilder.build();

        if(audioStr != null && (audioStr.equals("1") || audioStr.equals("2")) && repeatYN.equals("Y")) {
            notification.flags = Notification.FLAG_INSISTENT | Notification.FLAG_AUTO_CANCEL;  //알림확인할때까지 무한 반복, 클릭하면 자동으로 알림 삭제
        }

        nManager.notify(0 *//* ID of notification *//*, notification);
    }*/
}
