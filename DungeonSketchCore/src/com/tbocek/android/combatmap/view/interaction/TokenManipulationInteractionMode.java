package com.tbocek.android.combatmap.view.interaction;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

import com.tbocek.android.combatmap.R;
import com.tbocek.android.combatmap.model.primitives.BaseToken;
import com.tbocek.android.combatmap.model.primitives.CoordinateTransformer;
import com.tbocek.android.combatmap.model.primitives.PointF;
import com.tbocek.android.combatmap.model.primitives.Util;
import com.tbocek.android.combatmap.view.CombatView;

/**
 * An interaction mode that allows the user to drag tokens around the canvas.
 * 
 * @author Tim Bocek
 * 
 */
public final class TokenManipulationInteractionMode extends
        ZoomPanInteractionMode {
    /**
     * Distance in screen space that a token needs to be away from a snap point
     * until it will snap.
     */
    private static final int GRID_SNAP_THRESHOLD = 20;

    /**
     * Rectangle in which to draw the trash can.
     */
    // CHECKSTYLE:OFF
    private static final Rect TRASH_CAN_RECT = new Rect(16, 16, 96 + 16,
            96 + 16);
    // CHECKSTYLE:ON

    /**
     * Length of the trash can fade in, in ms.
     */
    private static final int TRASH_FADE_IN_DURATION = 1000;

    /**
     * Length of the trash can fade out, in ms.
     */
    private static final int TRASH_FADE_OUT_DURATION = 250;

    PointF debugSnapPoint = null;

    /**
     * True if the token is being hovered over the trash can.
     */
    private boolean mAboutToTrash;

    /**
     * Cached value of whether drawing against a dark background. Used to detect
     * theme changes.
     */
    private boolean mCachedDark;

    /**
     * The token currently being dragged around.
     */
    private BaseToken mCurrentToken;

    /**
     * Whether the use is currently dragging a token.
     */
    private boolean mDown;

    /**
     * Whether the current token has been moved.
     */
    private boolean mMoved;

    /**
     * The original location of the token being dragged around.
     */
    private PointF mOriginalLocation;

    /**
     * Animated alpha value to use for the trash can; allows it to fade in and
     * out.
     */
    private int mTrashCanAlpha;

    /**
     * Animation object to fade the trash can.
     */
    private ValueAnimator mTrashCanAnimator;

    /**
     * Animation update handler that changes the alpha value of the trash can.
     */
    private ValueAnimator.AnimatorUpdateListener mTrashCanFadeListener =
            new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    TokenManipulationInteractionMode.this.mTrashCanAlpha =
                            (Integer) animation.getAnimatedValue();
                    TokenManipulationInteractionMode.this.getView()
                            .refreshMap();
                }
            };

    /**
     * Cached drawable for a trash can to move tokens to.
     */
    private Drawable mTrashDrawable;

    /**
     * Cached drawable for the trash can image when a token is dragged to it.
     */
    private Drawable mTrashHoverDrawable;

    /**
     * Constructor.
     * 
     * @param view
     *            The CombatView that this mode will interact with.
     */
    public TokenManipulationInteractionMode(final CombatView view) {
        super(view);
    }

    @Override
    public void draw(final Canvas c) {
        if (this.mCurrentToken != null && this.mDown) {
            // Draw a ghost of the token at the location that it is being moved
            // from.
            this.mCurrentToken.drawGhost(c, this.getView()
                    .getGridSpaceTransformer(), this.mOriginalLocation);
        }

        if (this.mTrashCanAlpha != 0) {
            // Draw a trash can to drag tokens to.
            this.ensureTrashCanDrawablesCreated();

            if (this.mAboutToTrash) {
                this.mTrashHoverDrawable.setAlpha(this.mTrashCanAlpha);
                this.mTrashHoverDrawable.draw(c);
            } else {
                this.mTrashDrawable.setAlpha(this.mTrashCanAlpha);
                this.mTrashDrawable.draw(c);
            }
        }

        if (this.debugSnapPoint != null && this.mDown) {
            Paint p = new Paint();
            p.setColor(this.getData().getGrid().getColorScheme().getLineColor());
            p.setStyle(Paint.Style.STROKE);

            c.drawCircle(this.debugSnapPoint.x, this.debugSnapPoint.y, 3, p);

        }
    }

    /**
     * Checks whether trash can drawables need to be created, and creates them
     * if needed.
     */
    private void ensureTrashCanDrawablesCreated() {
        if (this.mTrashDrawable == null
                || this.mCachedDark != this.getData().getGrid().isDark()) {
            if (this.getData().getGrid().isDark()) {
                this.mTrashDrawable =
                        this.getView().getResources()
                                .getDrawable(R.drawable.trashcan);
            } else {
                this.mTrashDrawable =
                        this.getView().getResources()
                                .getDrawable(R.drawable.trashcan_dark);
            }

            this.mCachedDark = this.getData().getGrid().isDark();
            this.mTrashDrawable.setBounds(TRASH_CAN_RECT);
            this.mTrashHoverDrawable =
                    this.getView().getResources()
                            .getDrawable(R.drawable.trashcan_hover_over);
            this.mTrashHoverDrawable.setBounds(TRASH_CAN_RECT);
        }
    }

    /**
     * Begins an animation to fade the trash can in.
     */
    private void fadeTrashCanIn() {
        if (this.mTrashCanAnimator != null
                && this.mTrashCanAnimator.isRunning()) {
            this.mTrashCanAnimator.cancel();
        }
        this.mTrashCanAnimator = ValueAnimator.ofInt(0, Util.FULL_OPACITY);
        this.mTrashCanAnimator.setDuration(TRASH_FADE_IN_DURATION);
        this.mTrashCanAnimator.addUpdateListener(this.mTrashCanFadeListener);
        this.mTrashCanAnimator.start();
    }

    /**
     * Begins an animation to fade the trash can out.
     */
    private void fadeTrashCanOut() {
        if (this.mTrashCanAnimator != null
                && this.mTrashCanAnimator.isRunning()) {
            this.mTrashCanAnimator.cancel();
        }
        this.mTrashCanAnimator = ValueAnimator.ofInt(this.mTrashCanAlpha, 0);
        this.mTrashCanAnimator.setDuration(TRASH_FADE_OUT_DURATION);
        this.mTrashCanAnimator.addUpdateListener(this.mTrashCanFadeListener);
        this.mTrashCanAnimator.start();
    }

    @Override
    public boolean onDoubleTap(final MotionEvent e) {
        if (this.mCurrentToken != null) {
            this.getView().getTokens().checkpointToken(this.mCurrentToken);
            this.mCurrentToken.setBloodied(!this.mCurrentToken.isBloodied());
            this.getView().getTokens().createCommandHistory();
        }
        this.getView().refreshMap();
        return true;
    }

    @Override
    public boolean onDown(final MotionEvent e) {
        this.mCurrentToken =
                this.getView()
                        .getTokens()
                        .getTokenUnderPoint(new PointF(e.getX(), e.getY()),
                                this.getView().getGridSpaceTransformer());

        if (this.mCurrentToken != null) {
            this.mMoved = false;
            this.mOriginalLocation = this.mCurrentToken.getLocation();
            this.getView().getTokens().checkpointToken(this.mCurrentToken);
            this.fadeTrashCanIn();
        }

        this.mDown = true;
        return true;
    }

    @Override
    public void onLongPress(final MotionEvent e) {
        if (this.mCurrentToken != null) {
            this.getView().getMultiSelect().addToken(this.mCurrentToken);
            this.getView().refreshMap();
        }
    }

    @Override
    public boolean onScroll(final MotionEvent e1, final MotionEvent e2,
            final float distanceX, final float distanceY) {
        if (this.mCurrentToken != null) {
            this.mMoved = true;
            CoordinateTransformer transformer =
                    this.getView().getGridSpaceTransformer();
            PointF currentPointScreenSpace = new PointF(e2.getX(), e2.getY());
            if (this.getView().shouldSnapToGrid()) {
                // Get the nearest snap point in screen space
                PointF nearestSnapPointWorldSpace =
                        this.getData()
                                .getGrid()
                                .getNearestSnapPoint(
                                        transformer
                                                .screenSpaceToWorldSpace(currentPointScreenSpace),
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

                this.debugSnapPoint =
                        transformer
                                .worldSpaceToScreenSpace(nearestSnapPointWorldSpace);

                this.mCurrentToken
                        .setLocation(distanceToSnapPoint < GRID_SNAP_THRESHOLD
                                ? nearestSnapPointWorldSpace
                                : transformer
                                        .screenSpaceToWorldSpace(currentPointScreenSpace));
            } else {
                this.mCurrentToken.setLocation(transformer
                        .screenSpaceToWorldSpace(currentPointScreenSpace));
            }
            this.mAboutToTrash =
                    TRASH_CAN_RECT.contains((int) e2.getX(), (int) e2.getY());
        } else {
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
        this.getView().refreshMap();
        return true;
    }

    @Override
    public void onUp(final MotionEvent ev) {
        this.mDown = false;
        this.debugSnapPoint = null;
        this.getView().refreshMap();
        if (this.mCurrentToken != null) {
            if (this.mAboutToTrash) {
                this.getView().getTokens().restoreCheckpointedTokens();
                this.getView().getTokens().remove(this.mCurrentToken);
                this.getView().refreshMap();
            } else if (this.mMoved) {
                this.getView().getTokens().createCommandHistory();
            }
            this.fadeTrashCanOut();
        }
    }

}
