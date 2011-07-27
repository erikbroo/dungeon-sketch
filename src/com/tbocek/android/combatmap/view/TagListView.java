package com.tbocek.android.combatmap.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.tbocek.android.combatmap.TokenDatabase;
import com.tbocek.android.combatmap.graphicscore.BaseToken;

import android.content.Context;
import android.graphics.Color;
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
        void onDragTokenToTag(BaseToken token, String tag);
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
     * Color to highlight text with when a token is being dragged to it.
     */
    private static final int DRAG_HIGHLIGHT_COLOR = Color.rgb(41, 162, 255);

    /**
     * The layout that contains the list of tags.
     */
    private LinearLayout innerLayout;

    /**
     * All text views representing tags.
     */
    private List<TextView> textViews = new ArrayList<TextView>();

    /**
     * The tag that should be highlighted.
     */
    private String highlightedTag = TokenDatabase.ALL;

    /**
     * The listener that is called when a tag is selected or dragged to.
     */
    private OnTagListActionListener onTagListAction;

    /**
     * Size in points of the text used in the child tag views.
     */
    private float textSize = 30;

    /**
     * Constructor.
     * @param context The context that this view is constructed in.
     */
    public TagListView(final Context context) {
        super(context);
        innerLayout = new LinearLayout(this.getContext());
        addView(innerLayout);
        innerLayout.setOrientation(LinearLayout.VERTICAL);
    }

    /**
     * Sets the list of tags that are displayed, and recreates the view to
     * display the correct list.  Maintains the currently highlighted tag.
     * @param collection The displayed tags.
     */
    public void setTagList(final Collection<String> collection) {
        innerLayout.removeAllViews();
        textViews.clear();
        innerLayout.addView(createTextView(TokenDatabase.ALL));
        for (String tag : collection) {
            innerLayout.addView(createTextView(tag));
        }

        setHighlightedTag(highlightedTag);

        if (onTagListAction != null) {
            onTagListAction.onChangeSelectedTag(highlightedTag);
        }
    }

    public void setTextSize(final float size) {
    	this.textSize = size;
    	for (TextView view : textViews) {
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
        v.setTextSize(this.textSize);
        v.setPadding(HORIZONTAL_PADDING, VERTICAL_PADDING, 0, VERTICAL_PADDING);
        textViews.add(v);
        v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                TextView textView  = (TextView) v;
                setHighlightedTag(textView.getText().toString());
                if (onTagListAction != null) {
                    onTagListAction.onChangeSelectedTag(
                    		textView.getText().toString());
                }
            }
        });
        v.setOnDragListener(mOnDrag);
        return v;
    }

    /**
     * Sets the listener to call when a tag is selected or dragged to.
     * @param listener The listener.
     */
    public void setOnTagListActionListener(
    		final OnTagListActionListener listener) {
        this.onTagListAction = listener;
    }

    /**
     * Sets the tag that is currently highlighted.
     * @param tag The highlighted tag.
     */
    private void setHighlightedTag(final String tag) {
        highlightedTag = tag;
        for (TextView v : textViews) {
            setTextViewColorToCorrectHighlight(v);
        }
    }

    /**
     * Given a text view representing a tag, sets the text color such that it
     * is highlighted if the tag matches the name of the view.
     * @param v The text view to change.
     */
    private void setTextViewColorToCorrectHighlight(final TextView v) {
        if (v.getText() == highlightedTag) {
            v.setTextColor(Color.WHITE);
        } else {
            v.setTextColor(Color.GRAY);
        }
    }

    /**
     * @return The currently selected tag.
     */
    public String getTag() {
        return this.highlightedTag;
    }

    /**
     * Listener that manages dragging a token onto a tag view.
     */
    private View.OnDragListener mOnDrag = new View.OnDragListener() {
        @Override
        public boolean onDrag(final View view, final DragEvent event) {
            Log.d("DRAG", Integer.toString(event.getAction()));
            TextView tv = (TextView) view;
            if (event.getAction() == DragEvent.ACTION_DROP) {
                BaseToken toAdd = (BaseToken) event.getLocalState();
                if (onTagListAction != null) {
                    onTagListAction.onDragTokenToTag(
                    		toAdd, tv.getText().toString());
                }
                setTextViewColorToCorrectHighlight(tv);
                return true;
            } else if (event.getAction() == DragEvent.ACTION_DRAG_ENTERED) {
                tv.setTextColor(DRAG_HIGHLIGHT_COLOR);
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
}
