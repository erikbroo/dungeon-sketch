package com.tbocek.android.combatmap.graphicscore;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;

/**
 * This is a data class that collects everything that makes up the current map state,
 * including drawing, tokens, and the current view transformation so that this can all
 * be stored independently of the view.
 * @author Tim
 *
 */
public class MapData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3121845340089752312L;
	
	private MapData() {
		
	}
	private static MapData instance;
	
	public static MapData getInstance() {
		if (instance == null) {
			clear();
		}
		return instance;
	}
	
	public static void clear() {
		instance = new MapData();
	}
	
	public static void loadFromStream(InputStream input) throws IOException, ClassNotFoundException {
		ObjectInputStream objectIn = new ObjectInputStream(input);
		instance = (MapData) objectIn.readObject();
		objectIn.close();
	}
	
	public static void saveToStream(OutputStream output) throws IOException {
		ObjectOutputStream objectOut = new ObjectOutputStream(output);
		objectOut.writeObject(instance);
		objectOut.close();
	}
	
	public CoordinateTransformer transformer = new CoordinateTransformer(0,0,64);
	public LineCollection mBackgroundLines = new LineCollection();
	public LineCollection mAnnotationLines = new LineCollection();
	public TokenCollection tokens = new TokenCollection();
	public Grid grid = new RectangularGrid();
	
	public BoundingRectangle getBoundingRectangle() {
		BoundingRectangle r = new BoundingRectangle();
		
		r.updateBounds(mBackgroundLines.getBoundingRectangle());
		r.updateBounds(mAnnotationLines.getBoundingRectangle());
		
		for (BaseToken t: tokens.getTokens()) {
			r.updateBounds(t.getBoundingRectangle());
		}
		
		return r;
	}
	
	public void zoomToFit(int widthPixels, int heightPixels) {
		BoundingRectangle r = getBoundingRectangle();
		
		float scaleFactorX = widthPixels / r.getWidth();
		float scaleFactorY = heightPixels/ r.getHeight();
		//Find the optimal scale factor, and add a border.
		float scaleFactor = Math.min(scaleFactorX, scaleFactorY)/ 1.01f;
		
		this.transformer.setZoom(scaleFactor);
		this.transformer.setOriginInWorldSpace(r.getXMin(), r.getYMin());
	}
	
	public boolean hasData() {
		return !mBackgroundLines.isEmpty() && !mAnnotationLines.isEmpty() && !tokens.isEmpty();
	}
}