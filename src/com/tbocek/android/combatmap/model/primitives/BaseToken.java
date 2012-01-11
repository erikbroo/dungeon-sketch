package com.tbocek.android.combatmap.model.primitives;

import java.io.IOException;
import java.util.Set;

import com.tbocek.android.combatmap.TokenDatabase;
import com.tbocek.android.combatmap.model.io.MapDataDeserializer;
import com.tbocek.android.combatmap.model.io.MapDataSerializer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;

/**
 * Base class for token representing entities in combat.
 * @author Tim Bocek
 *
 */
public abstract class BaseToken  {

    /**
     * Tweak to the token's size so that it doesn't totally inscribe a grid
     * square.
     */
    private static final float TOKEN_SIZE_TWEAK = 0.9f;
    
    /**
     * Stroke width to use for the custom token border if one is specified.
     */
    private static final float CUSTOM_BORDER_STROKE_WIDTH = 3;

    /**
     * OPTIMIZATION: Shared, preallocated StringBuffer used to concatenate
     * strings for token IDs.
     */
    private static final StringBuffer CONCAT_BUFFER = new StringBuffer(1024);
    
    /**
     * Creates and loads a token from the given deserialization stream.
     * @param s Deserialization object.
     * @param tokenDatabase Token database for token creation.
     * @return The newly created token.
     * @throws IOException On deserialization error.
     */
	public static BaseToken deserialize(MapDataDeserializer s,
			TokenDatabase tokenDatabase) throws IOException {
		s.expectObjectStart();
		String tokenId = s.readString();
		BaseToken t = tokenDatabase.createToken(tokenId);
		t.mSize = s.readFloat();
		t.mLocation.x = s.readFloat();
		t.mLocation.y = s.readFloat();
		t.mHasCustomBorder = s.readBoolean();
		t.mCustomBorderColor = s.readInt();
		t.mBloodied = s.readBoolean();
		s.expectObjectEnd();
		return t;
	}
	
    /**
     * The token's location in grid space.
     */
    private PointF mLocation = new PointF(0, 0);

    /**
     * The token's diameter in grid space.
     */
    private float mSize = 1.0f;

    /**
     * Whether the token is bloodied.
     */
    private boolean mBloodied = false;
    
    /**
     * Cached paint object for the custom token border.
     */
    private transient Paint mCachedCustomBorderPaint = null;
    
    /**
     * Whether this token instance has been given a custom border.
     */
    private boolean mHasCustomBorder = false;
    
    /**
     * The color of the custom border if one is being used.
     */
    private int mCustomBorderColor;
    
    /**
     * OPTIMIZATION: This token's ID.  Because the token ID is computed using
     * relatively expensive operations, is needed frequently, and never changes
     * throughout the object's lifetime, it can be cached here.
     */
    private String mCachedTokenId;

    /**
     * Sets a custom border color for the token.
     * @param color Color of the custom border.
     */
    public void setCustomBorder(int color) {
    	mHasCustomBorder = true;
    	mCustomBorderColor = color;
    	mCachedCustomBorderPaint = null;
    }
    
    /**
     * Clears the custom border from the token.
     */
    public void clearCustomBorderColor() {
    	mHasCustomBorder = false;
    	mCachedCustomBorderPaint = null;
    }
    
    /**
     * Returns a paint object for the custom border, or null if no custom border
     * is requested for this token.  Will create the paint object if not valid,
     * or returns a cached paint object.
     * @return The paint object.
     */
    protected Paint getCustomBorderPaint() {
    	if (mCachedCustomBorderPaint == null && mHasCustomBorder) {
    		mCachedCustomBorderPaint = new Paint();
    		mCachedCustomBorderPaint.setStrokeWidth(CUSTOM_BORDER_STROKE_WIDTH);
    		mCachedCustomBorderPaint.setColor(mCustomBorderColor);
    		
    		mCachedCustomBorderPaint.setStyle(Style.STROKE);
    		
    	}
    	return mCachedCustomBorderPaint;
    }
    
