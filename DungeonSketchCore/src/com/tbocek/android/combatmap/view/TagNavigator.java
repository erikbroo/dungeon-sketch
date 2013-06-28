package com.tbocek.android.combatmap.view;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;
import com.tbocek.android.combatmap.R;
import com.tbocek.android.combatmap.TokenDatabase;
import com.tbocek.android.combatmap.TokenDatabase.TagTreeNode;
import com.tbocek.android.combatmap.model.primitives.BaseToken;
import com.tbocek.android.combatmap.model.primitives.Util;
import com.tbocek.android.combatmap.view.interaction.CombatViewInteractionMode;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

public class TagNavigator extends ScrollView {
	
    /**
     * Default text size to use in child views.
     */
    private static final int DEFAULT_TEXT_SIZE = 20;

    /**
     * Number of pixels to pad the top and bottom of each text view with.
     */
    private static final int VERTICAL_PADDING = 8;

	private static final int COLOR_DEFAULT = Color.GRAY;
	private static final int COLOR_SELECTED = Color.WHITE;
	private static final int COLOR_DRAG_TARGET = Util.ICS_BLUE;
	


    /**
     * The number of text views to create along with this parent view.
     * We create an initial text view pool because creating and destroying
     * objects constantly does not work with dragging and dropping; 
     * newly created views during a drag are not registered as drag
     * targets.
     */
    private static final int INITIAL_TEXT_VIEW_POOL = 40;
    
    
	private LinearLayout mChildTagList;
	private ImageButton mBackButton;
	private TextView mCurrentTag;
	private TokenDatabase mTokenDatabase;
	private TokenDatabase.TagTreeNode mCurrentTagTreeNode;
	
	/**
	 * Whether the tag navigator should show tags that were marked as inactive.
	 */
	private boolean showInactiveTags = true;

	private List<TextView> mTextViews = Lists.newArrayList();
	
	/**
	 * The tag that was selected when a drag and drop operation started.
	 * Used to return the user to that tag when the D&D operation ends.
	 */
	private TokenDatabase.TagTreeNode mTagOnDragStart = null;
	
	private int mTextSize;
	private boolean mAllowContextMenu;

	public TagNavigator(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.tagnavigator, this);
		
		mChildTagList = (LinearLayout)this.findViewById(R.id.tagnavigator_current_tag_list);
		mBackButton = (ImageButton)this.findViewById(R.id.tagnavigator_back);
		mCurrentTag = (TextView)this.findViewById(R.id.tagnavigator_current_tag);
		this.setTextSize(DEFAULT_TEXT_SIZE);
		
