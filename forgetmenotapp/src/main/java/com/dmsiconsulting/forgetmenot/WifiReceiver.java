package com.dmsiconsulting.forgetmenot;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import java.util.Calendar;

public class WifiReceiver extends BroadcastReceiver {
    // TODO move following 2 into shared preferences from main settings activity, also add ringtone from there.
    private static final String MY_SSID = "\"MOSES-5G\"";
    private static final String MY_TILE_MAC = "C4:AE:02:84:35:3B";

    private static final String SHARED_PREFERENCE = "SHARED_PREFERENCE";
    private static final String LAST_SEEN_SSID = "LAST_SEEN_SSID";
    private static final String ALARM_CHANNEL = "com.dmsiconsulting.alarm";

    private BTLEScanner btleScanner = new BTLEScanner();

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                SHARED_PREFERENCE, Context.MODE_PRIVATE);
        String lastSeenSSID = sharedPref.getString(LAST_SEEN_SSID, null);
        ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMan.getActiveNetworkInfo();
        if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            Log.d("WifiReceiver", "Have Wifi Connection" + ":" + netInfo.getExtraInfo());
        } else {
            Log.d("WifiReceiver", "Don't have Wifi Connection" + netInfo);
            Log.v("WifiReceiver", MY_SSID + ":" + lastSeenSSID);
            // netInfo can be null or set with the fallback (eg: LTE)
            if ((netInfo == null || !MY_SSID.equals(netInfo.getExtraInfo())) && MY_SSID.equals(lastSeenSSID)) {
                Log.d("WifiReceiver", "Disconnected from SSID of interest.  Checking to sound alarm");
                if (goingToWork()) {
                    btleScanner.scanForDevice(MY_TILE_MAC, 10000, (foundDevice) -> {
                        if (foundDevice) {
                            Log.d("WifiReceiver", "Phew, I got my backpack with me.");
                        } else {
                            soundAlarm(context);
                        }
                    });
                }
            }
        }
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(LAST_SEEN_SSID, netInfo == null ? null : netInfo.getExtraInfo());
        editor.commit();
    }

    private void soundAlarm(Context context) {
        Log.i("WifiReceiver", "Sounding alarm!!!!");
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel alarmChannel = new NotificationChannel(ALARM_CHANNEL, "ForgetMeNot ALARM", NotificationManager.IMPORTANCE_HIGH);
        Uri alarmUri = RingtoneManager.getActualDefaultRingtoneUri(context.getApplicationContext(), RingtoneManager.TYPE_ALARM);

        alarmChannel.setSound(alarmUri,new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build());
        nm.createNotificationChannel(alarmChannel);

        Notification.Builder builder = new Notification.Builder(context, ALARM_CHANNEL)
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setChannelId(ALARM_CHANNEL)
                .setAutoCancel(true)
                .setTicker("My Alarm ticker") // use something from something from R.string
                .setContentTitle("You forgot your backpack") // use something from something from
                .setContentText("My Alarm content"); // display indeterminate progress

        Notification alarmNotification = builder.build();
        nm.notify(1234567, alarmNotification);
    }

    private boolean goingToWork() {
        Calendar rightNow = Calendar.getInstance();
        int dayOfWeek = rightNow.get(Calendar.DAY_OF_WEEK);
        int hourOfDay = rightNow.get(Calendar.HOUR_OF_DAY);
        // TODO - see if twin cities
        return 1 < dayOfWeek && dayOfWeek < 7 && 5 < hourOfDay && hourOfDay < 11;
    }
}
