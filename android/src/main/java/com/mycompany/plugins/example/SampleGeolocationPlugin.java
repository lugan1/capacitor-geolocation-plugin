package com.mycompany.plugins.example;

import android.Manifest;
import android.content.Intent;

import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;
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

    private SampleGeolocation implementation = new SampleGeolocation();

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
/*        Geolocation geolocation = new Geolocation(getContext());
        geolocation.checkPermission();
        geolocation.checkGPS_Enabled();*/
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
}
