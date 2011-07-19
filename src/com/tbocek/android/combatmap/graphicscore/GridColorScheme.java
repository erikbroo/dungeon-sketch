package com.tbocek.android.combatmap.graphicscore;

import java.io.Serializable;

import android.graphics.Color;

public final class GridColorScheme implements Serializable{
	private static final long serialVersionUID = -7991703730328026635L;
	
	public static final GridColorScheme STANDARD = new GridColorScheme(Color.WHITE, Color.rgb(200, 200, 200));
	public static final GridColorScheme GRAPH_PAPER = new GridColorScheme(Color.rgb(248, 255, 180), Color.rgb(195, 255, 114));
	public static final GridColorScheme GRASS = new GridColorScheme(Color.rgb(63, 172, 41), Color.rgb(11, 121, 34));
	public static final GridColorScheme NIGHT = new GridColorScheme(Color.rgb(0, 0, 102), Color.rgb(83, 36, 0));
	
	public static GridColorScheme fromNamedScheme(String name) {
		if (name.equals("graphpaper")) return GRAPH_PAPER;
		if (name.equals("grass")) return GRASS;
		if (name.equals("night")) return NIGHT;
		return STANDARD;
	}
	
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
