package com.example.utils;

import java.util.LinkedList;
import java.util.List;

public class ScanResultBuffer {
	public static List<float[]> strengthList = new LinkedList<float[]>();
	public static List<long[]> macList = new LinkedList<long[]>();
	public static List<Long> timestamps = new LinkedList<Long>();
	public static void reset(){
		strengthList.clear();
		macList.clear();
		timestamps.clear();
	}
}
