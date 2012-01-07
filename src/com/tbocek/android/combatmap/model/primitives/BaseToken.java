package com.tbocek.android.combatmap.model.primitives;

import java.io.IOException;
import java.io.Serializable;
import java.util.Set;

import com.tbocek.android.combatmap.MapDataDeserializer;
import com.tbocek.android.combatmap.MapDataSerializer;
import com.tbocek.android.combatmap.TokenDatabase;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;

/**
 * Base class for token representing entities in combat.
 * @author Tim Bocek
 *
 */
public abstract class BaseToken implements Serializable {
    /**
     * The class's ID for serialization.
     */
    private static final long serialVersionUID = 9080531944602251588L;

    /**
     * Tweak to the token's size so that it doesn't totally inscribe a grid
     * square.
     */
    private static final float TOKEN_SIZE_TWEAK = 0.9f;

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
     *
     */
    private transient Paint mCachedCustomBorderPaint = null;
    protected boolean mHasCustomBorder = false;
    private int mCustomBorderColor;
    
    public void setCustomBorder(int color) {
    	mHasCustomBorder = true;
    	mCustomBorderColor = color;
    	mCachedCustomBorderPaint = null;
    }
    
    
    public void clearCustomBorderColor() {
    	mHasCustomBorder = false;
    	mCachedCustomBorderPaint = null;
    }
    
    protected Paint getCustomBorderPaint() {
    	if (mCachedCustomBorderPaint == null && mHasCustomBorder) {
    		mCachedCustomBorderPaint = new Paint();
    		mCachedCustomBorderPaint.setStrokeWidth(3);
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
     * @param isManipulatable
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
     * @param isManipulatable
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
     * OPTIMIZATION: Shared, preallocated StringBuffer used to concatenate
     * strings for token IDs.
     */
    private static final StringBuffer CONCAT_BUFFER = new StringBuffer(1024);

    /**
     * OPTIMIZATION: This token's ID.  Because the token ID is computed using
     * relatively expensive operations, is needed frequently, and never changes
     * throughout the object's lifetime, it can be cached here.
     */
    private String cachedTokenId = null;

    /**
     * Gets a unique identifier incorporating the token's type and a further
     * differentiator depending on the type.
     * @return The token ID.
     */
    public final String getTokenId() {
        if (cachedTokenId == null) {
            CONCAT_BUFFER.setLength(0);
            CONCAT_BUFFER.append(this.getClass().getName());
            CONCAT_BUFFER.append(getTokenClassSpecificId());
            cachedTokenId = CONCAT_BUFFER.toString();
        }
        return cachedTokenId;
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
        return ((BaseToken) other).getTokenId() == getTokenId();
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
    
    public BaseToken copyAttributes(BaseToken clone) {
    	clone.mBloodied = mBloodied;
    	clone.mCustomBorderColor = mCustomBorderColor;
    	clone.mHasCustomBorder = mHasCustomBorder;
    	clone.mLocation = new PointF(mLocation.x, mLocation.y);
    	clone.mSize = mSize;
    	return clone;
    }
    
    public void serialize(MapDataSerializer s) throws IOException {
    	s.startArray();
    	s.serializeString(this.getTokenId());
    	s.serializeFloat(this.mSize);
    	s.serializeFloat(this.mLocation.x);
    	s.serializeFloat(this.mLocation.y);
    	s.serializeBoolean(this.mHasCustomBorder);
    	s.serializeInt(this.mCustomBorderColor);
    	s.serializeBoolean(this.mBloodied);
    	s.endArray();
    }


	public static BaseToken deserialize(MapDataDeserializer s,
			TokenDatabase tokenDatabase) throws IOException {
		s.expectArrayStart();
		String tokenId = s.readString();
		BaseToken t = tokenDatabase.createToken(tokenId);
		t.mSize = s.readFloat();
		t.mLocation.x = s.readFloat();
		t.mLocation.y = s.readFloat();
		t.mHasCustomBorder = s.readBoolean();
		t.mCustomBorderColor = s.readInt();
		t.mBloodied = s.readBoolean();
		s.expectArrayEnd();
		return null;
	}
}