package com.example.wifiloc;

public class Signal {
	public long localMacIndex;
	public float strength;
	public Signal(long mac, float strength) {
		super();
		this.localMacIndex = mac;
		this.strength = strength;
	}
	public Signal(String mac, String strength) {
		super();
		this.localMacIndex = Long.valueOf(mac);
		this.strength = Float.valueOf(strength);
	}
	@Override
	public String toString() {
		return "(" + localMacIndex + "," + strength + ")";
	}
	
}
