package com.example.utils;

import java.util.ArrayList;
import java.util.List;

public class ArrayUtil {

	public static List<Float> toList(float[] t){
		List<Float> list = new ArrayList<Float>();
		for (int i = 0; i < t.length; i++) {
			list.add(t[i]);
		}
		return list;
	}
	public static List toList(long[] t){
		List<Long> list = new ArrayList<Long>();
		for (int i = 0; i < t.length; i++) {
			list.add(t[i]);
		}
		return list;
	}
	public static long[] toArray(List<Long> list){
		long[] n = new long[list.size()];
		for (int i = 0; i < list.size(); i++) {
			n[i] = list.get(i);
		}
		return n;
	}
	public static int commonElemsCount(List<Integer> list1, List<Long> list2){
		int count = 0;
		for (Integer i1 : list1) {
			for (Long i2 : list2) {
				if (i1 - i2 == 0) {
					count++;
				}
			}
		}
		return count;
	}
	
}
