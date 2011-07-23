package com.tbocek.android.combatmap.view;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.tbocek.android.combatmap.TokenDatabase;

/**
 * Provides a scrolling view of token categories that can be selected.
 * @author Tim Bocek
 *
 */
public final class TokenCategorySelector extends ScrollView {
	/**
	 * Layout that views will be added to.
	 */
    private LinearLayout innerLayout;

    /**
     * Listener that fires when a tag is selected.
     */
    private OnTagSelectedListener mOnTagSelectedListener;

    /**
     * Size to use when rendering text.
     */
    private static final int TEXT_SIZE = 16;

    /**
     * Sets the listener that fires when a tag is selected.
     * @param onCheckedListChangedListener The listener to use.
     */
    public void setOnCheckedListChangedListener(
            final OnTagSelectedListener onCheckedListChangedListener) {
        this.mOnTagSelectedListener = onCheckedListChangedListener;
    }

    /**
     * Constructor.
     * @param context The context to construct this view in.
     */
    public TokenCategorySelector(final Context context) {
        super(context);
        innerLayout = new LinearLayout(this.getContext());
        innerLayout.setOrientation(LinearLayout.VERTICAL);
        this.addView(innerLayout);
    }


    /**
     * Adds a button to the view that will select the given tag.
     * @param tag The tag represented by this button.
     */
    private void addButton(final String tag) {
        Button b = new Button(this.getContext());
        b.setText(tag);
        b.setTextSize(TEXT_SIZE);
        innerLayout.addView(b);
        b.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                if (mOnTagSelectedListener != null) {
                    mOnTagSelectedListener.onTagSelected(
                    		((Button) v).getText().toString());
                }
            }
        });
    }

    /**
     * Reads tags from the token database and sets up this view.
     * @param tokenDatabase The token database to use tags from.
     */
    public void setTokenDatabase(final TokenDatabase tokenDatabase) {
        innerLayout.removeAllViews();
        for (String tag : tokenDatabase.getTags()) {
            addButton(tag);
        }
    }

    /**
     * Listener that fires when a new tag is selected.
     * @author Tim Bocek
     *
     */
    public interface OnTagSelectedListener {
    	/**
    	 * Called when a new tag is selected.
    	 * @param tag The new tag.
    	 */
    	void onTagSelected(String tag);
    }

}
