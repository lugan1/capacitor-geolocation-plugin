package com.mycompany.plugins.example.GPS;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import android.app.AlertDialog;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
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
import com.mycompany.plugins.example.SampleGeolocationPlugin;

import java.util.Collections;

public class Geolocation {

  public static final int LOCATION_IN_AREA = 0;
  public static final int LOCATION_OUT_CIRCLE = 1;
  public static final int LOCATION_OUT_ALTITUDE = 2;
  public static final int LOCATION_OUT_CIRCLE_AND_ALTITUDE = 3;
  public static final int CELLID_CHANGED = 4;



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
      return;
    } else {
      // 권한이 허용된 상태가 아니라면, 얼럿을 띄워서 권한 요청을 한다.
/*      AlertDialog.Builder builder = new AlertDialog.Builder(SampleGeolocationPlugin.plugin.getBridge().getActivity());
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
      alertDialog.show();*/
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
    Task<Location> task = fusedLocationProviderClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, cts.getToken());
    return task;
  }


  public int is_containsLocation(double latitude, double longitude, double altitdue, QuarantineArea quarantineArea, Context context) {
    float[] distance = new float[2];
    Location.distanceBetween(latitude, longitude, quarantineArea.latitude, quarantineArea.longitude, distance);
    if ((distance[0] > quarantineArea.radius) && (altitdue >= quarantineArea.max_altitudeLimit || altitdue <= quarantineArea.min_altitdueLimit)) {
      Log.v("GEOFENCE", "원 밖에있음 + 고도 이탈 : " + distance[0]);
      return 3;
    } else if (distance[0] > quarantineArea.radius) {
      Log.v("GEOFENCE", "원 밖에 있음 : " + distance[0]);
      return 1;
    } else if (altitdue >= quarantineArea.max_altitudeLimit || altitdue <= quarantineArea.min_altitdueLimit) {
      Log.v("GEOFENCE", "고도 이탈 : 현재 고도" + altitdue);
      return 2;
    } else if (getCurrentCellId(context) != QuarantineArea.CellID){
      Log.v("GEOFENCE", "CELL ID 변경됨");
      return Geolocation.CELLID_CHANGED;
    } else {
      Log.v("GEOFENCE", "원 안에 있음 : " + distance[0]);
      return 0;
    }
  }

  public void getCurrentPressure(Context context) {
    SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    Sensor pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);

    SensorEventListener sensorEventListener = new SensorEventListener() {
      @Override
      public void onSensorChanged(SensorEvent event) {
        //센서에서 새로운 값을 보고할때 콜백. SensorEvent => 데이터의 정확도, 데이터를 생성한 센서, 타임스탬프등 표시
        Log.v("SALTITUDE", "현재 압력 : " + event.values[0]);

        float altitude = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, event.values[0]);

        Log.v("SALTITUDE", "현재 기압고도 : " + altitude);
        sensorManager.unregisterListener(this, pressureSensor);
        //호출당 한번의 값만 필요하기 때문의 값을 얻으면 콜백 바로해제
      }

      @Override
      public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //센서의 정확도가 변경될때 콜백

      }
    };

    sensorManager.registerListener(sensorEventListener, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL);
  }

  public int getCurrentCellId(Context context) {
    TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      return 0;
    }
    GsmCellLocation gsmCellLocation = (GsmCellLocation) telephonyManager.getCellLocation();
    int cellID = gsmCellLocation.getCid();
    int lac = gsmCellLocation.getLac();
    int psc = gsmCellLocation.getPsc();
    Toast.makeText(context, "현재 기지국 : "+cellID+" lac : "+lac+" psc : "+psc, Toast.LENGTH_LONG).show();
    return cellID;
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
