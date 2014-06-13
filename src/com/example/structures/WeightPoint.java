package com.example.structures;


public class WeightPoint extends Point{
	public int sameMacCount = 0;

	public WeightPoint(double x, double y, float distance, int sameMacCount) {
		super();
		this.x = (int) x;
		this.y = (int) y;
		weight = distance;
		this.sameMacCount = sameMacCount;
	}

	public WeightPoint() {
		super();
	}

	@Override
	public String toString() {
//		return "[" + x + ", " + y + ";"
//				+ sameMacCount + ";"+ weight+"]";
        return "[" + x + ", " + y + ";"+ (int)weight+"]";
	}

	

	
}
