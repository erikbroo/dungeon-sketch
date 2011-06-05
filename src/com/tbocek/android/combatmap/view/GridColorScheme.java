package com.tbocek.android.combatmap.view;

import android.graphics.Color;

public class GridColorScheme {
	public static final GridColorScheme STANDARD = new GridColorScheme(Color.WHITE, Color.rgb(200, 200, 200));
	public static final GridColorScheme GRAPH_PAPER = new GridColorScheme(Color.rgb(248, 255, 180), Color.rgb(195, 255, 114));
	
	private int backgroundColor;
	private int lineColor;
	
	public GridColorScheme(int backgroundColor, int lineColor) {
		this.backgroundColor = backgroundColor;
		this.lineColor = lineColor;
	}

	int getBackgroundColor() {
		return backgroundColor;
	}

	int getLineColor() {
		return lineColor;
	}
}
