package com.logictreeit.android.fracturedphoto.fcm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.dran.fracturedphoto.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.logictreeit.android.fracturedphoto.activities.PuzzlesGalleryActivity;
import com.logictreeit.android.fracturedphoto.utils.ApplicationConstants;

import org.json.JSONObject;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getNotification() != null) {
        }

        if (remoteMessage.getData().size() > 0) {
            try {
                JSONObject json = new JSONObject(remoteMessage.getData());
                handleDataMessage(json);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handleDataMessage(JSONObject json) {
        Log.v("push received", "push received - " + json.toString());
        try {
            String title = json.optString("title", "");
            String messageBody = json.optString("body", "");
            String puzzle_url = json.optString("puzzle_url", "");
            String preview_url = json.optString("preview_image", "");

            //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(puzzle_url));
            Intent intent = new Intent(this, PuzzlesGalleryActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra(ApplicationConstants.Extras_Keys.FROM_PUSH, true);
            intent.putExtra(ApplicationConstants.Extras_Keys.PUZZLE_URL, puzzle_url);

            displayNotification(intent, messageBody, title, null);
            /*try {
                Glide.with(getApplicationContext())
                        .asBitmap()
                        .load(preview_url)
                        .listener(new RequestListener<Bitmap>() {
                                      @Override
                                      public boolean onLoadFailed(@Nullable GlideException e, Object o, Target<Bitmap> target, boolean b) {
                                          displayNotification(intent, messageBody, title, null);
                                          return false;
                                      }

                                      @Override
                                      public boolean onResourceReady(Bitmap bitmap, Object o, Target<Bitmap> target, DataSource dataSource, boolean b) {
                                          displayNotification(intent, messageBody, title, bitmap);
                                          return false;
                                      }
                                  }
                        ).submit();
            } catch (Exception e) {
                displayNotification(intent, messageBody, title, null);
                e.printStackTrace();
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayNotification(Intent resultIntent, String messageBody, String title, Bitmap bmp) {
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(resultIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "fracturedphoto.notification.channel.id";
        String channelName = "fracturedphoto.notification.channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();

            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }

        PendingIntent contentIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(getNotificationIcon())
                .setColor(Color.parseColor("#009999"))
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.noti_large))
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle())
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_PROMO)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(contentIntent);

        Notification notification;
        if (bmp != null) {
            notification = notificationBuilder
                    .setStyle(new NotificationCompat.BigPictureStyle()
                    .bigPicture(bmp)
                    .bigLargeIcon(null)).build();
        } else {
            notification = notificationBuilder.build();
        }
        notificationManager.notify((int) System.currentTimeMillis(), notification);
    }

    private int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.fp_noti_small : R.drawable.small_logo;
    }
}