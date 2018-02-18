package com.dmsiconsulting.forgetmenot;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.JobIntentService;
import android.util.Log;

import java.util.List;

public class MyIntentService extends JobIntentService {
    private static final String NOTIFICATION_CHANNEL_ID = "com.dmsiconsulting.service";

    private BroadcastReceiver myBroadcastReceiver;

    public MyIntentService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("MyIntentService", "Initialize");
        myBroadcastReceiver = new WifiReceiver();

        getApplicationContext().registerReceiver(myBroadcastReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        nm.createNotificationChannel(new NotificationChannel(NOTIFICATION_CHANNEL_ID, "App Service", NotificationManager.IMPORTANCE_NONE));

        Notification.Builder builder = new Notification.Builder(getBaseContext(), NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setChannelId(NOTIFICATION_CHANNEL_ID)
                .setTicker("Your Ticker")
                .setContentTitle("Keeping you safe from forgetting")
                .setContentText("Always up");

        startForeground(1, builder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("MyIntentService", "Destroyed");
        getApplicationContext().unregisterReceiver(myBroadcastReceiver);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
    }
}
