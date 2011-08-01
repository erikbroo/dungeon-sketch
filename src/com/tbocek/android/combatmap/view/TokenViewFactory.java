package com.tbocek.android.combatmap.view;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.view.View;

import com.tbocek.android.combatmap.graphicscore.BaseToken;

/**
 * Factory that creates and returns views for tokens, and employs a caching
 * scheme to ensure that the same view is not returned for each token.
 * @author Tim Bocek
 *
 */
public class TokenViewFactory {

	/**
	 * Map that stores cached views for tokens.
	 */
    private Map<BaseToken, View> cachedViews = new HashMap<BaseToken, View>();

    /**
     * The context to use when creating views.
     */
    private Context mContext;

    /**
     * Constructor.
     * @param context The context to use when creating views.
     */
    public TokenViewFactory(final Context context) {
        this.mContext = context;
    }

    /**
     * Gets a view representing the given token.
     * @param prototype Token prototype that will be used in the view.
     * @return The view for the given token.
     */
    public final View getTokenView(final BaseToken prototype) {
        if (cachedViews.containsKey(prototype)) {
        	return cachedViews.get(prototype);
        }

        TokenButton b = newTokenView(prototype);
        cachedViews.put(prototype, b);
        return b;
    }

	/**
	 * Creates a new token view for the given prototype.
	 * @param prototype Token prototype to include in the view.
	 * @return The created view.
	 */
	protected TokenButton newTokenView(final BaseToken prototype) {
		return new TokenButton(getContext(), prototype);
	}

	/**
	 * @return The context to use when constructing tokens.
	 */
	protected final Context getContext() {
		return mContext;
	}
}