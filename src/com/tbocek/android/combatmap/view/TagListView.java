package com.tbocek.android.combatmap.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.tbocek.android.combatmap.TokenDatabase;
import com.tbocek.android.combatmap.model.primitives.BaseToken;
import com.tbocek.android.combatmap.model.primitives.Util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Scrolling list of tags that are presented in the token manager.  This allows
 * the user to pick a tag or to drop a token onto the tag.
 * @author Tim
 *
 */
public final class TagListView extends ScrollView {

	/**
	 * Listener that allows client code to specify what happens when a tag
	 * is selected or when a token is dropped onto a tag.
	 * @author Tim Bocek
	 *
	 */
    public interface OnTagListActionListener {
    	/**
    	 * Called when the user clicks on a tag.
    	 * @param newTag The tag clicked on.
    	 */
        void onChangeSelectedTag(String newTag);

        /**
         * Called when the user drags a token onto a tag.
         * @param token The token that was dragged.
         * @param tag The tag that the token was dragged to.
         */
        void onDragTokensToTag(Collection<BaseToken> token, String tag);
    }

    /**
     * Number of pixels to pad the top and bottom of each text view with.
     */
    private static final int VERTICAL_PADDING = 8;

    /**
     * Number of pixels to pad the left of each view with.
     */
    private static final int HORIZONTAL_PADDING = 6;
    
    /**
     * Default text size to use in child views.
     */
    private static final int DEFAULT_TEXT_SIZE = 30;

    /**
     * The layout that contains the list of tags.
     */
    private LinearLayout mInnerLayout;

    /**
     * All text views representing tags.
     */
    private List<TextView> mTextViews = new ArrayList<TextView>();

    /**
     * The tag that should be highlighted.
     */
    private String mHighlightedTag = TokenDatabase.ALL;

    /**
     * The listener that is called when a tag is selected or dragged to.
     */
    private OnTagListActionListener mOnTagListAction;

    /**
     * Size in points of the text used in the child tag views.
     */
    private float mTextSize = DEFAULT_TEXT_SIZE;

    /**
     * Listener that manages dragging a token onto a tag view.
     */
    private View.OnDragListener mOnDrag;
    
    /**
     * Whether children of this view should be registered for a context menu.
     */
    private boolean mRegisterChildrenForContextMenu;
    
    /**
     * Constructor.
     * @param context The context that this view is constructed in.
     */
    public TagListView(final Context context) {
        super(context);
        mInnerLayout = new LinearLayout(this.getContext());
        addView(mInnerLayout);
        mInnerLayout.setOrientation(LinearLayout.VERTICAL);
    }

    /**
     * Sets the list of tags that are displayed, and recreates the view to
     * display the correct list.  Maintains the currently highlighted tag.
     * @param collection The displayed tags.
     */
    public void setTagList(final Collection<String> collection) {
        mInnerLayout.removeAllViews();
        mTextViews.clear();
        mInnerLayout.addView(createTextView(TokenDatabase.ALL));
        for (String tag : collection) {
            mInnerLayout.addView(createTextView(tag));
        }

        setHighlightedTag(mHighlightedTag);

        if (mOnTagListAction != null) {
            mOnTagListAction.onChangeSelectedTag(mHighlightedTag);
        }
    }

    /**
     * Sets the text size to use in child views.
     * @param size The size to set.
     */
    public void setTextSize(final float size) {
    	this.mTextSize = size;
    	for (TextView view : mTextViews) {
    		view.setTextSize(size);
    	}
    }

    /**
     * Creates a text view representing a tag.
     * @param text The tag represented.
     * @return The text view.
     */
    private TextView createTextView(final String text) {
        TextView v = new TextView(this.getContext());
        v.setText(text);
        v.setTextSize(this.mTextSize);
        v.setPadding(HORIZONTAL_PADDING, VERTICAL_PADDING, 0, VERTICAL_PADDING);
        mTextViews.add(v);
        v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                TextView textView  = (TextView) v;
                setHighlightedTag(textView.getText().toString());
                if (mOnTagListAction != null) {
                    mOnTagListAction.onChangeSelectedTag(
                    		textView.getText().toString());
                }
            }
        });
        
        if (mRegisterChildrenForContextMenu 
        		&& !text.equals(TokenDatabase.ALL)) {
        	Activity activity = (Activity) this.getContext();
        	activity.registerForContextMenu(v);
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        	mOnDrag = new View.OnDragListener() {
                @Override
                public boolean onDrag(final View view, final DragEvent event) {
                    Log.d("DRAG", Integer.toString(event.getAction()));
                    TextView tv = (TextView) view;
                    if (event.getAction() == DragEvent.ACTION_DROP) {
                        @SuppressWarnings("unchecked")
						Collection<BaseToken> toAdd =
                        	(Collection<BaseToken>) event.getLocalState();
                        if (mOnTagListAction != null) {
                            mOnTagListAction.onDragTokensToTag(
                            		toAdd, tv.getText().toString());
                        }
                        setTextViewColorToCorrectHighlight(tv);
                        return true;
                    } else if (event.getAction() == DragEvent.ACTION_DRAG_ENTERED) {
                        tv.setTextColor(Util.ICS_BLUE);
                        return true;
                    } else if (event.getAction() == DragEvent.ACTION_DRAG_EXITED) {
                        setTextViewColorToCorrectHighlight(tv);
                        return true;
                    } else if (event.getAction() == DragEvent.ACTION_DRAG_STARTED) {
                        return true;
                    }
                    return true;
                }
            };
        	v.setOnDragListener(mOnDrag);
        }

        return v;
    }

    /**
     * Sets the listener to call when a tag is selected or dragged to.
     * @param listener The listener.
     */
    public void setOnTagListActionListener(
    		final OnTagListActionListener listener) {
        this.mOnTagListAction = listener;
    }

    /**
     * Sets the tag that is currently highlighted.
     * @param tag The highlighted tag.
     */
    public void setHighlightedTag(final String tag) {
        mHighlightedTag = tag;
        for (TextView v : mTextViews) {
            setTextViewColorToCorrectHighlight(v);
        }
    }

    /**
     * Given a text view representing a tag, sets the text color such that it
     * is highlighted if the tag matches the name of the view.
     * @param v The text view to change.
     */
    private void setTextViewColorToCorrectHighlight(final TextView v) {
        if (v.getText().equals(mHighlightedTag)) {
            v.setTextColor(Color.WHITE);
        } else {
            v.setTextColor(Color.GRAY);
        }
    }

    /**
     * @return The currently selected tag.
     */
    public String getTag() {
        return this.mHighlightedTag;
    }
    
    /**
     * Sets whether to register children for a context menu.
     * @param register Whether to register children.
     */
    public void setRegisterChildrenForContextMenu(boolean register) {
    	this.mRegisterChildrenForContextMenu = register;
    }
    
    /**
     * Checks whether the given view is managed by this view.
     * @param v The child view to check.
     * @return True if the view is managed by this view, false otherwise
     */
    public boolean isViewAChild(View v) {
    	return this.mTextViews.contains(v);
    }

}
