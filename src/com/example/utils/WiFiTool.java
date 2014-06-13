package com.example.utils;

public class WiFiTool {
	public static long mac2Long(String mac){
		mac = mac.replace(":", "");
		return Long.valueOf(mac, 16);
	}
}
