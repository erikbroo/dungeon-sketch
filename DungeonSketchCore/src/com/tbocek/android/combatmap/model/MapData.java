package com.tbocek.android.combatmap.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import android.graphics.PointF;
import android.graphics.RectF;

import com.tbocek.android.combatmap.TokenDatabase;
import com.tbocek.android.combatmap.model.io.MapDataDeserializer;
import com.tbocek.android.combatmap.model.io.MapDataSerializer;
import com.tbocek.android.combatmap.model.primitives.BoundingRectangle;
import com.tbocek.android.combatmap.model.primitives.CoordinateTransformer;

/**
 * This is a data class that collects everything that makes up the current map
 * state, including drawing, tokens, and the current view transformation so that
 * this can all be stored independently of the view.
 * 
 * @author Tim Bocek
 * 
 */
public final class MapData {

    /**
     * Initial zoom level of newly created maps. Corresponds to 1 square = 64
     * pixels. Not density independent.
     */
    private static final int INITIAL_ZOOM = 64;

    /**
     * The map data currently being managed.
     */
    private static MapData instance;

    /**
     * Version level of this map data, used for saving/loading.
     * Version History:
     * 0: Initial Version
     * 1: Added background image collection.
     * 2: Added last tag accessed on the map.
     */
    private static final int MAP_DATA_VERSION = 2;

    /**
     * Command history to use for the annotations.
     */
    private CommandHistory mAnntationCommandHistory = new CommandHistory();

    /**
     * Annotation lines.
     */
    private LineCollection mAnnotationLines = new LineCollection(
            this.mAnntationCommandHistory);

    /**
     * Command history object to use for the background and associated fog of
     * war.
     */
    private CommandHistory mBackgroundCommandHistory = new CommandHistory();

    /**
     * Lines that represent the fog of war.
     */
    private LineCollection mBackgroundFogOfWar = new LineCollection(
            this.mBackgroundCommandHistory);

    /**
     * Collection of background images.
     */
    private BackgroundImageCollection mBackgroundImages =
            new BackgroundImageCollection(this.mBackgroundCommandHistory);

    /**
     * Background lines.
     */
    private LineCollection mBackgroundLines = new LineCollection(
            this.mBackgroundCommandHistory);

    /**
     * Command history to use for the GM notes and associated fog of war.
     */
    private CommandHistory mGmNotesCommandHistory = new CommandHistory();

    /**
     * Notes for the GM that are not visible when combat is occurring.
     */
    private LineCollection mGmNoteLines = new LineCollection(
            this.mGmNotesCommandHistory);

    /**
     * Lines that represent the fog of war.
     */
    private LineCollection mGmNotesFogOfWar = new LineCollection(
            this.mGmNotesCommandHistory);

    /**
     * The grid to draw.
     */
    private Grid mGrid = new Grid();

    /**
     * Whether map attributes such as color scheme and grid type should be
     * updated in response to loading preferences. Set to true if this map is
     * newly loaded.
     */
    private boolean mMapAttributesLocked;
    
    private String mLastTag = TokenDatabase.ALL;

    /**
     * Command history to use for combat tokens.
     */
    private CommandHistory mTokenCollectionCommandHistory =
            new CommandHistory();

    /**
     * Tokens that have been placed on the map.
     */
    private TokenCollection mTokens = new TokenCollection(
            this.mTokenCollectionCommandHistory);

    /**
     * Transformation from world space to screen space.
     */
    private CoordinateTransformer mTransformer = new CoordinateTransformer(0,
            0, INITIAL_ZOOM);

    /**
     * Clears the map by loading a new instance.
     */
    public static void clear() {
        instance = new MapData();
    }

    /**
     * Creates, populates, and returns a new MapData object from the given
     * deserialization stream.
     * 
     * @param s
     *            The stream to read from.
     * @param tokens
     *            Token database to load tokens from.
     * @return The created map data.
     * @throws IOException
     *             On deserialization error.
     */
    public static MapData deserialize(MapDataDeserializer s,
            TokenDatabase tokens) throws IOException {
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
        if (mapDataVersion >= 1) {
            data.mBackgroundImages.deserialize(s);
        }
        if (mapDataVersion >= 2) {
        	data.mLastTag = s.readString();
        }
        return data;
    }

    /**
     * Gets the current map data instance.
     * 
     * @return The map data.
     */
    public static MapData getInstance() {
        if (instance == null) {
            clear();
        }
        return instance;
    }

    /**
     * @return True if an instance of MapData has already been created.
     */
    public static boolean hasValidInstance() {
        return instance != null;
    }

    /**
     * Clears the currently loaded map data, forcing a reload next time map data
     * is needed.
     */
    public static void invalidate() {
        instance = null;
    }

