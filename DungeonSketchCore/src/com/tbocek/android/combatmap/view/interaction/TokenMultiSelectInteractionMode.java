package com.tbocek.android.combatmap.view.interaction;

import java.util.ArrayList;
import java.util.Collection;

import android.graphics.Canvas;
import android.view.MotionEvent;

import com.google.common.collect.Lists;
import com.tbocek.android.combatmap.model.primitives.BaseToken;
import com.tbocek.android.combatmap.model.primitives.CoordinateTransformer;
import com.tbocek.android.combatmap.model.primitives.PointF;
import com.tbocek.android.combatmap.model.primitives.Util;
import com.tbocek.android.combatmap.view.CombatView;

/**
 * Interaction mode for managing multiple selected tokens.
 * 
 * @author Tim
 * 
 */
public class TokenMultiSelectInteractionMode extends ZoomPanInteractionMode {
    /**
     * Distance in screen space that a token needs to be away from a snap point
     * until it will snap.
     */
    private static final int GRID_SNAP_THRESHOLD = 20;

    /**
     * The token that the user clicked on to start a drag operation. Will be
     * used to determine snapping to grid.
     */
    private BaseToken mCurrentToken;

    /**
     * Whether the current scroll operation should drag the selected tokens or
     * scroll the screen.
     */
    private boolean mDragging;

    /**
     * When moving tokens with snap to grid enabled, this is the last point that
     * was snapped to.
     */
    private PointF mLastSnappedLocation;

    /**
     * Whether the current token has been moved.
     */
    private boolean mMoved;

    /**
     * Collection of ghost tokens to draw when moving a group of tokens.
     */
    private Collection<BaseToken> mUnmovedTokens = Lists.newArrayList();

    /**
     * Constructor.
     * 
     * @param view
     *            The view to manipulate.
     */
    public TokenMultiSelectInteractionMode(CombatView view) {
        super(view);
    }

    @Override
    public void draw(final Canvas c) {
        for (BaseToken t : this.mUnmovedTokens) {
            t.drawGhost(c, this.getView().getGridSpaceTransformer(),
                    t.getLocation());
        }
    }

    @Override
    public boolean onDown(final MotionEvent e) {
        this.mCurrentToken =
                this.getView()
                        .getTokens()
                        .getTokenUnderPoint(new PointF(e.getX(), e.getY()),
                                this.getView().getGridSpaceTransformer());

        if (this.mCurrentToken != null
                && this.getView().getMultiSelect().getSelectedTokens()
                        .contains(this.mCurrentToken)) {
            this.mMoved = false;
            this.mDragging = true;
            this.getView()
                    .getTokens()
                    .checkpointTokens(
                            new ArrayList<BaseToken>(this.getView()
                                    .getMultiSelect().getSelectedTokens()));
            for (BaseToken t : this.getView().getMultiSelect()
                    .getSelectedTokens()) {
                this.mUnmovedTokens.add(t.clone());
            }
            this.mLastSnappedLocation = this.mCurrentToken.getLocation();
        } else {
            this.mDragging = false;
        }

        return true;
    }

    @Override
    public boolean onScroll(final MotionEvent e1, final MotionEvent e2,
            final float distanceX, final float distanceY) {
        if (this.mDragging) {
            this.mMoved = true;
            CoordinateTransformer transformer =
                    this.getView().getGridSpaceTransformer();
            float deltaX;
            float deltaY;

            // If snap to grid is enabled, change the world space movement
            // deltas to compensate for distance between the real point and
            // the snap to grid point.
            if (this.getView().shouldSnapToGrid()) {
                PointF currentPointScreenSpace =
                        new PointF(e2.getX(), e2.getY());
                PointF currentPointWorldSpace =
                        transformer
                                .screenSpaceToWorldSpace(currentPointScreenSpace);
                PointF nearestSnapPointWorldSpace =
                        this.getData()
                                .getGrid()
                                .getNearestSnapPoint(
                                        currentPointWorldSpace,
                                        this.getView()
                                                .tokensSnapToIntersections()
                                                ? 0
                                                : this.mCurrentToken.getSize());

                // Snap to that point if it is less than a threshold
                float distanceToSnapPoint =
                        Util.distance(
                                transformer
                                        .worldSpaceToScreenSpace(nearestSnapPointWorldSpace),
                                currentPointScreenSpace);

                PointF newLocationWorldSpace =
                        distanceToSnapPoint < GRID_SNAP_THRESHOLD
                                ? nearestSnapPointWorldSpace
                                : currentPointWorldSpace;

                deltaX = this.mLastSnappedLocation.x - newLocationWorldSpace.x;
                deltaY = this.mLastSnappedLocation.y - newLocationWorldSpace.y;
                this.mLastSnappedLocation = newLocationWorldSpace;

            } else {
                deltaX = transformer.screenSpaceToWorldSpace(distanceX);
                deltaY = transformer.screenSpaceToWorldSpace(distanceY);
            }
            for (BaseToken t : this.getView().getMultiSelect()
                    .getSelectedTokens()) {
                t.move(deltaX, deltaY);
            }
        } else {
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
        this.getView().refreshMap();
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(final MotionEvent e) {
        BaseToken t =
                this.getView()
                        .getTokens()
                        .getTokenUnderPoint(new PointF(e.getX(), e.getY()),
                                this.getView().getGridSpaceTransformer());
        if (t != null) {
            this.getView().getMultiSelect().toggleToken(t);
            this.getView().refreshMap();
        }
        return true;
    }

    @Override
    public void onUp(final MotionEvent ev) {
        this.mUnmovedTokens.clear();
        if (this.mMoved) {
            this.getView().getTokens().createCommandHistory();
        }
        this.getView().refreshMap();
    }

}
