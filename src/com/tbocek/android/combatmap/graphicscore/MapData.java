package com.tbocek.android.combatmap.graphicscore;

/**
 * This is a data class that collects everything that makes up the current map state,
 * including drawing, tokens, and the current view transformation so that this can all
 * be stored independently of the view.
 * @author Tim
 *
 */
public class MapData {
	public CoordinateTransformer transformer = new CoordinateTransformer(0,0,64);
	public LineCollection mBackgroundLines = new LineCollection();
	public LineCollection mAnnotationLines = new LineCollection();
	public TokenCollection tokens = new TokenCollection();
	public Grid grid = new Grid();
}