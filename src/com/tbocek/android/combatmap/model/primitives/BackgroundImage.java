package com.tbocek.android.combatmap.model.primitives;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

public class BackgroundImage {
    private Drawable mDrawable;
    private float mHeightWorldSpace = 1;
    private PointF mOriginWorldSpace;

    private float mWidthWorldSpace = 1;

    public BackgroundImage(Drawable drawable) {
        this.mDrawable = drawable;
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
