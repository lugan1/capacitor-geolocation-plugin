package com.mycompany.plugins.example;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.mycompany.plugins.example.GPS.CustomGeoFence;
import com.mycompany.plugins.example.GPS.Geolocation;
import com.mycompany.plugins.example.GPS.QuarantineArea;
import com.mycompany.plugins.example.WPS.WifiUtil;

public class GoogleMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    SampleGeolocationPlugin sampleGeolocationPlugin;

    static GoogleMapActivity googleMapActivity;

    public GoogleMapActivity() {
        googleMapActivity = this;
    }

    public static void setGoogleMapObj(GoogleMap googleMapObj) {
        GoogleMapActivity.googleMapObj = googleMapObj;
    }

    public static GoogleMap getGoogleMapObj() {
        return googleMapObj;
    }

    static GoogleMap googleMapObj;
    Button start_button;
    Button stop_button;
    static LatLng softnet;

    static double a;
    static double b;
    static int r;
    static Circle circle;

    static QuarantineArea quarantineArea;


    public static Geolocation getGeolocation() {
        return geolocation;
    }

    static Geolocation geolocation;
    WifiUtil wifiUtil;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maplayout);

        geolocation = new Geolocation();
        wifiUtil = new WifiUtil(getApplicationContext());


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        CustomGeoFence customGeoFence = new CustomGeoFence();


        start_button = (Button) findViewById(R.id.button_start);
        stop_button = (Button) findViewById(R.id.button_stop);

        start_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customGeoFence.addGeofences(getApplicationContext());
                BackgroundGPS backgroundGPS = new BackgroundGPS();
                backgroundGPS.execute(getApplicationContext());
            }
        });


        stop_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(wifiUtil.check_wifiOn()){
                    wifiUtil.startWifiScan();
                }
                else{
                    wifiUtil.showWifiAlert(googleMapActivity);
                }
            }
        });
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Task<Location> locationTask = geolocation.getCurrentLocation(getApplicationContext());
        if(locationTask != null){
            locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    a = location.getLatitude();
                    b = location.getLongitude();
                    r = 20;
                    quarantineArea = new QuarantineArea(a, b, r, location.getAltitude());
                    CircleOptions circleOptions = new CircleOptions()
                            .center(new LatLng(location.getLatitude(), location.getLongitude()))
                            .radius(20); // In meters

                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    googleMap.setMyLocationEnabled(true);
                    googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                    circle = googleMap.addCircle(circleOptions);

                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
                    googleMap.animateCamera(CameraUpdateFactory.zoomTo(18));
                    googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    setGoogleMapObj(googleMap);
                }
            });
        }
        else {
            //
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    static class BackgroundGPS extends AsyncTask<Context, String, Integer>{

        @Override
        protected Integer doInBackground(Context... context) {
            while (!isCancelled()){
                try {
                    Thread.sleep(5000L);

                    Task<Location> locationTask = geolocation.getCurrentLocation(context[0]);
                    if(locationTask != null){
                        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                //Log.v("GEOTEST","현재 고도 : "+location.getAltitude()+" 경도(longitude):"+location.getLongitude()+"  위도(laitude):"+location.getLatitude());

                                int is_containsLocation;

                                //((SampleGeolocationPlugin)SampleGeolocationPlugin.plugin).listenerTest();

                                //geolocation.getCurrentPressure(context[0]);
                                //geolocation.getCurrentCellId(context[0]);


                                is_containsLocation = geolocation.is_containsLocation(location.getLatitude(), location.getLongitude(), location.getAltitude(), quarantineArea, context[0]);
                                switch (is_containsLocation){
                                    case Geolocation.LOCATION_IN_AREA :
                                        //Toast.makeText(context[0], "원 안에 들어있음. 고도 : "+location.getAltitude()+"\n최대 고도 : "+quarantineArea.getMax_altitudeLimit()+"\n최저 고도 : "+quarantineArea.getMin_altitdueLimit(), Toast.LENGTH_SHORT).show();
                                        break;
                                    case Geolocation.LOCATION_OUT_CIRCLE:
                                        //Toast.makeText(context[0], "원 밖에 나가있음. 고도 : "+location.getAltitude()+"\n최대 고도 : "+quarantineArea.getMax_altitudeLimit()+"\n최저 고도 : "+quarantineArea.getMin_altitdueLimit(), Toast.LENGTH_SHORT).show();
                                        break;
                                    case Geolocation.LOCATION_OUT_CIRCLE_AND_ALTITUDE:
                                        //Toast.makeText(context[0], "원 밖에 나가있음+고도 이탈중 : "+location.getAltitude()+"\n최대 고도 : "+quarantineArea.getMax_altitudeLimit()+"\n최저 고도 : "+quarantineArea.getMin_altitdueLimit(), Toast.LENGTH_SHORT).show();
                                        break;
                                    case Geolocation.LOCATION_OUT_ALTITUDE:
                                        //Toast.makeText(context[0], "고도이탈. 현재 고도 : "+location.getAltitude()+"\n최대 고도 : "+quarantineArea.getMax_altitudeLimit()+"\n최저 고도 : "+quarantineArea.getMin_altitdueLimit(), Toast.LENGTH_SHORT).show();
                                        break;
                                    case Geolocation.CELLID_CHANGED:
                                        Toast.makeText(context[0],"CELL ID 변경됨", Toast.LENGTH_SHORT).show();
                                        break;
                                }


/*                            float[] distance = new float[2];
                            Location.distanceBetween(location.getLatitude(), location.getLongitude(), a, b, distance);

                            if( distance[0] > r){
                                Log.v("GEOFENCE","원 밖에 있음 : "+distance[0]);
                                Toast.makeText(context[0], "원 밖에 있음", Toast.LENGTH_SHORT).show();
                            } else{
                                Log.v("GEOFENCE","원 안에 있음 : "+distance[0]);
                                Toast.makeText(context[0], "원 안에 있음", Toast.LENGTH_SHORT).show();
                            }*/
                            }
                        });
                    }
                    else{
                        //geolocation.showWifiAlert(googleMapActivity);
                    }
                }catch (InterruptedException exception){

                }

            }
            return null;
        }
    }

    boolean is_containsLocation(double latitude, double longitude, double circle_latitude, double circle_longitude, int circle_radius){
        float[] distance = new float[2];
        Location.distanceBetween(latitude, longitude, circle_latitude, circle_longitude, distance);

        if( distance[0] > circle_radius){
            Log.v("GEOFENCE","원 밖에 있음 : "+distance[0]);
            //Toast.makeText(context[0], "원 밖에 있음", Toast.LENGTH_SHORT).show();
            return false;
        } else{
            Log.v("GEOFENCE","원 안에 있음 : "+distance[0]);
            //Toast.makeText(context[0], "원 안에 있음", Toast.LENGTH_SHORT).show();
            return true;
        }
    }

/*    boolean is_containsLocation(double x, double y, double a, double b, double r){
        if(Math.pow((x-a),2) + Math.pow((y-b),2) <= Math.pow(r,2)){
            return true;
        }
        else{
            return false;
        }
    }*/

}
