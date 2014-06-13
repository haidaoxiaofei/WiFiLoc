package com.example.wifiloc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.example.interfaces.EstimationListener;
import com.example.utils.ScanResultBuffer;
import com.example.utils.WiFiTool;

import java.util.List;

public class WifiReceiver extends BroadcastReceiver {

    private static boolean isScanning = false;
    public boolean isCollecting = false;
    private boolean AP_FILTERING = true;
    private WifiManager wifiManager;
    private EstimationListener listener;
    private static final String[] wifi_names = {"sMobileNet","Universities WiFi","Y5ZONE","Alumni","eduroam","PCCW"};
    private boolean isFocus(String name) {
        for (int i = 0; i < wifi_names.length; i++) {
            if (wifi_names[i].equals(name)) {
                return true;
            }
        }
        return false;
    }
    public WifiReceiver(WifiManager wifiManager, EstimationListener listener) {
        super();
        this.wifiManager = wifiManager;
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("receive", "receive");
        if (!isScanning) {
            return;
        }
        List<ScanResult> results = wifiManager.getScanResults();
        for (int i = results.size() - 1; i >= 0 ; i--) {
            if (AP_FILTERING && !isFocus(results.get(i).SSID)) {
                results.remove(i);
            }
        }


        float signals[] = new float[results.size()];
        long macList[] = new long[results.size()];

        for (int i = 0; i < results.size(); i++) {
            signals[i] = results.get(i).level;
            macList[i] = WiFiTool.mac2Long(results.get(i).BSSID);
        }
        ScanResultBuffer.macList.add(macList);
        ScanResultBuffer.strengthList.add(signals);
        ScanResultBuffer.timestamps.add(System.currentTimeMillis());
        if (!isCollecting) {
            listener.estimatePosition();
        }


        wifiManager.startScan();

    }

    public void startScan(){
        isScanning = true;
        wifiManager.startScan();
    }

    public void stopScan(){
        isScanning = false;
    }
}



