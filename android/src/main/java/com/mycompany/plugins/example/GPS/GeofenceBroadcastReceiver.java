package com.mycompany.plugins.example.GPS;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

        Log.v("GEOFENCE","지오펜스 브로드캐스트 수신");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "default");
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(new NotificationChannel("default","기본채널", NotificationManager.IMPORTANCE_DEFAULT));


        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceStatusCodes
                    .getStatusCodeString(geofencingEvent.getErrorCode());
            Log.v("GEOFENCE", errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Get the geofences that were triggered. A single event can trigger
        // multiple geofences.
        List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

        switch (geofenceTransition){
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                Log.v("GEOFENCE","지오펜스 안에 들어옴");
                builder.setContentTitle("지오펜스 ENTER")
                        .setSmallIcon(android.R.mipmap.sym_def_app_icon)
                        .setContentText("지오펜스 안에 들어옴");
                notificationManager.notify(1, builder.build());
                break;
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                Log.v("GEOFENCE","지오펜스 안에 배회중");
                builder.setContentTitle("지오펜스 DWELL")
                        .setSmallIcon(android.R.mipmap.sym_def_app_icon)
                        .setContentText("지오펜스 안에 배회중");
                notificationManager.notify(1, builder.build());
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Log.v("GEOFENCE","지오펜스 밖으로 나감");
                builder.setContentTitle("지오펜스 EXIT")
                        .setSmallIcon(android.R.mipmap.sym_def_app_icon)
                        .setContentText("지오펜스 밖에 나감");
                notificationManager.notify(1, builder.build());
                break;
        }


    }
}