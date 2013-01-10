package com.tbocek.android.combatmap.tokenmanager;

import java.util.Collection;

import android.graphics.Canvas;
import android.graphics.Point;
import android.view.View;

import com.tbocek.android.combatmap.model.primitives.BaseToken;

/**
 * This drag shadow draws the given collection of tokens as a stack. It is used
 * to display tokens when dragged and dropped as a group.
 * 
 * @author Tim Bocek
 * 
 */
public final class TokenStackDragShadow extends View.DragShadowBuilder {

    /**
     * Maximum tokens to display on the stack.
     */
    private static final int MAX_DISPLAYED_TOKENS = 5;

    /**
     * The radius of a single token.
     */
    private int mTokenRadius;

    /**
     * The tokens to display.
     */
    private Collection<BaseToken> mTokens;

    /**
     * Constructor.
     * 
     * @param tokens
     *            The tokens to display.
     * @param tokenRadius
     *            The radius of a single token.
     */
    public TokenStackDragShadow(final Collection<BaseToken> tokens,
            final int tokenRadius) {
        super();
        this.mTokens = tokens;
        this.mTokenRadius = tokenRadius;
    }

    @Override
    /**
     * Draws up to five tokens, offset.
     */
    public void onDrawShadow(final Canvas canvas) {
        int displayedTokens =
                Math.min(this.mTokens.size(), MAX_DISPLAYED_TOKENS);
        int tokenDiameter = canvas.getWidth() / 2;

        // If one token, display it alone. If two or three tokens, offset by
        // half a radius. Otherwise, crowd tokens until they fit.
        int tokenOffset = 0;
        if (displayedTokens < MAX_DISPLAYED_TOKENS - 1) {
            tokenOffset = tokenDiameter / 2;
        } else {
            tokenOffset = tokenDiameter / displayedTokens - 1;
        }

        int i = displayedTokens;
        for (BaseToken t : this.mTokens) {
            if (i == 0) {
                return;
            }

            int center = tokenDiameter / 2 + tokenOffset * (i - 1);
            // Force tokens into not being selected.
            boolean cachedSelected = t.isSelected();
            t.setSelected(false);
            t.draw(canvas, center, center, tokenDiameter / 2.0f, true, true);
            t.setSelected(cachedSelected);
            i--;
        }
    }

    @Override
    /**
     * Drag shadow should always be twice the size of a token, with the finger
     * location in the middle of the first displayed token.
     */
    public void onProvideShadowMetrics(final Point shadowSize,
            final Point shadowTouchPoint) {
        // CHECKSTYLE:OFF
        shadowSize.x = this.mTokenRadius * 4;
        shadowSize.y = this.mTokenRadius * 4;
        // CHECKSTYLE:ON
        shadowTouchPoint.x = this.mTokenRadius;
        shadowTouchPoint.y = this.mTokenRadius;
    }
}
