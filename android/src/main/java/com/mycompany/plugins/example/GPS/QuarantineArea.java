package com.mycompany.plugins.example.GPS;

import android.util.Log;

public class QuarantineArea {
    double latitude;
    double longitude;
    int radius;
    String address;
    final static int CellID = 768781;

    int altitudeLimit = 3;

    double max_altitudeLimit;
    double min_altitdueLimit;

    public QuarantineArea(double latitude, double longitude, int radius, double altitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        max_altitudeLimit = altitude+altitudeLimit;
        min_altitdueLimit = altitude-altitudeLimit;

        Log.v("GEOFENCE","max_altitudeLimit : "+max_altitudeLimit + "   min_altitudeLimit : "+min_altitdueLimit);
    }

    public double getMax_altitudeLimit() {
        return max_altitudeLimit;
    }

    public double getMin_altitdueLimit() {
        return min_altitdueLimit;
    }
}