		for (int i = 0; i < INITIAL_TEXT_VIEW_POOL; ++i) {
			TextView tv = createTextView();
			mTextViews.add(tv);
			mChildTagList.addView(tv);
		}
	}
	
	public void setTextSize(int size) {
		this.mTextSize = size;
		this.mCurrentTag.setTextSize(size);
		for (TextView existingView : mTextViews) {
			existingView.setTextSize(mTextSize);
		}
	}
	
	public void setAllowContextMenu(boolean allowContextMenu) {
		mAllowContextMenu = allowContextMenu;
		
		Activity activity = (Activity) this.getContext();
        activity.registerForContextMenu(mCurrentTag);
	}

	public void setTokenDatabase(TokenDatabase database) {
		selectTag(database.getRootNode(), true);
	}
	
	public TagTreeNode getCurrentTagNode() {
		return mCurrentTagTreeNode;
	}
	

	public String getCurrentTag() {
		return mCurrentTagTreeNode.getName();
	}
	
	public String getCurrentTagPath() {
		return mCurrentTagTreeNode.getPath();
	}
	
	public void setShowInactiveTags(boolean show) {
		this.showInactiveTags = show;
	}
	
	private void loadTokenData(TagTreeNode node) {
		mCurrentTagTreeNode = node;
		boolean root = node.getParent() == null;
		mBackButton.setVisibility(root ? View.GONE : View.VISIBLE);
		mCurrentTag.setText(node.getName());
		
		//TODO: should only have to set these once.
		mCurrentTag.setOnClickListener(new TagLabelClickedListener());
		mBackButton.setOnClickListener(new TagLabelClickedListener());
		
		mCurrentTag.setOnDragListener(new OnDragListener());
		mCurrentTag.setTag(node);
		mBackButton.setOnDragListener(new OnDragListener());
		mBackButton.setTag(node.getParent());
		
		List<String> tagNames = Lists.newArrayList(node.getTagNames());
		if (!this.showInactiveTags) {
			List<String> activeTagNames = Lists.newArrayList();
			for (String tag: tagNames) {
				if (node.getNamedChild(tag, false).isActive()) {
					activeTagNames.add(tag);
				}
			}
			tagNames = activeTagNames;
		}
		
		
		Collections.sort(tagNames, new Comparator<String>() {
		    @Override
		    public int compare(String o1, String o2) {              
		        return o1.compareToIgnoreCase(o2);
		    }});
		
		
		
		// Make sure there are enough text views to go around.
		for (int i = mTextViews.size(); i < tagNames.size(); ++i) {
			TextView tv = createTextView();
			mTextViews.add(tv);
			mChildTagList.addView(tv);
		}
		
		for (int i = 0; i < mTextViews.size(); ++i) {
			TextView tv = mTextViews.get(i);
			if (i < tagNames.size()) {
				TagTreeNode child = node.getNamedChild(tagNames.get(i), false);
				tv.setText(child.getName());
				tv.setTag(child);
				tv.setVisibility(View.VISIBLE);
				if (node.getNamedChild(tagNames.get(i), false).isActive()) {
					tv.setPaintFlags(tv.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
				} else {
					tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
				}
			} else {
				tv.setVisibility(View.GONE);
			}
		}
		setTextViewColors();
	}
	
	private TextView createTextView() {
		TextView textView = new TextView(this.getContext());
		textView.setOnClickListener(new TagLabelClickedListener());
		textView.setTextSize(this.mTextSize);
		textView.setPadding(0, VERTICAL_PADDING, 0, VERTICAL_PADDING);
		if (mAllowContextMenu) {
			Activity activity = (Activity) this.getContext();
	        activity.registerForContextMenu(textView);
		}
		textView.setOnDragListener(new OnDragListener());
		textView.setVisibility(View.GONE);
		
		return textView;
	}
	
	private void setTextViewColors() {
		for (TextView v: this.mTextViews) {
			v.setTextColor(v.getText().equals(this.mCurrentTagTreeNode.getName()) ? COLOR_SELECTED : COLOR_DEFAULT);
		}
		this.mCurrentTag.setTextColor(mCurrentTag.getText().equals(this.mCurrentTagTreeNode.getName()) ? COLOR_SELECTED : COLOR_DEFAULT);
	}
	
	private void selectTag(TagTreeNode node, boolean updateColors) {
		if (mTagSelectedListener != null) {
			mTagSelectedListener.onTagSelected(node);
		}
		mCurrentTagTreeNode = node;
		if (node.hasChildren()) {
			loadTokenData(node);
		} else {
			if (updateColors) {
				setTextViewColors();
			}
		}
		
	}
	
	private class TagLabelClickedListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			selectTag((TagTreeNode) v.getTag(), true);
		}
	}
	
	private class OnDragListener implements View.OnDragListener {
        private Handler mLongDragHandler = new Handler();
        
        
        private class LongDragRunnable implements Runnable {
        	public TagTreeNode mNode;
			@Override
			public void run() {
				selectTag(mNode, false);
			}
        	
        }
        private LongDragRunnable mLongDragRunnable = new LongDragRunnable();
        
		@Override
		public boolean onDrag(View v, DragEvent event) {
            if (event.getAction() == DragEvent.ACTION_DROP) {
                @SuppressWarnings("unchecked")
                Collection<BaseToken> toAdd =
                        (Collection<BaseToken>) event.getLocalState();
                if (TagNavigator.this.mTagSelectedListener != null) {
                    TagNavigator.this.mTagSelectedListener
                            .onDragTokensToTag(toAdd, (TagTreeNode)v.getTag());
                }
                setTextViewColors();
                mLongDragHandler.removeCallbacks(this.mLongDragRunnable);
                return true;
            } else if (event.getAction() == DragEvent.ACTION_DRAG_ENTERED) {
            	try {
            		((TextView)v).setTextColor(COLOR_DRAG_TARGET);
            	} catch (Exception e) {
            		// Ignore - bad cast expected here
            	}
            	mLongDragRunnable.mNode = (TagTreeNode)v.getTag();
                mLongDragHandler.postDelayed(mLongDragRunnable, ViewConfiguration.getLongPressTimeout());
                return true;
            } else if (event.getAction() == DragEvent.ACTION_DRAG_EXITED) {
            	setTextViewColors();
                mLongDragHandler.removeCallbacks(this.mLongDragRunnable);
                return true;
            } else if (event.getAction() == DragEvent.ACTION_DRAG_STARTED) {
            	if (mTagOnDragStart == null) {
            		TagNavigator.this.mTagOnDragStart = TagNavigator.this.mCurrentTagTreeNode;
            	}
            	return true;
            } else if (event.getAction() == DragEvent.ACTION_DRAG_ENDED) {
            	if (mTagOnDragStart != null) {
            		TagNavigator.this.selectTag(TagNavigator.this.mTagOnDragStart, true);
            		mTagOnDragStart = null;
            	}
            	return true;
            }
            return true;
		}
		
	}
	
	
	public interface TagSelectedListener {
		
        /**
         * Called when the user clicks on a tag.
         * 
         * @param selectedTag
         *            The tag clicked on.
         */
		void onTagSelected(TagTreeNode selectedTag);

        /**
         * Called when the user drags a token onto a tag.
         * 
         * @param token
         *            The token that was dragged.
         * @param tag
         *            The tag that the token was dragged to.
         */
        void onDragTokensToTag(Collection<BaseToken> token, TagTreeNode tag);
	}
	private TagSelectedListener mTagSelectedListener = null;
	public void setTagSelectedListener(TagSelectedListener listener) {
		mTagSelectedListener = listener;
	}

	public boolean isViewAChild(View v) {
		return (v == this.mCurrentTag) || this.mTextViews.contains(v);
	}

	public void selectRoot() {
		TagTreeNode n = this.mCurrentTagTreeNode;
		while (n.getParent() != null) {
			n = n.getParent();
		}
		selectTag(n, true);
	}

	public void setCurrentTagIsActive(boolean active) {
		this.getCurrentTagNode().setIsActive(active);
		selectTag(this.getCurrentTagNode(), true);
		for (TextView tv: mTextViews) {
			if (tv.getText().equals(this.getCurrentTagNode().getName())) {
				if (active) {
					tv.setPaintFlags(tv.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
				} else {
					tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
				}
			}
		}
	}

}