    /**
     * Moves this token the given distance relative to its current location.
     * In grid space.
     * @param distanceX Distance to move in X coordinate in grid space.
     * @param distanceY Distance to move in Y coordinate in grid space.
     */
    public final void move(final float distanceX, final float distanceY) {
        setLocation(new PointF(
                getLocation().x - distanceX, getLocation().y - distanceY));
    }

    /**
     * @return A copy of this token.
     */
    public abstract BaseToken clone();

    /**
     * Draw the token at the given coordinates and size.
     * Everything in screen space.
     * @param c Canvas to draw on.
     * @param x The x coordinate in screen space to draw the token at.
     * @param y The y coordinate in screen space to draw the token at.
     * @param radius The radius of the token in screen space.
     * @param darkBackground Whether the token is drawn against a dark
     * 		background.  The token can try to make its self more visible in
     * 		this case.
     * @param isManipulatable Whether the token can currently be manipulated.
     */
    public abstract void draw(Canvas c, float x, float y, float radius,
    		final boolean darkBackground, boolean isManipulatable);

    /**
     * Draw a bloodied version of the token at the given coordinates and size.
     * Everything in screen space.
     * @param c Canvas to draw on.
     * @param x The x coordinate in screen space to draw the token at.
     * @param y The y coordinate in screen space to draw the token at.
     * @param radius The radius of the token in screen space.
     * @param isManipulatable Whether the token can currently be manipulated.
     */
    public abstract void drawBloodied(Canvas c, float x, float y,
            float radius, boolean isManipulatable);

    /**
     * Draw a ghost version of the token at the given coordinates and size.
     * Everything in screen space.
     * @param c Canvas to draw on.
     * @param x The x coordinate in screen space to draw the token at.
     * @param y The y coordinate in screen space to draw the token at.
     * @param radius The radius of the token in screen space.
     */
    protected abstract void drawGhost(Canvas c, float x, float y, float radius);

    /**
     * Draw a ghost version of this token on the given canvas at the given
     * point in grid space.
     * @param c The canvas to draw the ghost on.
     * @param transformer Transformer between grid space and screen space.
     * @param ghostPoint The point to draw the ghost at.
     */
    public final void drawGhost(
            final Canvas c, final CoordinateTransformer transformer,
            final PointF ghostPoint) {
        PointF center = transformer.worldSpaceToScreenSpace(ghostPoint);
        float radius = transformer.worldSpaceToScreenSpace(
                this.getSize() * TOKEN_SIZE_TWEAK / 2);
        drawGhost(c, center.x, center.y, radius);
    }

    /**
     * Draws this token in the correct position on the given canvas.
     * @param c The canvas to draw on.
     * @param transformer Grid space to screen space transformer.
     * @param darkBackground Whether the token is drawn against a dark
     * 		background.  The token can try to make its self more visible in
     * 		this case.
     * @param isManipulatable Whether the token can currently be manipulated.
     */
    public final void drawInPosition(
            final Canvas c, final CoordinateTransformer transformer,
            final boolean darkBackground, boolean isManipulatable) {
        PointF center = transformer.worldSpaceToScreenSpace(getLocation());
        float radius = transformer.worldSpaceToScreenSpace(
                this.getSize() * TOKEN_SIZE_TWEAK / 2);

        if (isBloodied()) {
            drawBloodied(c, center.x, center.y, radius, isManipulatable);
        } else {
            draw(c, center.x, center.y, radius, darkBackground,
            		isManipulatable);
        }
        
        if (mHasCustomBorder) {
        	 c.drawCircle(center.x, center.y, radius, getCustomBorderPaint());
        }
    }

    /**
     * Marks this token as bloodied.
     * @param bloodied Whether the token should be set to bloodied.
     */
    public final void setBloodied(final boolean bloodied) {
        this.mBloodied = bloodied;
    }

    /**
     * @return True if the token is currently bloodied.
     */
    public final boolean isBloodied() {
        return mBloodied;
    }

