package com.example.interfaces;

import com.example.structures.AreaPoint;

public interface EstimationListener {
	void estimatePosition();
	void updateEstimatedPosition(AreaPoint p);
}
