package com.mycompany.plugins.example;

import static android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS;
import static com.mycompany.plugins.example.GoogleMapActivity.geolocation;

import android.Manifest;
import android.app.Instrumentation;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import android.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.mycompany.plugins.example.GPS.Geolocation;

@CapacitorPlugin(
        name = "SampleGeolocation",
        permissions = {
                @Permission(
                        alias = "ACCESS_COARSE_LOCATION",
                        strings = {Manifest.permission.ACCESS_COARSE_LOCATION}
                ),
                @Permission(
                        alias = "ACCESS_FINE_LOCATCION",
                        strings = {Manifest.permission.ACCESS_FINE_LOCATION}
                )
        }

)
public class SampleGeolocationPlugin extends Plugin {
    final String CHANNEL_ID = "test_channelID";


    private SampleGeolocation implementation = new SampleGeolocation();

    public static Plugin plugin;

    @Override
    public void load() {
        super.load();
        plugin = this;
        createNotificationChannel();
    }

    @PluginMethod
    public void requestPermission(PluginCall call){
        if((getPermissionState("ACCESS_COARSE_LOCATION")!= PermissionState.GRANTED)
                || (getPermissionState("ACCESS_FINE_LOCATCION") != PermissionState.GRANTED)){
            requestPermissionForAliases(new String[]{"ACCESS_COARSE_LOCATION", "ACCESS_FINE_LOCATCION"}, call, "locationPermissionCallback");
        }
        else {
            JSObject jsObject = new JSObject();
            jsObject.put("result","이미 모두 granted 상태");
            call.resolve(jsObject);
        }
    }

    @PermissionCallback
    public void locationPermissionCallback(PluginCall call){
        if((getPermissionState("ACCESS_COARSE_LOCATION")== PermissionState.GRANTED)
                && (getPermissionState("ACCESS_FINE_LOCATCION") == PermissionState.GRANTED)){
            JSObject jsObject = new JSObject();
            jsObject.put("result","권한승인 완료");
            call.resolve(jsObject);
        }
        else{
            call.reject("권한요청 거부");
        }
    }

    @PluginMethod
    public void gpsStart(PluginCall call){
        //createNotificationChannel();

/*

        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(getBridge().getContext(), getBridge().getActivity().getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getBridge().getContext(), 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), CHANNEL_ID)
                .setSmallIcon(android.R.mipmap.sym_def_app_icon)
                .setContentTitle("테스트 제목")
                .setContentText("테스트 내용")
                .setStyle(new NotificationCompat.BigTextStyle().bigText("큰 텍스트 제목"))
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(1, builder.build());
*/

        /*        Geolocation geolocation = new Geolocation(getContext());
        geolocation.checkPermission();
        geolocation.checkGPS_Enabled();*/


        Geolocation geolocation = new Geolocation();
        if(geolocation.checkGooglePlayService_Available(getContext())){
            geolocation.checkGPS_Enabled(getContext()).addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                @Override
                public void onSuccess(LocationSettingsResponse locationSettingsResponse) {

                    JSObject jsObject = new JSObject();
                    jsObject.put("result","GPS 이용가능");
                    call.resolve(jsObject);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
/*                    AlertDialog.Builder builder = new AlertDialog.Builder(getBridge().getActivity());
                    builder.setTitle("GPS 이용불가")
                            .setMessage("위치정보 기능을 사용할 수 없습니다. 위치정보 설정을 확인해주세요.")
                            .setPositiveButton("설정으로 가기", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent();

                                    // Application details requires package name as URI.
                                    if (ACTION_APPLICATION_DETAILS_SETTINGS.equals(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) {
                                        intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                        intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
                                    } else {
                                        intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    }

                                    // Start intent in activity.
                                    getActivity().startActivity(intent);
                                }
                            })
                            .setCancelable(false)
                            .create().show();*/
                    call.reject("GPS 이용불가");
                }
            });
        } else { call.reject("구글 플레이 서비스 API 이용 불가"); }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "테스트 이름"; //앱 정보의 알림 설정에서 표시될 채널 이름
            String description = "테스트 설명"; //앱 정보의 알림 설정에서 표시될 채널 설명
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);
            channel.setLightColor(Color.RED);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @PluginMethod
    public void gpsStop(PluginCall call){

    }




    @PluginMethod
    public void requestPsermissionMap(PluginCall call){

    }

    @PluginMethod
    public void openGoogleMap(PluginCall call){
        Intent intent = new Intent(getContext(), GoogleMapActivity.class);
        getActivity().startActivity(intent);
    }


    public void listenerTest(){
        JSObject jsObject = new JSObject();
        jsObject.put("name","testname");
        jsObject.put("number",5);


        notifyListeners("abnormal_vitalSign", jsObject);
    }
}
