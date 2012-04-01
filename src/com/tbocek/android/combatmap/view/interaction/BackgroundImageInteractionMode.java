package com.tbocek.android.combatmap.view.interaction;

import android.view.MotionEvent;

import com.tbocek.android.combatmap.R;
import com.tbocek.android.combatmap.model.primitives.BackgroundImage;
import com.tbocek.android.combatmap.model.primitives.PointF;
import com.tbocek.android.combatmap.view.CombatView;

/**
 * Provides an interaction mode that allows importing and creating images.
 * @author Tim
 *
 */
public class BackgroundImageInteractionMode extends BaseDrawInteractionMode {

	public BackgroundImageInteractionMode(CombatView view) {
		super(view);
	}

    @Override
    public boolean onSingleTapConfirmed(final MotionEvent e) {
    	BackgroundImage i = new BackgroundImage(getView().getResources().getDrawable(R.drawable.add_image));
        i.setLocation(getView().getData().getWorldSpaceTransformer().screenSpaceToWorldSpace(new PointF(e.getX(), e.getY())));
    	getView().getData().getBackgroundImages().addImage(i);
    	getView().refreshMap();
        return true;
    }
}
