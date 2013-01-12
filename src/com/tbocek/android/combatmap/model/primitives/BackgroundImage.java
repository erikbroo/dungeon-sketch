package com.tbocek.android.combatmap.model.primitives;

import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.tbocek.android.combatmap.DataManager;

public class BackgroundImage {

    /**
     * The data manager that is used to load custom images.
     */
    private static transient DataManager dataManager = null;

    public static void registerDataManager(DataManager dataManager) {
        BackgroundImage.dataManager = dataManager;
    }

    /**
     * Path that this image should load from.
     */
    private String mPath;

    /**
     * Drawable containing the background image.
     */
    private transient Drawable mDrawable = null;
    private transient boolean mTriedToLoadDrawable = false;

    /**
     * Height of the image's containing rectangle, in world space.
     */
    private float mHeightWorldSpace = 1;

    /**
     * Width of the image's containing rectangle, in world space.
     */
    private float mWidthWorldSpace = 1;

    /**
     * Location of the upper left corner of the image's containing rectangle, in
     * world space.
     */
    private PointF mOriginWorldSpace;

    /**
     * Constructor.
     * @param path Path to the resource to load.
     * @param originWorldSpace Initial position of the background image, in
     *     world space.
     */
    public BackgroundImage(String path, PointF originWorldSpace) {
        this.mPath = path;
        this.mOriginWorldSpace = originWorldSpace;

        // TODO: BAD!!! VERY BAD!!! GET THIS OFF THE UI THREAD!!!!
        loadDrawable();
    }

    private void loadDrawable() {
        // Don't go any further if we've already tried and failed.
        if (mTriedToLoadDrawable) {
            return;
        }

        // Don't go any further if we don't have a data manager.
        if (dataManager == null) {
            return;
        }

        Bitmap b;
        try {
            b = dataManager.loadMapDataImage(this.mPath);
            this.mDrawable = new BitmapDrawable(b);
        } catch (IOException e) {
            e.printStackTrace();
            this.mTriedToLoadDrawable = true;
        }
    }

    /**
     * Draws the background image. Needs to assume an untransformed coordinate
     * space, UNLIKE other draw commands. The coordinate transformer is passed
     * in instead. This is because setBounds is retarded and takes integers.
     * 
     * @param c
     *            Canvas to draw on.
     * @param transformer
     *            The screen to world space transformer.
     */
    public void draw(Canvas c, CoordinateTransformer transformer) {
        if (this.mDrawable == null) {
            // TODO: GET THIS OFF THE UI THREAD!!!!
            loadDrawable();
            if (this.mDrawable == null) {
                return;
            }
        }

        // Convert bounding rectangle bounds to screen space.
        PointF upperLeft =
                transformer.worldSpaceToScreenSpace(new PointF(
                        this.mOriginWorldSpace.x, this.mOriginWorldSpace.y));
        PointF lowerRight =
                transformer.worldSpaceToScreenSpace(new PointF(
                        this.mOriginWorldSpace.x + this.mWidthWorldSpace,
                        this.mOriginWorldSpace.y + this.mHeightWorldSpace));
        int left = (int) upperLeft.x;
        int right = (int) lowerRight.x;
        int top = (int) upperLeft.y;
        int bottom = (int) lowerRight.y;

        this.mDrawable.setBounds(left, top, right, bottom);

        this.mDrawable.draw(c);
    }

    public BoundingRectangle getBoundingRectangle() {
        return this.getBoundingRectangle(0);
    }

    public BoundingRectangle getBoundingRectangle(float borderWorldSpace) {
        PointF p1 =
                new PointF(this.mOriginWorldSpace.x - borderWorldSpace,
                        this.mOriginWorldSpace.y - borderWorldSpace);
        PointF p2 =
                new PointF(this.mOriginWorldSpace.x + this.mWidthWorldSpace
                        + borderWorldSpace, this.mOriginWorldSpace.y
                        + this.mHeightWorldSpace + borderWorldSpace);
        return new BoundingRectangle(p1, p2);
    }

    public void moveBottom(float delta) {
        this.mHeightWorldSpace += delta;
    }

    public void moveImage(float deltaX, float deltaY) {
        this.mOriginWorldSpace =
                new PointF(this.mOriginWorldSpace.x + deltaX,
                        this.mOriginWorldSpace.y + deltaY);
    }

    public void moveLeft(float delta) {
        this.mOriginWorldSpace.x += delta;
        this.mWidthWorldSpace -= delta;
    }

    public void moveRight(float delta) {
        this.mWidthWorldSpace += delta;
    }

    public void moveTop(float delta) {
        this.mOriginWorldSpace.y += delta;
        this.mHeightWorldSpace -= delta;
    }

    public void setLocation(PointF location) {
        this.mOriginWorldSpace = location;
    }
}
