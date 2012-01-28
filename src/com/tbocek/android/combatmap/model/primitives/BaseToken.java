package com.tbocek.android.combatmap.model.primitives;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import com.tbocek.android.combatmap.TokenDatabase;
import com.tbocek.android.combatmap.model.io.MapDataDeserializer;
import com.tbocek.android.combatmap.model.io.MapDataSerializer;
import com.tbocek.android.combatmap.view.TagListView;

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
     * Stroke width to use for the selection border.
     */
    private static final float SELECTION_STROKE_WIDTH = 4;

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
	 * Checks whether all tokens in the given collection are bloodied.
	 * @param tokens Collection of tokens to check.
	 * @return True if every token is bloodied, False if at least one isn't.
	 */
	public static boolean allBloodied(Collection<BaseToken> tokens) {
		for (BaseToken t: tokens) {
			if (!t.isBloodied()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Checks whether all tokens in the given collection are the same size.
	 * @param tokens The list of tokens to check.
	 * @return True if the tokens are the same size, False otherwise.
	 */
	public static boolean areTokenSizesSame(Collection<BaseToken> tokens) {
		float commonSize = Float.NaN;
		for (BaseToken t: tokens) {
			if (commonSize == Float.NaN) {
				commonSize = t.getSize();
			} else if (Math.abs(commonSize - t.getSize()) 
					> Util.FP_COMPARE_ERROR) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Checks whether all tokens in the collection have the same custom border.
	 * @param tokens The collection to check.
	 * @return True if all tokens have no custom border or the same custom
	 * 		border.  False if custom border options differ in the collection.
	 */
	public static boolean areTokenBordersSame(Collection<BaseToken> tokens) {
		boolean seenOne = false;
		boolean hasCustomBorder = false;
		int customBorderColor = 0;
		for (BaseToken t: tokens) {
			if (!seenOne) {
				seenOne = true;
				hasCustomBorder = t.mHasCustomBorder;
				customBorderColor = t.getCustomBorderColor();
			} else if (t.mHasCustomBorder != hasCustomBorder 
					|| t.getCustomBorderColor() == customBorderColor) {
				return false;
			}
		}
		return true;
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
    private boolean mBloodied;
    
    /**
     * Cached paint object for the custom token border.
     */
    private transient Paint mCachedCustomBorderPaint;
    
    /**
     * Whether this token instance has been given a custom border.
     */
    private boolean mHasCustomBorder;
    
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
     * OPTIMIZATION: This token's sort order.  See the optimization note on
     * mCachedTokenId;
     */
    private String mCachedSortOrder;
    
    /**
     * Whether this token is part of a selection.
     */
    private boolean mSelected;

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
    		mCachedCustomBorderPaint.setColor(getCustomBorderColor());
    		
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
    protected abstract void drawImpl(Canvas c, float x, float y, float radius,
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
    protected abstract void drawBloodiedImpl(Canvas c, float x, float y,
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
            drawBloodiedImpl(c, center.x, center.y, radius, isManipulatable);
        } else {
            drawImpl(c, center.x, center.y, radius, darkBackground,
            		isManipulatable);
        }
        
        if (mHasCustomBorder) {
        	 c.drawCircle(center.x, center.y, radius, getCustomBorderPaint());
        }
        
        if (mSelected) {
        	// TODO: Cache this.
    		Paint selectPaint = new Paint();
    		selectPaint.setStrokeWidth(SELECTION_STROKE_WIDTH);
    		selectPaint.setColor(TagListView.DRAG_HIGHLIGHT_COLOR);
    		selectPaint.setStyle(Style.STROKE);
    		c.drawCircle(center.x, center.y, radius + SELECTION_STROKE_WIDTH, 
    				selectPaint);
        }
    }
    

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
    public void draw(Canvas c, float x, float y, float radius,
    		final boolean darkBackground, boolean isManipulatable) {
        if (isBloodied()) {
            drawBloodiedImpl(c, x, y, radius, isManipulatable);
        } else {
            drawImpl(c, x, y, radius, darkBackground,
            		isManipulatable);
        }
        
        if (mHasCustomBorder) {
        	 c.drawCircle(x, y, radius, getCustomBorderPaint());
        }
        
        if (mSelected) {
        	// TODO: Cache this.
    		Paint selectPaint = new Paint();
    		selectPaint.setStrokeWidth(SELECTION_STROKE_WIDTH);
    		selectPaint.setColor(TagListView.DRAG_HIGHLIGHT_COLOR);
    		selectPaint.setStyle(Style.STROKE);
    		c.drawCircle(x, y, radius + SELECTION_STROKE_WIDTH, 
    				selectPaint);
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
     * Sets whether this token is currently selected.
     * @param selected Whether token is selected.
     */
    public final void setSelected(final boolean selected) {
    	this.mSelected = selected;
    }
    
    /**
     * @return True if this token is part of a selection.
     */
    public final boolean isSelected() {
    	return mSelected;
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
            CONCAT_BUFFER.append(this.getClass().getSimpleName());
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
    
    /**
     * Gets a global sort order incorporating this token's class and a
     * class-specific sort order.  Tokens are sorted first by class, then by
     * an order specified by each class.
     * @return The sort order.
     */
    public final String getSortOrder() {
        if (mCachedSortOrder == null) {
            CONCAT_BUFFER.setLength(0);
            CONCAT_BUFFER.append(this.getClass().getSimpleName());
            CONCAT_BUFFER.append(getTokenClassSpecificSortOrder());
            mCachedSortOrder = CONCAT_BUFFER.toString();
        }
        return mCachedSortOrder;
    }
    
    /**
     * @return A sort order within this token class.  By default, it is the same
     * 		as the class specific ID, but subclasses can override this.
     */
    protected String getTokenClassSpecificSortOrder() {
    	return getTokenClassSpecificId();
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
    	s.serializeInt(this.getCustomBorderColor());
    	s.serializeBoolean(this.mBloodied);
    	s.endObject();
    }

	/**
	 * @return The color of this token's custom border.
	 */
	public int getCustomBorderColor() {
		return mCustomBorderColor;
	}

	/**
	 * @return Whether this token uses a custom border.
	 */
	public boolean hasCustomBorder() {
		return mHasCustomBorder;
	}
}