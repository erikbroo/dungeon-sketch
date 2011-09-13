package com.tbocek.android.combatmap.graphicscore;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * This is a data class that collects everything that makes up the current map
 * state, including drawing, tokens, and the current view transformation so that
 * this can all be stored independently of the view.
 * @author Tim Bocek
 *
 */
public final class MapData implements Serializable {
    /**
     * ID for serialization.
     */
    private static final long serialVersionUID = -3121845340089752312L;

    /**
     * Private constructor - singleton pattern.
     */
    private MapData() {

    }

    /**
     * The map data currently being managed.
     */
    private static MapData instance;

    /**
     * Gets the current map data instance.
     * @return The map data.
     */
    public static MapData getInstance() {
        if (instance == null) {
            clear();
        }
        return instance;
    }

    /**
     * Clears the map by loading a new instance.
     */
    public static void clear() {
        instance = new MapData();
    }

    /**
     * Loads the map data from an input stream.
     * @param input The stream to read from.
     * @throws IOException On read error.
     * @throws ClassNotFoundException On deserialization error.
     */
    public static void loadFromStream(final InputStream input)
    		throws IOException, ClassNotFoundException {
        ObjectInputStream objectIn = new ObjectInputStream(input);
        instance = (MapData) objectIn.readObject();
        objectIn.close();
    }

    /**
     * Saves the map data to a stream.
     * @param output The stream to write to.
     * @throws IOException On write error.
     */
    public static void saveToStream(final OutputStream output)
    		throws IOException {
        ObjectOutputStream objectOut = new ObjectOutputStream(output);
        objectOut.writeObject(instance);
        objectOut.close();
    }

    /**
     * Transformation from world space to screen space.
     */
    public CoordinateTransformer transformer =
    	new CoordinateTransformer(0, 0, 64);

    private LineCollection.CommandHistory backgroundCommandHistory =
    		new LineCollection.CommandHistory();

    private LineCollection.CommandHistory anntationCommandHistory =
		    new LineCollection.CommandHistory();

    private LineCollection.CommandHistory gmNotesCommandHistory =
    		new LineCollection.CommandHistory();

    /**
     * Background lines.
     */
    private LineCollection mBackgroundLines =
    		new LineCollection(backgroundCommandHistory);

    /**
     * Lines that represent the fog of war.
     */
    private LineCollection mFogOfWar =
    		new LineCollection(backgroundCommandHistory);

    /**
     * Annotation lines.
     */
    private LineCollection mAnnotationLines =
    		new LineCollection(anntationCommandHistory);

    /**
     * Notes for the GM that are not visible when combat is occurring.
     */
    private LineCollection mGmNoteLines =
    	new LineCollection(gmNotesCommandHistory);

    /**
     * Tokens that have been placed on the map.
     */
    private TokenCollection tokens = new TokenCollection();

    /**
     * The grid to draw.
     */
    private Grid mGrid = new RectangularGrid();


    /**
     * Gets a rectangle that encompasses the entire map.
     * @return The bounding rectangle.
     */
    public BoundingRectangle getBoundingRectangle() {
        BoundingRectangle r = new BoundingRectangle();

        r.updateBounds(mBackgroundLines.getBoundingRectangle());
        r.updateBounds(mAnnotationLines.getBoundingRectangle());
        r.updateBounds(getTokens().getBoundingRectangle());

        return r;
    }

    /**
     * Changes the world to screen transformation so that the entire map
     * fits on the screen.
     * TODO: this doesn't work right.
     * @param widthPixels Width of the screen.
     * @param heightPixels Height of the screen.
     */
    public void zoomToFit(final int widthPixels, final int heightPixels) {
        BoundingRectangle r = getBoundingRectangle();

        float scaleFactorX = widthPixels / r.getWidth();
        float scaleFactorY = heightPixels / r.getHeight();
        //Find the optimal scale factor, and add a border.
        float scaleFactor = Math.min(scaleFactorX, scaleFactorY) / 1.01f;

        this.transformer.setZoom(scaleFactor);
        this.transformer.setOriginInWorldSpace(r.getXMin(), r.getYMin());
    }

    /**
     * @return Whether anything has been placed on the map.
     */
    public boolean hasData() {
        return !mBackgroundLines.isEmpty()
        	|| !mAnnotationLines.isEmpty()
        	|| !getTokens().isEmpty();
    }

	/**
	 * @return the backgroundLines
	 */
	public LineCollection getBackgroundLines() {
		return mBackgroundLines;
	}

	/**
	 * @return the fog of war lines.
	 */
	public LineCollection getFogOfWar() {
		return mFogOfWar;
	}

	/**
	 * @return the annotationLines
	 */
	public LineCollection getAnnotationLines() {
		return mAnnotationLines;
	}

	/**
	 * @return Private notes for the GM
	 */
	public LineCollection getGmNoteLines() {
		return mGmNoteLines;
	}

	/**
	 * @return the tokens
	 */
	public TokenCollection getTokens() {
		return tokens;
	}

	/**
	 * @param grid the grid to set
	 */
	public void setGrid(final Grid grid) {
		this.mGrid = grid;
	}

	/**
	 * @return the grid
	 */
	public Grid getGrid() {
		return mGrid;
	}
}