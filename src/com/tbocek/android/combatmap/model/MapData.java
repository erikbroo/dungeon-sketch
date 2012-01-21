package com.tbocek.android.combatmap.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import android.view.MenuItem;

import com.tbocek.android.combatmap.TokenDatabase;
import com.tbocek.android.combatmap.model.io.MapDataDeserializer;
import com.tbocek.android.combatmap.model.io.MapDataSerializer;
import com.tbocek.android.combatmap.model.primitives.BoundingRectangle;
import com.tbocek.android.combatmap.model.primitives.CoordinateTransformer;

/**
 * This is a data class that collects everything that makes up the current map
 * state, including drawing, tokens, and the current view transformation so that
 * this can all be stored independently of the view.
 * @author Tim Bocek
 *
 */
public final class MapData {

	/**
	 * Version level of this map data, used for saving/loading.
	 */
	private static final int MAP_DATA_VERSION = 0;

	/**
	 * Initial zoom level of newly created maps.  Corresponds to 1 square = 
	 * 64 pixels.  Not density independent.
	 */
	private static final int INITIAL_ZOOM = 64;
	
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
     * Clears the currently loaded map data, forcing a reload next time map
     * data is needed.
     */
    public static void invalidate() {
    	instance = null;
    }
    
    /**
     * @return True if an instance of MapData has already been created.
     */
    public static boolean hasValidInstance() {
    	return instance != null;
    }
    
    /**
     * Loads the map data from an input stream.
     * @param input The stream to read from.
     * @param tokens Token database to use when creating tokens.
     * @throws IOException On read error.
     * @throws ClassNotFoundException On deserialization error.
     */
    public static void loadFromStream(
    		final InputStream input, TokenDatabase tokens)
    		throws IOException, ClassNotFoundException {
        InputStreamReader inReader = new InputStreamReader(input);
        BufferedReader reader = new BufferedReader(inReader);
        MapDataDeserializer s = new MapDataDeserializer(reader);
        try {
        	instance = MapData.deserialize(s, tokens);
        } finally {
	        reader.close();
	        inReader.close();
        }
    }

    /**
     * Saves the map data to a stream.
     * @param output The stream to write to.
     * @throws IOException On write error.
     */
    public static void saveToStream(final OutputStream output)
    		throws IOException {
        OutputStreamWriter outWriter = new OutputStreamWriter(output);
        BufferedWriter writer = new BufferedWriter(outWriter);
        MapDataSerializer s = new MapDataSerializer(writer);
        try {
        	instance.serialize(s);
        } finally {
	        writer.close();
	        outWriter.close();
    	}
    }
    
	/**
	 * Creates, populates, and returns a new MapData object from the given
	 * deserialization stream.
	 * @param s The stream to read from.
	 * @param tokens Token database to load tokens from.
	 * @return The created map data.
	 * @throws IOException On deserialization error.
	 */
	public static MapData deserialize(
			MapDataDeserializer s, TokenDatabase tokens) throws IOException {
		@SuppressWarnings("unused")
		int mapDataVersion = s.readInt();
		MapData data = new MapData();
		data.mGrid = Grid.deserialize(s);
		data.mTransformer = CoordinateTransformer.deserialize(s);
		data.mTokens.deserialize(s, tokens);
		data.mBackgroundLines.deserialize(s);
		data.mBackgroundFogOfWar.deserialize(s);
		data.mGmNoteLines.deserialize(s);
		data.mGmNotesFogOfWar.deserialize(s);
		data.mAnnotationLines.deserialize(s);
		return data;
	}

    
    /**
     * Transformation from world space to screen space.
     */
    private CoordinateTransformer mTransformer =
    	new CoordinateTransformer(0, 0, INITIAL_ZOOM);

    /**
     * Command history object to use for the background and associated fog of
     * war.
     */
    private CommandHistory mBackgroundCommandHistory =
    		new CommandHistory();

    /**
     * Command history to use for the annotations.
     */
    private CommandHistory mAnntationCommandHistory =
		    new CommandHistory();

    /**
     * Command history to use for the GM notes and associated fog of war.
     */
    private CommandHistory mGmNotesCommandHistory =
    		new CommandHistory();
    
    /**
     * Command history to use for combat tokens.
     */
    private CommandHistory mTokenCollectionCommandHistory = 
    		new CommandHistory();

    /**
     * Background lines.
     */
    private LineCollection mBackgroundLines =
    		new LineCollection(mBackgroundCommandHistory);

    /**
     * Lines that represent the fog of war.
     */
    private LineCollection mBackgroundFogOfWar =
    		new LineCollection(mBackgroundCommandHistory);

    /**
     * Annotation lines.
     */
    private LineCollection mAnnotationLines =
    		new LineCollection(mAnntationCommandHistory);

    /**
     * Notes for the GM that are not visible when combat is occurring.
     */
    private LineCollection mGmNoteLines =
    	new LineCollection(mGmNotesCommandHistory);
    
    /**
     * Lines that represent the fog of war.
     */
    private LineCollection mGmNotesFogOfWar =
    		new LineCollection(mGmNotesCommandHistory);

    /**
     * Tokens that have been placed on the map.
     */
    private TokenCollection mTokens =
    		new TokenCollection(mTokenCollectionCommandHistory);

    /**
     * The grid to draw.
     */
    private Grid mGrid = new RectangularGrid();

	
    /**
     * Private constructor - singleton pattern.
     */
    private MapData() {

    }

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
	public LineCollection getBackgroundFogOfWar() {
		return mBackgroundFogOfWar;
	}
	
	/**
	 * @return the fog of war lines.
	 */
	public LineCollection getGmNotesFogOfWar() {
		return mGmNotesFogOfWar;
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
		return mTokens;
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
	
	/**
	 * Saves the entire MapData to the given serialization stream.
	 * @param s The stream to save to.
	 * @throws IOException On serialization error.
	 */
	public void serialize(MapDataSerializer s) throws IOException {
		s.serializeInt(MAP_DATA_VERSION);
		this.mGrid.serialize(s);
		this.mTransformer.serialize(s);
		this.mTokens.serialize(s);
		this.mBackgroundLines.serialize(s);
		this.mBackgroundFogOfWar.serialize(s);
		this.mGmNoteLines.serialize(s);
		this.mGmNotesFogOfWar.serialize(s);
		this.mAnnotationLines.serialize(s);
	}
	

	/**
	 * @return the transformer
	 */
	public CoordinateTransformer getWorldSpaceTransformer() {
		return mTransformer;
	}
}