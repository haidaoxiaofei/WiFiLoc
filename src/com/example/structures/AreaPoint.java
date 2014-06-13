package com.example.structures;

public class AreaPoint extends WeightPoint{
//	public WeightPoint2D p;
	public int areaId;
	public AreaPoint(WeightPoint p, int areaId) {

		this.x = p.x;
		this.y = p.y;
		this.areaId = areaId;
	}
	public AreaPoint(int x, int y, int areaId) {
		super();
		this.x = x;
		this.y = y;
		this.areaId = areaId;
	}

    public AreaPoint(String x, String y, String areaId, String weight) {
        super();
        this.x = Integer.valueOf(x);
        this.y = Integer.valueOf(y);
        this.areaId = Integer.valueOf(areaId);
        this.weight = Double.valueOf(weight);
    }
	public AreaPoint() {
		super();
	}
}
