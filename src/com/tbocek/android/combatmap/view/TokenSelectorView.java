package com.tbocek.android.combatmap.view;

import java.util.ArrayList;
import java.util.Collection;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.tbocek.android.combatmap.R;
import com.tbocek.android.combatmap.TokenDatabase;
import com.tbocek.android.combatmap.model.primitives.BaseToken;

/**
 * Provides a horizontally scrolling list of tokens, allowing the user to
 * pick one.
 * @author Tim Bocek
 *
 */
public final class TokenSelectorView extends LinearLayout {

	/**
	 * Dimensions of the square token views.
	 */
	private static final int TOKEN_VIEW_SIZE = 80;

	/**
     * The inner layout that contains the tokens.
     */
	private LinearLayout tokenLayout;

	/**
	 * Button that represents opening the tag selector.
	 */
    private Button groupSelector;

    /**
     * Button that represents opening the token manager.
     */
    private Button tokenManager;

    /**
     * A factory that caches views already created for a given token.
     */
    private TokenViewFactory mTokenViewFactory;

    /**
     * Whether this control is being superimposed over a dark background.
     */
    private boolean drawDark = false;

    /**
     * Constructor.
     * @param context The context to create this view in.
     */
    public TokenSelectorView(final Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.token_selector, this);
        //Create and add the child layout, which will be a linear layout of
        // tokens.

        tokenLayout = new LinearLayout(context);
        ((HorizontalScrollView) findViewById(R.id.token_scroll_view))
        		.addView(tokenLayout);

        groupSelector = (Button) findViewById(
        		R.id.token_category_selector_button);
        groupSelector.setAlpha(1.0f);
        tokenManager = (Button) findViewById(R.id.token_manager_button);
        tokenManager.setAlpha(1.0f);
        mTokenViewFactory = new TokenViewFactory(context);
    }


    /**
     * Creates a view that allows a token to be selected.
     * @param prototype  The token prototype this view will represent.
     * @return The view.
     */
    private View createTokenView(final BaseToken prototype) {
        View b = this.mTokenViewFactory.getTokenView(prototype);
        b.setOnClickListener(onClickListener);
        b.setLayoutParams(new LinearLayout.LayoutParams(
        		(int) (TOKEN_VIEW_SIZE * getResources().getDisplayMetrics().density),
        		(int) (TOKEN_VIEW_SIZE * getResources().getDisplayMetrics().density)));
        return b;
    }

    /**
     * Listener that fires when an individual token is clicked.  This forwards
     * the call on.
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            TokenButton clicked = (TokenButton) v;
            if (mOnTokenSelectedListener != null) {
                mOnTokenSelectedListener.onTokenSelected(clicked.getClone());
            }
        }
    };

    /**
     * Listener that fires when a token is selected.
     * @author Tim Bocek
     *
     */
    public interface OnTokenSelectedListener {
    	/**
    	 *
    	 * @param t The selected token.  This is already a unique clone.
    	 */
        void onTokenSelected(BaseToken t);
    }

    /**
     * Listener that fires when a token is selected.
     */
    private OnTokenSelectedListener mOnTokenSelectedListener = null;

    /**
     * Database to load tokens from.
     */
    private TokenDatabase tokenDatabase;

    /**
     * Sets the listener to fire when a token is selected.
     * @param onTokenSelectedListener The listener.
     */
    public void setOnTokenSelectedListener(
    		final OnTokenSelectedListener onTokenSelectedListener) {
        this.mOnTokenSelectedListener = onTokenSelectedListener;
    }



    /**
     * Sets an OnClickListener for the tag selector button.
     * @param listener The listener to use when the tag selector is clicked.
     */
    public void setOnClickGroupSelectorListener(
    		final View.OnClickListener listener) {
        groupSelector.setOnClickListener(listener);
    }

    /**
     * Sets an OnClickLIstener for the token manager button.
     * @param listener The listener to use when the token manager button is
     * 		clicked.
     */
    public void setOnClickTokenManagerListener(
    		final View.OnClickListener listener) {
        tokenManager.setOnClickListener(listener);
    }


    /**
     * Sets the token database to query for tokens.
     * @param database The token database.
     */
    public void setTokenDatabase(final TokenDatabase database) {
        this.tokenDatabase = database;
        setTokenList(tokenDatabase.getAllTokens());
    }

    /**
     * Sets the tag currently being displayed.
     * @param checkedTag The tag currently being displayed.
     */
    public void setSelectedTag(final String checkedTag) {
        setTokenList(tokenDatabase.getTokensForTag(checkedTag));
    }

    /**
     * Sets the list of tokens displayed to the given collection.
     * @param tokens Tokens to offer in the selector.
     */
    private void setTokenList(final Collection<BaseToken> tokens) {
        tokenLayout.removeAllViews();
        Collection<TokenButton> buttons = new ArrayList<TokenButton>();
        for (BaseToken token : tokens) {
        	TokenButton b = (TokenButton) createTokenView(token);
            tokenLayout.addView(b);
            buttons.add(b);
            b.setShouldDrawDark(drawDark);
        }
        new TokenLoadTask(buttons).execute();
    }


	/**
	 * @param drawDark Whether tokens are drawn on a dark background.
	 */
	public void setShouldDrawDark(boolean drawDark) {
		this.drawDark = drawDark;
	}


	/**
	 * @return Whether tokens are drawn on a dark background.
	 */
	public boolean shouldDrawDark() {
		return drawDark;
	}
}