    /**
     * Loads the map data from an input stream.
     * 
     * @param input
     *            The stream to read from.
     * @param tokens
     *            Token database to use when creating tokens.
     * @throws IOException
     *             On read error.
     * @throws ClassNotFoundException
     *             On deserialization error.
     */
    public static void loadFromStream(final InputStream input,
            TokenDatabase tokens) throws IOException, ClassNotFoundException {
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
     * 
     * @param output
     *            The stream to write to.
     * @throws IOException
     *             On write error.
     */
    public static void saveToStream(final OutputStream output)
            throws IOException {
        OutputStreamWriter outWriter = new OutputStreamWriter(output);
        BufferedWriter writer = new BufferedWriter(outWriter);
        MapDataSerializer s = new MapDataSerializer(writer);
        // TODO: Buffer this into memory and then write on a different thread.
        try {
            instance.serialize(s);
        } finally {
            writer.close();
            outWriter.close();
        }
    }

    /**
     * Private constructor - singleton pattern.
     */
    private MapData() {

    }

    /**
     * 
     * @return Whether map attributes are locked.
     */
    public boolean areMapAttributesLocked() {
        return this.mMapAttributesLocked;
    }

    /**
     * @return the annotationLines
     */
    public LineCollection getAnnotationLines() {
        return this.mAnnotationLines;
    }

    /**
     * @return the fog of war lines.
     */
    public LineCollection getBackgroundFogOfWar() {
        return this.mBackgroundFogOfWar;
    }

    /**
     * @return The collection of background images.
     */
    public BackgroundImageCollection getBackgroundImages() {
        return this.mBackgroundImages;
    }

    /**
     * @return the backgroundLines
     */
    public LineCollection getBackgroundLines() {
        return this.mBackgroundLines;
    }

    /**
     * Gets a rectangle that encompasses the entire map.
     * 
     * @return The bounding rectangle.
     */
    public BoundingRectangle getBoundingRectangle() {
        BoundingRectangle r = new BoundingRectangle();

        r.updateBounds(this.mBackgroundLines.getBoundingRectangle());
        r.updateBounds(this.mAnnotationLines.getBoundingRectangle());
        r.updateBounds(this.getTokens().getBoundingRectangle());

        return r;
    }

    /**
     * @return Private notes for the GM
     */
    public LineCollection getGmNoteLines() {
        return this.mGmNoteLines;
    }

    /**
     * @return the fog of war lines.
     */
    public LineCollection getGmNotesFogOfWar() {
        return this.mGmNotesFogOfWar;
    }

    /**
     * @return the grid
     */
    public Grid getGrid() {
        return this.mGrid;
    }

    /**
     * Gets the screen space bounding rectangle of the entire map based on the
     * current screen space transformation.
     * 
     * @param marginsPx
     *            Margin to apply to each edge.
     * @return The bounding rectangle.
     */
    public RectF getScreenSpaceBoundingRect(int marginsPx) {
        BoundingRectangle worldSpaceRect = this.getBoundingRectangle();
        PointF ul =
                this.mTransformer.worldSpaceToScreenSpace(
                        worldSpaceRect.getXMin(), worldSpaceRect.getYMin());
        PointF lr =
                this.mTransformer.worldSpaceToScreenSpace(
                        worldSpaceRect.getXMax(), worldSpaceRect.getYMax());
        return new RectF(ul.x - marginsPx, ul.y - marginsPx, lr.x + marginsPx,
                lr.y + marginsPx);
    }

    /**
     * @return the tokens
     */
    public TokenCollection getTokens() {
        return this.mTokens;
    }

    /**
     * @return the transformer
     */
    public CoordinateTransformer getWorldSpaceTransformer() {
        return this.mTransformer;
    }

    /**
     * @return Whether anything has been placed on the map.
     */
    public boolean hasData() {
        return !this.mBackgroundLines.isEmpty()
                || !this.mAnnotationLines.isEmpty()
                || !this.getTokens().isEmpty();
    }

    /**
     * Saves the entire MapData to the given serialization stream.
     * 
     * @param s
     *            The stream to save to.
     * @throws IOException
     *             On serialization error.
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
        this.mBackgroundImages.serialize(s);
        s.serializeString(mLastTag != null ? mLastTag : TokenDatabase.ALL);
    }

    /**
     * @param grid
     *            the grid to set
     */
    public void setGrid(final Grid grid) {
        this.mGrid = grid;
    }

    /**
     * Sets whether to lock map attributes.
     * 
     * @param locked
     *            True if map attributes are locked, False otherwise.
     */
    public void setMapAttributesLocked(boolean locked) {
        this.mMapAttributesLocked = locked;
    }
    
    public void setLastTag(String lastTag) {
    	mLastTag = lastTag;
    }
    
    public String getLastTag() {
    	return mLastTag;
    }
}