    /**
     * Sets the location of the token in grid space.
     * @param location The new location in grid space.
     */
    public final void setLocation(final PointF location) {
        this.mLocation = location;
    }

    /**
     * @return The location of the token in grid space.
     */
    public final PointF getLocation() {
        return mLocation;
    }

    /**
     * Sets the diameter of the token in grid space.
     * @param size The new diameter in grid space.
     */
    public final void setSize(final float size) {
        this.mSize = size;
    }

    /**
     * @return The radius of this token in grid space
     */
    public final float getSize() {
        return mSize;
    }

    /**
     * @return A rectangle that bounds the circle that this token draws as.
     */
    public final BoundingRectangle getBoundingRectangle() {
        BoundingRectangle r = new BoundingRectangle();
        r.updateBounds(
                new PointF(mLocation.x - mSize / 2, mLocation.y - mSize / 2));
        r.updateBounds(
                new PointF(mLocation.x + mSize / 2, mLocation.y + mSize / 2));
        return r;
    }


    /**
     * Gets a unique identifier incorporating the token's type and a further
     * differentiator depending on the type.
     * @return The token ID.
     */
    public final String getTokenId() {
        if (mCachedTokenId == null) {
            CONCAT_BUFFER.setLength(0);
            CONCAT_BUFFER.append(this.getClass().getName());
            CONCAT_BUFFER.append(getTokenClassSpecificId());
            mCachedTokenId = CONCAT_BUFFER.toString();
        }
        return mCachedTokenId;
    }

    /**
     * Gets an ID that differentiates this token from others in its class.
     * Subclasses should override this such that tokens that display the same
     * thing return the same ID.  The class its self need not be represented.
     * @return The class-specific part of the ID.
     */
    protected abstract String getTokenClassSpecificId();

    @Override
    public final boolean equals(final Object other) {
        if (this == other) { return true; }
        if (!(other instanceof BaseToken)) { return false; }
        return ((BaseToken) other).getTokenId().equals(getTokenId());
    }

    @Override
    public final int hashCode() {
        return getTokenId().hashCode();
    }

    /**
     * @return A set of tags to apply to this token by default.
     */
    public abstract Set<String> getDefaultTags();

    /**
     * If possible, permanently deletes this token from internal storage.
     * @return True if the token was deleted.
     * @throws IOException If the token was attempted to be deleted but there
     * 		was an error.
     */
    public boolean maybeDeletePermanently() throws IOException {
        return false;
    }

    /**
     * @return True if this is a built-in token, False otherwise.
     */
    public boolean isBuiltIn() {
        return true;
    }

    /**
     * @return True if some expensive action (e.g. disk IO) is needed to load
     * 		the token.  Should return false if no action is needed, or if the
     * 		action is already taken.
     */
    public boolean needsLoad() {
    	return false;
    }

    /**
     * Takes any action needed to load the token.
     */
    public void load() { }
    
    /**
     * Copy attributes from this token to the given token.
     * @param clone Token to copy attributes into.
     * @return The token that was copied into.
     */
    public BaseToken copyAttributesTo(BaseToken clone) {
    	clone.mBloodied = mBloodied;
    	clone.mCustomBorderColor = mCustomBorderColor;
    	clone.mHasCustomBorder = mHasCustomBorder;
    	clone.mLocation = new PointF(mLocation.x, mLocation.y);
    	clone.mSize = mSize;
    	return clone;
    }
    
    /**
     * Saves this token to the given serialization stream.
     * @param s The serialization object to save to.
     * @throws IOException On serialization error.
     */
    public void serialize(MapDataSerializer s) throws IOException {
    	s.startObject();
    	s.serializeString(this.getTokenId());
    	s.serializeFloat(this.mSize);
    	s.serializeFloat(this.mLocation.x);
    	s.serializeFloat(this.mLocation.y);
    	s.serializeBoolean(this.mHasCustomBorder);
    	s.serializeInt(this.mCustomBorderColor);
    	s.serializeBoolean(this.mBloodied);
    	s.endObject();
    }


}