package com.mycompany.plugins.example;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.mycompany.plugins.example.GPS.Geolocation;

public class GoogleMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static GoogleMap getGoogleMap() {
        return googleMap;
    }

    static GoogleMap googleMap;
    Button start_button;
    Button stop_button;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maplayout);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Geolocation geolocation = new Geolocation(this, googleMap);

        start_button = (Button) findViewById(R.id.button_start);
        stop_button = (Button) findViewById(R.id.button_stop);

        start_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                geolocation.GPS_START();
            }
        });


        stop_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                geolocation.GPS_STOP();
            }
        });
    }



    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;

        LatLng SEOUL = new LatLng(37.56, 126.97);

/*        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(SEOUL);
        markerOptions.title("서울");
        markerOptions.snippet("한국의 수도");
        this.googleMap.addMarker(markerOptions);*/

        this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(SEOUL));
        this.googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

}
