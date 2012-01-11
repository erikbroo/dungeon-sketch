package com.tbocek.android.combatmap.model;

import java.io.IOException;

import com.tbocek.android.combatmap.model.io.MapDataDeserializer;
import com.tbocek.android.combatmap.model.io.MapDataSerializer;

import android.graphics.Color;

/**
 * Stores a color scheme to use when drawing the grid.
 * @author Tim Bocek
 *
 */
public final class GridColorScheme {

    // BUILT-IN COLOR SCHEMES

    /**
     * Grey on white.
     */
    public static final GridColorScheme STANDARD =
    	new GridColorScheme(Color.WHITE, Color.rgb(200, 200, 200), false);

    /**
     * Green on light yellow, a classic graph paper look.
     */
    public static final GridColorScheme GRAPH_PAPER =
    	new GridColorScheme(Color.rgb(248, 255, 180), Color.rgb(195, 255, 114), false);

    /**
     * Dark green on light green.
     */
    public static final GridColorScheme GRASS =
    	new GridColorScheme(Color.rgb(63, 172, 41), Color.rgb(11, 121, 34), false);


    /**
     * Light blue on white.
     */
    public static final GridColorScheme ICE =
    	new GridColorScheme(Color.WHITE, Color.rgb(160, 160, 255), false);

    /**
     * Dark blue on dark green.
     */
    public static final GridColorScheme FOREST =
    	new GridColorScheme(Color.rgb(0, 128, 0), Color.rgb(0, 0, 100), true);

    /**
     * Black on dark blue.
     */
    public static final GridColorScheme NIGHT =
    	new GridColorScheme(Color.rgb(0, 0, 102), Color.rgb(0, 0, 0), true);

    /**
     * Dark red on grey.
     */
    public static final GridColorScheme DUNGEON =
    	new GridColorScheme(Color.rgb(64, 64, 64), Color.rgb(64, 0, 0), true);

    /**
     * Light blue on black.
     */
    public static final GridColorScheme HOLOGRAM =
    	new GridColorScheme(Color.rgb(0, 0, 0), Color.rgb(41, 162, 255), true);

    /**
     * Green on black.
     */
    public static final GridColorScheme CONSOLE =
    	new GridColorScheme(Color.rgb(0, 0, 0), Color.GREEN, true);

    /**
     * Given the name of a color scheme, returns the scheme represented by that
     * name.  If the scheme is not found, returns the standard grey-on-white
     * color scheme.
     * @param name The name of the scheme to use.
     * @return The color scheme.
     */
    public static GridColorScheme fromNamedScheme(final String name) {
        if (name.equals("graphpaper")) {
        	return GRAPH_PAPER;
        }
        if (name.equals("grass")) {
        	return GRASS;
        }
        if (name.equals("ice")) {
        	return ICE;
        }
        if (name.equals("forest")) {
        	return FOREST;
        }
        if (name.equals("night")) {
        	return NIGHT;
        }
        if (name.equals("dungeon")) {
        	return DUNGEON;
        }
        if (name.equals("hologram")) {
        	return HOLOGRAM;
        }
        if (name.equals("console")) {
        	return CONSOLE;
        }
        return STANDARD;
    }

    /**
     * The color to draw in the background.
     */
    private int mBackgroundColor;

    /**
     * The color to draw grid lines with.
     */
    private int mLineColor;

    /**
     * Whether the color scheme has a dark background.
     */
    private boolean mIsDark;

    /**
     * Constructor.
     * @param backgroundColor The color to draw in the background.
     * @param lineColor The color to draw grid lines with.
     * @param isDark Whether the grid should request that dark background
     * 		versions of tokens be drawn.
     */
    public GridColorScheme(final int backgroundColor, final int lineColor,
    		final boolean isDark) {
        this.mBackgroundColor = backgroundColor;
        this.mLineColor = lineColor;
        this.mIsDark = isDark;
    }

    /**
     * @return The color to draw in the background.
     */
    int getBackgroundColor() {
        return mBackgroundColor;
    }

    /**
     * @return The color to draw grid lines with.
     */
    int getLineColor() {
        return mLineColor;
    }

    /**
     * @return Whether the grid has a dark background.
     */
    boolean isDark() {
    	return mIsDark;
    }
    
    public void serialize(MapDataSerializer s) throws IOException {
    	s.startObject();
    	s.serializeInt(this.mBackgroundColor);
    	s.serializeInt(this.mLineColor);
    	s.serializeBoolean(this.mIsDark);
    	s.endObject();
    }

	public static GridColorScheme deserialize(MapDataDeserializer s) throws IOException {
		s.expectObjectStart();
		int bkg = s.readInt();
		int line = s.readInt();
		boolean dark = s.readBoolean();
		s.expectObjectEnd();
		return new GridColorScheme(bkg, line, dark);
	}
}
