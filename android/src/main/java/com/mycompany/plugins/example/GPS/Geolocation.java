package com.mycompany.plugins.example.GPS;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import android.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.Task;

import java.util.Collections;

public class Geolocation {

  public static final int LOCATION_IN_AREA = 0;
  public static final int LOCATION_OUT_CIRCLE = 1;
  public static final int LOCATION_OUT_ALTITUDE = 2;
  public static final int LOCATION_OUT_CIRCLE_AND_ALTITUDE = 3;


  public boolean permission_granted = false;
  public boolean GPS_Enable = false;
  public boolean googlePlayService_Enable = false;

  private LocationRequest locationRequest;
  private FusedLocationProviderClient fusedLocationProviderClient;
  private CancellationTokenSource cts;

  public Geolocation() {
    cts = new CancellationTokenSource();
  }

  // GPS를 사용하기 위해서는 ACCES FINE LOCATION 권한이 필요하므로 권한검사
  // ACCESS FINE LOCATION 이 있으면 자동적으로 COUARS LOCATION 권한도 있으므로 FINE LOCATION만 검사한다.
  public void checkPermission(Context context) {
    int permssion = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
    if (permssion == PackageManager.PERMISSION_GRANTED) {
      // 권한이 허용 된 상태라면, GPS가 이용가능한 상태인지 확인
      permission_granted = true;
    } else {
      // 권한이 허용된 상태가 아니라면, 얼럿을 띄워서 권한 요청을 한다.
      AlertDialog.Builder builder = new AlertDialog.Builder(context);
      builder.setTitle("위치 권한이 꺼져있습니다.");
      builder.setMessage("[권한] 설정 -> [위치] 권한을 허용해야 합니다.");
      builder.setPositiveButton("설정으로 가기", (dialog, which) -> {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        context.startActivity(intent);
      });
      AlertDialog alertDialog = builder.create();
      alertDialog.show();
    }
  }

  // 구글플레이 서비스 API 가 이용 가능한 상태인지 확인
  public boolean checkGooglePlayService_Available(Context context) {
    int result = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
    switch (result) {
      case ConnectionResult.SUCCESS:
        Log.v("GEOTEST", "이용가능, SUCCESS");
        googlePlayService_Enable = true;
        break;
      case ConnectionResult.SERVICE_MISSING:
        Log.v("GEOTEST", "이용불가, SERVICE_MISSING");
        googlePlayService_Enable = false;
        break;
      case ConnectionResult.SERVICE_UPDATING:
        Log.v("GEOTEST", "이용불가, SERVICE_UPDATING");
        googlePlayService_Enable = false;
        break;
      case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
        Log.v("GEOTEST", "이용불가, SERVICE_VERSION_UPDATE_REQUIRED");
        googlePlayService_Enable = false;
        break;
      case ConnectionResult.SERVICE_DISABLED:
        Log.v("GEOTEST", "이용불가, SERVICE_DISABLED");
        googlePlayService_Enable = false;
        break;
      case ConnectionResult.SERVICE_INVALID:
        Log.v("GEOTESET", "이용불가, SERVICE_INVALID");
        googlePlayService_Enable = false;
        break;
      default:
        Log.v("GEOTEST", "이용불가, 알수없는 이유 code : " + result);
        googlePlayService_Enable = false;
        break;
    }
    return googlePlayService_Enable;
  }

  // 설정된 GPS 값이 디바이스에서 설정가능한지 확인. 비동기적으로 값이 들어오기 때문에 task 를 받아서 이벤트 처리 필요
  public Task<LocationSettingsResponse> checkGPS_Enabled(Context context) {
    locationRequest = LocationRequest.create();
    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    locationRequest.setInterval(2000L);
    locationRequest.setFastestInterval(1000L);

    LocationSettingsRequest.Builder builder = new LocationSettingsRequest
            .Builder()
            .addAllLocationRequests(Collections.singleton(locationRequest))
            .setAlwaysShow(true);

    SettingsClient settingsClient = LocationServices.getSettingsClient(context);

    // 설정한 세팅값이 디바이스 장치의 GPS 가 사용할수 있는지 테스트 ( Promise , Observable 패턴과 비슷하다)
    Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

    return task;
  }


  // Rx 패턴처럼 Task 객체를 반환하면 받은쪽에서 리스너 등록해서 사용
  public Task<Location> getCurrentLocation(Context context) {
    checkPermission(context);

    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
    Task<Location> task = fusedLocationProviderClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY,cts.getToken());
    return task;
  }


  public int is_containsLocation(double latitude, double longitude, double altitdue, QuarantineArea quarantineArea){
    float[] distance = new float[2];
    Location.distanceBetween(latitude, longitude, quarantineArea.latitude, quarantineArea.longitude, distance);
    if((distance[0] > quarantineArea.radius) && (altitdue >= quarantineArea.max_altitudeLimit || altitdue <= quarantineArea.min_altitdueLimit)) {
      Log.v("GEOFENCE","원 밖에있음 + 고도 이탈 : "+distance[0]);
      return 3;
    }
    else if(distance[0] > quarantineArea.radius){
      Log.v("GEOFENCE","원 밖에 있음 : "+distance[0]);
      return 1;
    }
    else if(altitdue >= quarantineArea.max_altitudeLimit || altitdue <= quarantineArea.min_altitdueLimit) {
      Log.v("GEOFENCE","고도 이탈 : 현재 고도"+altitdue);
      return 2;
    }
    else {
      Log.v("GEOFENCE","원 안에 있음 : "+distance[0]);
      return 0;
    }

  }

  /*private LocationCallback locationCallback = new LocationCallback() {
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onLocationResult(@NonNull LocationResult locationResult) {
      super.onLocationResult(locationResult);

      double longitude = locationResult.getLastLocation().getLongitude();
      double laitude = locationResult.getLastLocation().getLatitude();
      double alitude = locationResult.getLastLocation().getAltitude();

      if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
              && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        return;
      }

     *//* googleMap.animateCamera(CameraUpdateFactory.zoomBy(10));*//*

      Log.v("GEOTEST","위도(laitude) : "+laitude+"   경도(longitude) : "+longitude+" 고도(Alitude):"+alitude);
      Log.v("GEOTEST","고도 정확도 : "+locationResult.getLastLocation().getVerticalAccuracyMeters());

      Toast.makeText(context,"위도(laitude) : "+laitude+"   경도(longitude) : "+longitude+" 고도(Alitude):"+alitude, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
      super.onLocationAvailability(locationAvailability);
      Log.v("GEOTEST","onLocationAvailablity - "+locationAvailability);
    }
  };*/

/*  public void GPS_STOP(){
    fusedLocationProviderClient.removeLocationUpdates(locationCallback);
  }*/
}
