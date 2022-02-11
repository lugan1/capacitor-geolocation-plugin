package com.mycompany.plugins.example.WPS;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.util.List;

public class WifiUtil {
    WifiManager wifiManager;
    BroadcastReceiver wifiScanReceiver;
    List<ScanResult> results;
    Context context;

    public WifiUtil(Context context) {
        this.context = context;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context rcontext, Intent intent) {
                boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success){
                    // 스캔 성공
                    results = wifiManager.getScanResults();
                    //wifi ap 목록 가져오기
                    //context.unregisterReceiver(wifiScanReceiver);
                }
            }
        };
    }

    // wifi 스캔
    public void startWifiScan(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(wifiScanReceiver, intentFilter);
        boolean success = wifiManager.startScan();
        //startScan()으로 wifi 스캔

    }

    // wifi 켜져있는지 체크 (GPS 정확도 향상. 특히 실내에서 위치 측정시 중요)
    public boolean check_wifiOn(){
        if(wifiManager.isWifiEnabled()){
            return true;
        }
        return false;
    }

    // wifi 꺼져있을시의 얼럿 출력
    public void showWifiAlert(Activity activity){
        AlertDialog.Builder wifiAlertDialog = new AlertDialog.Builder(activity);
        // alert의 title과 Messege 세팅

        wifiAlertDialog.setTitle("WIFI 상태 확인");
        wifiAlertDialog.setMessage("WIFI가 꺼져있습니다.\n실내에서 위치측정시 WIFI가 켜져있어야 정확한 현재위치가 측정됩니다.");
        // 버튼 추가 (Ok 버튼과 Cancle 버튼 )
        wifiAlertDialog.setPositiveButton("WIFI 설정",new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog,int which){
                // OK 버튼을 눌렸을 경우
                if(Build.VERSION.SDK_INT >= 29){
                    // api 29 이상 부터는 wifi 설정 패널을 띄울수 있다.
                    Intent panelIntent = new Intent(Settings.Panel.ACTION_WIFI);
                    activity.startActivity(panelIntent);
                }
                else {
                    // WIFI 설정 Activity 띄우기
                    activity.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                }
            }
        });
        wifiAlertDialog.setCancelable(false);

        // Alert를 생성해주고 보여주는 메소드(show를 선언해야 Alert가 생성됨)
        wifiAlertDialog.show();
    }
}
