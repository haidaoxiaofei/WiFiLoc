package com.example.wifiloc;

import java.util.Arrays;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

import com.example.interfaces.EstimationListener;
import com.example.utils.ScanResultBuffer;
import com.example.utils.WiFiTool;


public class WifiReceiver extends BroadcastReceiver {

	private static boolean isScanning = false;
    private boolean AP_FILTERING = true;
    private WifiManager wifiManager;
    private long lastWiFiTS;// timestamp for last wifi record
    Handler mHandler = new Handler();
    private EstimationListener listener;
    private static final String[] wifi_names = {"The_Dragon","VLDB2014","WiFiSong"};
//    private static final String[] wifi_names = {"sMobileNet","Universities WiFi","Y5ZONE","Alumni","eduroam","PCCW"};
//    private static final String[] wifi_names = {"epfl","public-epfl","eduroam","Swisscom_Auto_Login"};
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                long current = System.currentTimeMillis();
                                if(isScanning && current - lastWiFiTS >= 2000){
                                    startScan();
                                }
                            }
                        });
                    } catch (Exception e) {
                    }
                }
            }
        }).start();
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        if (!isScanning) {
            return;
        }
        lastWiFiTS = System.currentTimeMillis();
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

        Log.i("sampleS:",Arrays.toString(signals));
        ScanResultBuffer.macList.add(macList);
        ScanResultBuffer.strengthList.add(signals);
        ScanResultBuffer.timestamps.add(System.currentTimeMillis());
        listener.estimatePosition();
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




