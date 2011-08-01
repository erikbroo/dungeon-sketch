package com.tbocek.android.combatmap.tokenmanager;

import java.util.Collection;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.view.View;

import com.tbocek.android.combatmap.graphicscore.BaseToken;
import com.tbocek.android.combatmap.view.TagListView;
import com.tbocek.android.combatmap.view.TokenButton;

/**
 * Extends the behavior of TokenButton to allow multiple tokens to be selected
 * at once.  Dragging from this button will drag a list of tokens, not a single
 * token.
 * @author Tim Bocek
 *
 */
public final class MultiSelectTokenButton extends TokenButton {
	/**
	 * Whether this token is currently selected.
	 */
    private boolean selected = false;

    /**
     * The manager that tracks which tokens are selected across a group of
     * MultiSelectTokenButton instances.
     */
    private MultiSelectManager mMultiSelect;

    /**
     * Stroke width to use when drawing a border around selected tokens.
     */
    private static final int SELECTION_BORDER_STROKE_WIDTH = 4;

    /**
     * Constructor.
     * @param context Context to create this button in.
     * @param token The token represented by this button.
     * @param multiSelect The manager that tracks a group of selected tokens.
     */
	public MultiSelectTokenButton(
			final Context context, final BaseToken token,
			final MultiSelectManager multiSelect) {
		super(context, token);

		this.mMultiSelect = multiSelect;

        this.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View arg0) {
				selected = !selected;
				invalidate();
				if (selected) {
					mMultiSelect.addToken(getClone());
				} else {
					mMultiSelect.removeToken(getTokenId());
				}
			}
        });
	}

    @Override
    public void onDraw(final Canvas c) {
    	super.onDraw(c);
	    if (selected) {
	    	float radius = this.getTokenRadius();
	    	Paint p = new Paint();
	    	p.setColor(TagListView.DRAG_HIGHLIGHT_COLOR);
	    	p.setStyle(Style.STROKE);
	    	p.setStrokeWidth(SELECTION_BORDER_STROKE_WIDTH);
	    	c.drawCircle(this.getWidth() / 2, this.getHeight() / 2, radius, p);
    	}
    }

    @Override
    protected void onStartDrag() {
    	// Add this token to the selection, so we are at least dragging it.
    	this.mMultiSelect.addToken(this.getClone());

    	Collection<BaseToken> tokens = this.mMultiSelect.getSelectedTokens();
        startDrag(
        		null,
        		new TokenStackDragShadow(tokens, (int) this.getTokenRadius()),
        		tokens, 0);
    }

    /**
     * Reloads whether this token is selected from the token database.
     */
	public void refreshSelectedState() {
		this.selected = mMultiSelect.isTokenSelected(getTokenId());
	}
}