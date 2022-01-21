package com.mycompany.plugins.example.GPS;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.mycompany.plugins.example.GoogleMapActivity;

import java.util.Collections;

public class Geolocation {

  private Context context;
  public boolean permission_granted = false;
  public boolean GPS_Enable = false;


  private LocationRequest locationRequest;
  private FusedLocationProviderClient fusedLocationProviderClient;

  GoogleMap googleMap;


  public Geolocation(Context context, GoogleMap googleMap) {
    this.context = context;
    this.googleMap = googleMap;
  }

  // GPS를 사용하기 위해서는 ACCES FINE LOCATION 권한이 필요하므로 권한검사
  // ACCESS FINE LOCATION 이 있으면 자동적으로 COUARS LOCATION 권한도 있으므로 FINE LOCATION만 검사한다.
  private void checkPermission() {
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
  private boolean checkGooglePlayService_Available() {
    int result = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
    boolean available;
    switch (result) {
      case ConnectionResult.SUCCESS:
        Log.v("GEOTEST", "이용가능, SUCCESS");
        available = true;
        break;
      case ConnectionResult.SERVICE_MISSING:
        Log.v("GEOTEST", "이용불가, SERVICE_MISSING");
        available = false;
        break;
      case ConnectionResult.SERVICE_UPDATING:
        Log.v("GEOTEST", "이용불가, SERVICE_UPDATING");
        available = false;
        break;
      case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
        Log.v("GEOTEST", "이용불가, SERVICE_VERSION_UPDATE_REQUIRED");
        available = false;
        break;
      case ConnectionResult.SERVICE_DISABLED:
        Log.v("GEOTEST", "이용불가, SERVICE_DISABLED");
        available = false;
        break;
      case ConnectionResult.SERVICE_INVALID:
        Log.v("GEOTESET", "이용불가, SERVICE_INVALID");
        available = false;
        break;
      default:
        Log.v("GEOTEST", "이용불가, 알수없는 이유 code : " + result);
        available = false;
        break;
    }
    return available;
  }

  // 설정된 GPS 값이 디바이스에서 설정가능한지 확인. 비동기적으로 값이 들어오기 때문에 task 를 받아서 이벤트 처리 필요
  private Task<LocationSettingsResponse> checkGPS_Enabled() {
    locationRequest = LocationRequest.create();
    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
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


  // 먼저 권한 설정이 되어있는지 확인
  // GPS설정이 가능한지 확인 후 성공 콜백 받으면, 마지막으로 구글 플레이서비스 API 이용 가능한지 확인후 GPS 시작
  public void GPS_START() {
    checkPermission();
    CancellationTokenSource cts = new CancellationTokenSource();

    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
    Task<Location> task = fusedLocationProviderClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY,cts.getToken());
    task.addOnSuccessListener(new OnSuccessListener<Location>() {
      @Override
      public void onSuccess(Location location) {
        Log.v("GEOTEST","현재 고도 : "+location.getAltitude()+" 경도(longitude):"+location.getLongitude()+"  위도(laitude):"+location.getLatitude());
      }
    });


    checkGPS_Enabled().addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
      @Override
      public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
        if (permission_granted == true
        && checkGooglePlayService_Available() == true) {
          GPS_Enable = true;
          Log.v("GEOTEST", "설정가능");

          if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                  && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
          }

          fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
          fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
      }
    }).addOnFailureListener(new OnFailureListener() {
      @Override
      public void onFailure(@NonNull Exception e) {
        ApiException error = (ApiException) e;
        Log.v("GEOTEST", "error message : "+error.getStatus().getStatusMessage());
        GPS_Enable = false;
      }
    });
  }

  private LocationCallback locationCallback = new LocationCallback() {
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onLocationResult(@NonNull LocationResult locationResult) {
      super.onLocationResult(locationResult);

      double longitude = locationResult.getLastLocation().getLongitude();
      double laitude = locationResult.getLastLocation().getLatitude();
      double alitude = locationResult.getLastLocation().getAltitude();


      LatLng NOW_LOCATION = new LatLng(laitude, longitude);

      MarkerOptions markerOptions = new MarkerOptions();
      markerOptions.position(NOW_LOCATION);
      markerOptions.title("현재위치 테스트");
      markerOptions.snippet("지금의 현재위치");


      if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
              && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        return;
      }

      GoogleMap googleMap = GoogleMapActivity.getGoogleMap();
      googleMap.setMyLocationEnabled(true);
      googleMap.getUiSettings().setMyLocationButtonEnabled(true);
      googleMap.addMarker(markerOptions);

      googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
              new LatLng(laitude, longitude), 18));
     /* googleMap.animateCamera(CameraUpdateFactory.zoomBy(10));*/

      Log.v("GEOTEST","위도(laitude) : "+laitude+"   경도(longitude) : "+longitude+" 고도(Alitude):"+alitude);
      Log.v("GEOTEST","고도 정확도 : "+locationResult.getLastLocation().getVerticalAccuracyMeters());

      Toast.makeText(context,"위도(laitude) : "+laitude+"   경도(longitude) : "+longitude+" 고도(Alitude):"+alitude, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
      super.onLocationAvailability(locationAvailability);
      Log.v("GEOTEST","onLocationAvailablity - "+locationAvailability);
    }
  };

  public void GPS_STOP(){
    fusedLocationProviderClient.removeLocationUpdates(locationCallback);
  }
}
