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
	 * Whether the current scroll operation should drag the selected tokens or
	 * scroll the screen.
	 */
	private boolean mDragging;

	/**
	 * Whether the current token has been moved.
	 */
	private boolean mMoved;

	/**
	 * Collection of ghost tokens to draw when moving a group of tokens.
	 */
	private Collection<BaseToken> mUnmovedTokens = Lists.newArrayList();

	/**
	 * The token that the user clicked on to start a drag operation. Will be
	 * used to determine snapping to grid.
	 */
	private BaseToken mCurrentToken;

	/**
	 * When moving tokens with snap to grid enabled, this is the last point that
	 * was snapped to.
	 */
	private PointF mLastSnappedLocation;

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
	public boolean onSingleTapConfirmed(final MotionEvent e) {
		BaseToken t = getView().getTokens().getTokenUnderPoint(
				new PointF(e.getX(), e.getY()),
				getView().getGridSpaceTransformer());
		if (t != null) {
			getView().getMultiSelect().toggleToken(t);
			getView().refreshMap();
		}
		return true;
	}

	@Override
	public boolean onDown(final MotionEvent e) {
		mCurrentToken = getView().getTokens().getTokenUnderPoint(
				new PointF(e.getX(), e.getY()),
				getView().getGridSpaceTransformer());

		if (mCurrentToken != null
				&& getView().getMultiSelect().getSelectedTokens()
						.contains(mCurrentToken)) {
			mMoved = false;
			mDragging = true;
			getView().getTokens().checkpointTokens(
					new ArrayList<BaseToken>(getView().getMultiSelect()
							.getSelectedTokens()));
			for (BaseToken t : getView().getMultiSelect().getSelectedTokens()) {
				this.mUnmovedTokens.add(t.clone());
			}
			mLastSnappedLocation = mCurrentToken.getLocation();
		} else {
			mDragging = false;
		}

		return true;
	}

	@Override
	public void onUp(final MotionEvent ev) {
		mUnmovedTokens.clear();
		if (mMoved) {
			getView().getTokens().createCommandHistory();
		}
		getView().refreshMap();
	}

	@Override
	public boolean onScroll(final MotionEvent e1, final MotionEvent e2,
			final float distanceX, final float distanceY) {
		if (mDragging) {
			mMoved = true;
			CoordinateTransformer transformer = getView()
					.getGridSpaceTransformer();
			float deltaX;
			float deltaY;

			// If snap to grid is enabled, change the world space movement
			// deltas to compensate for distance between the real point and
			// the snap to grid point.
			if (getView().shouldSnapToGrid()) {
				PointF currentPointScreenSpace = new PointF(e2.getX(),
						e2.getY());
				PointF currentPointWorldSpace = transformer
						.screenSpaceToWorldSpace(currentPointScreenSpace);
				PointF nearestSnapPointWorldSpace = getData().getGrid()
						.getNearestSnapPoint(
								currentPointWorldSpace,
								getView().tokensSnapToIntersections() ? 0
										: mCurrentToken.getSize());

				// Snap to that point if it is less than a threshold
				float distanceToSnapPoint = Util.distance(transformer
						.worldSpaceToScreenSpace(nearestSnapPointWorldSpace),
						currentPointScreenSpace);

				PointF newLocationWorldSpace = distanceToSnapPoint < GRID_SNAP_THRESHOLD ? nearestSnapPointWorldSpace
						: currentPointWorldSpace;

				deltaX = mLastSnappedLocation.x - newLocationWorldSpace.x;
				deltaY = mLastSnappedLocation.y - newLocationWorldSpace.y;
				mLastSnappedLocation = newLocationWorldSpace;

			} else {
				deltaX = transformer.screenSpaceToWorldSpace(distanceX);
				deltaY = transformer.screenSpaceToWorldSpace(distanceY);
			}
			for (BaseToken t : getView().getMultiSelect().getSelectedTokens()) {
				t.move(deltaX, deltaY);
			}
		} else {
			return super.onScroll(e1, e2, distanceX, distanceY);
		}
		getView().refreshMap();
		return true;
	}

	@Override
	public void draw(final Canvas c) {
		for (BaseToken t : mUnmovedTokens) {
			t.drawGhost(c, getView().getGridSpaceTransformer(), t.getLocation());
		}
	}

}
