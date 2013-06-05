package com.tbocek.android.combatmap.model.primitives;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;

import com.tbocek.android.combatmap.model.io.MapDataDeserializer;
import com.tbocek.android.combatmap.model.io.MapDataSerializer;

/**
 * Represents a short piece of text on the combat map.
 * 
 * @author Tim
 * 
 */
public class Text extends Shape {

    /**
     * Global flag to control whether bounding boxes should be drawn around the
     * text. This will generally be active when text is explicitly being
     * manipulated.
     */
    private static boolean drawBoundingBoxes;

    /**
     * Short character string that is the type of the shape.
     */
    public static final String SHAPE_TYPE = "txt";

    /**
     * Whether this text object has a pending erase operation.
     */
    private boolean mErased;

    /**
     * Location of the lower left hand corner of the text.
     */
    private PointF mLocation;

    /**
     * Contents of the text field.
     */
    private String mText;

    /**
     * Text size, in world space. 1 point = 1 grid space, although this is not
     * rescaled to account for later changes in grid size.
     */
    private float mTextSize;

    /**
     * Sets whether to draw bounding boxes around every text object.
     * 
     * @param value
     *            Whether to draw the boxes.
     */
    public static void shouldDrawBoundingBoxes(boolean value) {
        drawBoundingBoxes = value;
    }

    /**
     * HACK: Ctor for deserialization ONLY!!! The bounding rectangle in
     * particular MUST be manually set!!!
     * 
     * @param color
     *            Color of the text object.
     * @param strokeWidth
     *            Stroke width of the text object.
     */
    Text(int color, float strokeWidth) {
        this.setColor(color);
        this.setWidth(strokeWidth);
    }

    /**
     * Constructor.
     * 
     * @param text
     *            The contents of the text object.
     * @param size
     *            Font size for the text object.
     * @param color
     *            Color of the text.
     * @param strokeWidth
     *            Stroke width to use (currently ignored, might bold the text
     *            later).
     * @param location
     *            Location of the lower left hand corner of the text.
     * @param transform
     *            Coordinate transformer from world to screen space. This is
     *            only used to determine the bounding box, and is not saved.
     */
    public Text(String text, float size, int color, float strokeWidth,
            PointF location, CoordinateTransformer transform) {
        this.mText = text;
        this.mTextSize = size;
        this.setColor(color);
        this.setWidth(strokeWidth);

        this.mLocation = location;

        // Compute the bounding rectangle.
        // To do this, we need to create the Paint object so we know the size
        // of the text.
        this.ensurePaintCreated();
        this.getPaint().setTextSize(this.mTextSize);

        Rect bounds = new Rect();
        this.getPaint().getTextBounds(this.mText, 0, this.mText.length(),
                bounds);
        this.getBoundingRectangle().updateBounds(location);
        this.getBoundingRectangle().updateBounds(
                new PointF(location.x + bounds.width(), location.y
                        - bounds.height()));
    }

    /**
     * Copy constructor.
     * 
     * @param copyFrom
     *            Text object to copy parameters from.
     */
    public Text(Text copyFrom) {
        this.mText = copyFrom.mText;
        this.mTextSize = copyFrom.mTextSize;
        this.setColor(copyFrom.getColor());
        this.setWidth(copyFrom.getWidth());
        this.getBoundingRectangle().clear();
        this.getBoundingRectangle().updateBounds(
                copyFrom.getBoundingRectangle());
        this.mLocation = new PointF(copyFrom.mLocation.x, copyFrom.mLocation.y);
    }

    @Override
    public void addPoint(PointF p) {
        throw new RuntimeException("Adding point to text not supported.");
    }

    @Override
    public boolean contains(PointF p) {
        return this.getBoundingRectangle().contains(p);
    }

    @Override
    protected Path createPath() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void draw(final Canvas c) {
        Paint p = this.getPaint();
        p.setTextSize(this.mTextSize);
        if (Text.drawBoundingBoxes) {
            this.getPaint().setStyle(Style.STROKE);
            c.drawRect(this.getBoundingRectangle().toRectF(), this.getPaint());
            p.setStyle(Style.FILL);
        }
        c.drawText(this.mText, this.mLocation.x, this.mLocation.y,
                this.getPaint());
    }

    @Override
    public void erase(PointF center, float radius) {
        if (this.getBoundingRectangle().intersectsWithCircle(center, radius)) {
            this.mErased = true;
        }
    }

    /**
     * @return The location of the lower left hand corner of the text.
     */
    public PointF getLocation() {
        return this.mLocation;
    }

    @Override
    protected Shape getMovedShape(float deltaX, float deltaY) {
        Text t = new Text(this);

        t.mLocation.x += deltaX;
        t.mLocation.y += deltaY;
        t.getBoundingRectangle().move(deltaX, deltaY);

        return t;
    }

    /**
     * @return The contents of the text object.
     */
    public String getText() {
        return this.mText;
    }

    /**
     * @return The size of the text in the text object.
     */
    public float getTextSize() {
        return this.mTextSize;
    }

    @Override
    public boolean isValid() {
        return this.mText != null && this.mLocation != null;
    }

    @Override
    public boolean needsOptimization() {
        // TODO Auto-generated method stub
        return this.mErased;
    }

    @Override
    public List<Shape> removeErasedPoints() {
        List<Shape> ret = new ArrayList<Shape>();
        if (!this.mErased) {
            ret.add(this);
        } else {
            this.mErased = false;
        }
        return ret;
    }

    @Override
    public void serialize(MapDataSerializer s) throws IOException {
        this.serializeBase(s, SHAPE_TYPE);

        s.startObject();
        s.serializeString(this.mText);
        s.serializeFloat(this.mTextSize);
        s.serializeFloat(this.mLocation.x);
        s.serializeFloat(this.mLocation.y);
        s.endObject();
    }

    @Override
    protected void shapeSpecificDeserialize(MapDataDeserializer s)
            throws IOException {
        s.expectObjectStart();
        this.mText = s.readString();
        this.mTextSize = s.readFloat();
        this.mLocation = new PointF();
        this.mLocation.x = s.readFloat();
        this.mLocation.y = s.readFloat();
        s.expectObjectEnd();
    }

    @Override
    public boolean shouldDrawBelowGrid() {
        return false; // Text should never draw below the grid.
    }
}
