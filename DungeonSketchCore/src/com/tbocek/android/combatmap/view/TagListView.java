package com.tbocek.android.combatmap.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

import com.tbocek.android.combatmap.TokenDatabase;
import com.tbocek.android.combatmap.model.primitives.BaseToken;
import com.tbocek.android.combatmap.model.primitives.Util;

/**
 * Scrolling list of tags that are presented in the token manager. This allows
 * the user to pick a tag or to drop a token onto the tag.
 * 
 * @author Tim
 * 
 */
public final class TagListView extends ScrollView {

    /**
     * Default text size to use in child views.
     */
    private static final int DEFAULT_TEXT_SIZE = 30;

    /**
     * Number of pixels to pad the left of each view with.
     */
    private static final int HORIZONTAL_PADDING = 6;

    /**
     * Number of pixels to pad the top and bottom of each text view with.
     */
    private static final int VERTICAL_PADDING = 8;
    /**
     * The tag that should be highlighted.
     */
    private String mHighlightedTag = TokenDatabase.ALL;

    /**
     * The layout that contains the list of tags.
     */
    private LinearLayout mInnerLayout;

    /**
     * Listener that manages dragging a token onto a tag view.
     */
    private View.OnDragListener mOnDrag;

    /**
     * The listener that is called when a tag is selected or dragged to.
     */
    private OnTagListActionListener mOnTagListAction;

    /**
     * Whether children of this view should be registered for a context menu.
     */
    private boolean mRegisterChildrenForContextMenu;

    /**
     * Size in points of the text used in the child tag views.
     */
    private float mTextSize = DEFAULT_TEXT_SIZE;

    /**
     * All text views representing tags.
     */
    private List<TextView> mTextViews = new ArrayList<TextView>();

    /**
     * Constructor.
     * 
     * @param context
     *            The context that this view is constructed in.
     */
    public TagListView(final Context context) {
        super(context);
        this.mInnerLayout = new LinearLayout(this.getContext());
        this.addView(this.mInnerLayout);
        this.mInnerLayout.setOrientation(LinearLayout.VERTICAL);
        
        // Create an initial cache of TextViews.s
    }

    /**
     * Creates a text view representing a tag.
     * 
     * @param text
     *            The tag represented.
     * @return The text view.
     */
    private TextView createTextView(final String text) {
        TextView v = new TextView(this.getContext());
        v.setText(text);
        v.setTextSize(this.mTextSize);
        v.setPadding(HORIZONTAL_PADDING, VERTICAL_PADDING, 0, VERTICAL_PADDING);
        this.mTextViews.add(v);
        v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                TextView textView = (TextView) v;
                TagListView.this.setHighlightedTag(textView.getText()
                        .toString());
                if (TagListView.this.mOnTagListAction != null) {
                    TagListView.this.mOnTagListAction
                            .onChangeSelectedTag(textView.getText().toString());
                }
            }
        });

        if (this.mRegisterChildrenForContextMenu
                && !text.equals(TokenDatabase.ALL)) {
            Activity activity = (Activity) this.getContext();
            activity.registerForContextMenu(v);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            this.mOnDrag = new View.OnDragListener() {
                @Override
                public boolean onDrag(final View view, final DragEvent event) {
                    Log.d("DRAG", Integer.toString(event.getAction()));
                    TextView tv = (TextView) view;
                    if (event.getAction() == DragEvent.ACTION_DROP) {
                        @SuppressWarnings("unchecked")
                        Collection<BaseToken> toAdd =
                                (Collection<BaseToken>) event.getLocalState();
                        if (TagListView.this.mOnTagListAction != null) {
                            TagListView.this.mOnTagListAction
                                    .onDragTokensToTag(toAdd, tv.getText()
                                            .toString());
                        }
                        TagListView.this.setTextViewColorToCorrectHighlight(tv);
                        return true;
                    } else if (event.getAction() == DragEvent.ACTION_DRAG_ENTERED) {
                        tv.setTextColor(Util.ICS_BLUE);
                        return true;
                    } else if (event.getAction() == DragEvent.ACTION_DRAG_EXITED) {
                        TagListView.this.setTextViewColorToCorrectHighlight(tv);
                        return true;
                    } else if (event.getAction() == DragEvent.ACTION_DRAG_STARTED) {
                        return true;
                    }
                    return true;
                }
            };
            v.setOnDragListener(this.mOnDrag);
        }

        return v;
    }

    /**
     * @return The currently selected tag.
     */
    @Override
    public String getTag() {
        return this.mHighlightedTag;
    }

    /**
     * Checks whether the given view is managed by this view.
     * 
     * @param v
     *            The child view to check.
     * @return True if the view is managed by this view, false otherwise
     */
    public boolean isViewAChild(View v) {
        return this.mTextViews.contains(v);
    }

    /**
     * Sets the tag that is currently highlighted.
     * 
     * @param tag
     *            The highlighted tag.
     */
    public void setHighlightedTag(final String tag) {
        this.mHighlightedTag = tag;
        for (TextView v : this.mTextViews) {
            this.setTextViewColorToCorrectHighlight(v);
        }
    }

    /**
     * Sets the listener to call when a tag is selected or dragged to.
     * 
     * @param listener
     *            The listener.
     */
    public void setOnTagListActionListener(
            final OnTagListActionListener listener) {
        this.mOnTagListAction = listener;
    }

    /**
     * Sets whether to register children for a context menu.
     * 
     * @param register
     *            Whether to register children.
     */
    public void setRegisterChildrenForContextMenu(boolean register) {
        this.mRegisterChildrenForContextMenu = register;
    }

    /**
     * Sets the list of tags that are displayed, and recreates the view to
     * display the correct list. Maintains the currently highlighted tag.
     * 
     * @param collection
     *            The displayed tags.
     */
    public void setTagList(final Collection<String> collection) {
        this.mInnerLayout.removeAllViews();
        this.mTextViews.clear();
        this.mInnerLayout.addView(this.createTextView(TokenDatabase.ALL));
        for (String tag : collection) {
            this.mInnerLayout.addView(this.createTextView(tag));
        }

        this.setHighlightedTag(this.mHighlightedTag);

        if (this.mOnTagListAction != null) {
            this.mOnTagListAction.onChangeSelectedTag(this.mHighlightedTag);
        }
    }

    /**
     * Sets the text size to use in child views.
     * 
     * @param size
     *            The size to set.
     */
    public void setTextSize(final float size) {
        this.mTextSize = size;
        for (TextView view : this.mTextViews) {
            view.setTextSize(size);
        }
    }

    /**
     * Given a text view representing a tag, sets the text color such that it is
     * highlighted if the tag matches the name of the view.
     * 
     * @param v
     *            The text view to change.
     */
    private void setTextViewColorToCorrectHighlight(final TextView v) {
        if (v.getText().equals(this.mHighlightedTag)) {
            v.setTextColor(Color.WHITE);
        } else {
            v.setTextColor(Color.GRAY);
        }
    }

    /**
     * Listener that allows client code to specify what happens when a tag is
     * selected or when a token is dropped onto a tag.
     * 
     * @author Tim Bocek
     * 
     */
    public interface OnTagListActionListener {
        /**
         * Called when the user clicks on a tag.
         * 
         * @param newTag
         *            The tag clicked on.
         */
        void onChangeSelectedTag(String newTag);

        /**
         * Called when the user drags a token onto a tag.
         * 
         * @param token
         *            The token that was dragged.
         * @param tag
         *            The tag that the token was dragged to.
         */
        void onDragTokensToTag(Collection<BaseToken> token, String tag);
    }

}
