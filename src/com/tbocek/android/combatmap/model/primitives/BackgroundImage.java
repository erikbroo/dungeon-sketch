package com.tbocek.android.combatmap.model.primitives;

import java.io.IOException;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.drawable.Drawable;

import com.tbocek.android.combatmap.model.io.MapDataDeserializer;
import com.tbocek.android.combatmap.model.io.MapDataSerializer;

public class BackgroundImage {
	private PointF mOriginWorldSpace;
	private float mWidthWorldSpace = 1;
	private float mHeightWorldSpace = 1;

	private Drawable mDrawable;
	
	public BackgroundImage(Drawable drawable) {
		mDrawable = drawable;
	}
	
	public void setLocation(PointF location) {
		mOriginWorldSpace = location;
	}
	
	public void draw(Canvas c) {
		mDrawable.setBounds((int)mOriginWorldSpace.x, (int)mOriginWorldSpace.y, (int)(mOriginWorldSpace.x + mWidthWorldSpace), (int)(mOriginWorldSpace.y + mHeightWorldSpace));
		mDrawable.draw(c);
	}

	public BoundingRectangle getBoundingRectangle() {
		PointF p1 = mOriginWorldSpace;
		PointF p2 = new PointF(mOriginWorldSpace.x + mWidthWorldSpace, mOriginWorldSpace.y + mHeightWorldSpace);
		return new BoundingRectangle(p1, p2);
	}
}
