package com.mycompany.plugins.example.GPS;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

public class CustomGeoFence {

    public PendingIntent geofencePendingIntent;
    private GeofencingClient geofencingClient;


    public CustomGeoFence() {
    }

    @SuppressLint("Range")
    private GeofencingRequest getGeofencingRequest(double longitude, double latitude) {

        List<Geofence> geoFenceList = new ArrayList<Geofence>();

        //경도(longitude):127.0531348  위도(laitude):37.5124846
        //서울특별시 강남구 삼성동 삼성로 573 성원빌딩 4층

/*        longitude = 127.0531348;
        latitude = 37.5124846;*/
        float GEOFENCE_RADIUS_IN_METERS = 50; //반경 50m 의 원
        String id = "quarantine_area"; //지오펜스의 ID 지정

        //Long GEOFENCE_EXPIRATION_IN_MILLISECONDS = 60 * 1000L;

        Geofence geofence = new Geofence.Builder()
                .setRequestId(id)
                .setCircularRegion(
                        latitude,
                        longitude,
                        GEOFENCE_RADIUS_IN_METERS)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL)
                .setLoiteringDelay(5000)
                .setNotificationResponsiveness(5000)
                .build();

        GeofencingRequest.Builder request_builder = new GeofencingRequest.Builder();
        request_builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        request_builder.addGeofence(geofence);
        return request_builder.build();
    }


    public PendingIntent getGeofencePendingIntent(Context context) {
        // Reuse the PendingIntent if we already have it.\

        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(context, GeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        geofencePendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }

    public void addGeofences(Context context) {
        new Geolocation().getCurrentLocation(context).addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                geofencingClient = LocationServices.getGeofencingClient(context);
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                geofencingClient.addGeofences(getGeofencingRequest(location.getLongitude(), location.getLatitude()), getGeofencePendingIntent(context))
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.v("GEOFENCE", "지오펜스 등록 성공");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.v("GEOFENCE", "지오펜스 등록 실패");
                    }
                });
            }
        });
    }
}
