package com.tbocek.android.combatmap.tokenmanager;

import java.io.IOException;
import java.util.Collection;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.support.v4.app.FragmentTransaction;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuInflater;
import com.google.common.collect.Lists;
import com.tbocek.android.combatmap.CombatMap;
import com.tbocek.android.combatmap.DeveloperMode;
import com.tbocek.android.combatmap.R;
import com.tbocek.android.combatmap.TextPromptDialog;
import com.tbocek.android.combatmap.TokenDatabase;
import com.tbocek.android.combatmap.model.MultiSelectManager;
import com.tbocek.android.combatmap.model.primitives.BaseToken;
import com.tbocek.android.combatmap.model.primitives.BuiltInImageToken;
import com.tbocek.android.combatmap.view.GridLayout;
import com.tbocek.android.combatmap.view.TagListView;
import com.tbocek.android.combatmap.view.TokenButton;
import com.tbocek.android.combatmap.view.TokenLoadTask;
/**
 * This activity lets the user view their library of tokens and manage which
 * tags apply to which tokens.
 * @author Tim Bocek
 *
 */
public final class TokenManager extends SherlockActivity {
	/**
	 * The width and height of each token button.
	 */
	private static final int TOKEN_BUTTON_SIZE = 150;

	/**
	 * ID for the dialog that will create a new tag.
	 */
    private static final int DIALOG_ID_NEW_TAG = 0;

    /**
     * View that displays tags in the token database.
     */
	private TagListView mTagListView;

	/**
	 * The database to load tags and tokens from.
	 */
    private TokenDatabase mTokenDatabase;

    /**
     * Scroll view that wraps the layout of tokens for the currently selected
     * tag and allows it to scroll.
     */
    private ScrollView mScrollView;

    /**
     * Drag target to delete tokens.
     */
    private TokenDeleteButton mTrashButton;

    /**
     * Factory that creates views to display tokens and implements a caching
     * scheme.
     */
    private MultiSelectTokenViewFactory mTokenViewFactory;
	
	/**
	 * The action mode that was started to manage the selection.
	 */
	private ActionMode mMultiSelectActionMode;	
	
	/**
	 * The list of token buttons that are managed by this activity.
	 */
	private Collection<TokenButton> mButtons = Lists.newArrayList();
	
	/**
	 * When a context menu for a tag is opened, stores the tag that the menu
	 * opened on.
	 */
	private String mContextMenuTag;
	
	/**
	 * Whether token tags are loaded into the action bar as tabs.
	 */
	private boolean mTagsInActionBar = false;

    /**
     * Listener that reloads the token view when a new tag is selected, and
     * tags a token when a token is dragged onto that tag.
     */
    private TagListView.OnTagListActionListener mOnTagListActionListener =
    	new TagListView.OnTagListActionListener() {
		@Override
		public void onChangeSelectedTag(final String newTag) {
			setScrollViewTag(newTag);
		}

		@Override
		public void onDragTokensToTag(
				final Collection<BaseToken> tokens, final String tag) {
			for (BaseToken t : tokens) {
				mTokenDatabase.tagToken(t.getTokenId(), tag);
			}
		}
    };

    /**
     * Sets the tag that the scroll view will display tokens from.
     * @param tag The new tag to display tokens for.
     */
	private void setScrollViewTag(final String tag) {
		mTokenViewFactory.getMultiSelectManager().selectNone();
		mScrollView.removeAllViews();
		if (tag.equals(TokenDatabase.ALL)) {
			mScrollView.addView(
					getTokenButtonLayout(mTokenDatabase.getAllTokens()));
		} else {
			mScrollView.addView(
					getTokenButtonLayout(mTokenDatabase.getTokensForTag(tag)));
		}
	}

	@Override
    public void onCreate(final Bundle savedInstanceState) {
    	DeveloperMode.strictMode();
		super.onCreate(savedInstanceState);

		setContentView(R.layout.token_manager_layout);

        BuiltInImageToken.registerResources(
        		this.getApplicationContext().getResources());

		mTokenViewFactory = new MultiSelectTokenViewFactory(this);

    	mTagListView = new TagListView(this);
    	mTagListView.setOnTagListActionListener(mOnTagListActionListener);
    	mTagListView.setRegisterChildrenForContextMenu(true);


        // On large screens, set up a seperate column of token tags and possibly
    	// set up a trash can to drag tokens too if drag&drop is an option on
    	// the platform.
        // Otherwise, use tabs in the action bar to display & select token tags.
        if (isLargeScreen()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
    	    	mTrashButton = new TokenDeleteButton(this);
    	    	this.registerForContextMenu(mTrashButton);
    	    	((FrameLayout) this.findViewById(
    	    			R.id.token_manager_delete_button_frame))
    	    			.addView(mTrashButton);
            } else {
            	// If not Honeycomb, no need for the trash button.
            	this.findViewById(R.id.token_manager_delete_button_frame).setVisibility(View.GONE);
            }
            
	    	FrameLayout tagListFrame =
	    		(FrameLayout) this.findViewById(R.id.token_manager_taglist_frame);
	    	tagListFrame.addView(mTagListView);
        } else {
        	this.mTagsInActionBar = true;
        	getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        	getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        
    	mScrollView =
    		(ScrollView) this.findViewById(R.id.token_manager_scroll_view);
    	
    	mTokenViewFactory.getMultiSelectManager().setSelectionChangedListener(
    			new MultiSelectManager.SelectionChangedListener() {
		
    		
			@Override
			public void selectionStarted() {
				mMultiSelectActionMode = startActionMode(
						new TokenSelectionActionModeCallback());
			}

			@Override
			public void selectionEnded() {
				if (mMultiSelectActionMode != null) {
					ActionMode m = mMultiSelectActionMode;
					mMultiSelectActionMode = null;
					m.finish();
				}
				
				for (TokenButton b: mButtons) {
					MultiSelectTokenButton msb = (MultiSelectTokenButton) b;
					msb.setSelected(false);
				}
			}

			@Override
			public void selectionChanged() {
				int numTokens = mTokenViewFactory.getMultiSelectManager()
						.getSelectedTokens().size();
				if (mMultiSelectActionMode != null) {
					mMultiSelectActionMode.setTitle(
							Integer.toString(numTokens) 
							+ (numTokens == 1 ? " Token " : " Tokens ")
							+ "Selected.");
				}
			}
		});

    }
	
	private boolean isLargeScreen() {
		int layout = getResources().getConfiguration().screenLayout;
		int layoutSize = layout & Configuration.SCREENLAYOUT_SIZE_MASK;
		return false;
		//return layoutSize == Configuration.SCREENLAYOUT_SIZE_LARGE ||
		//	   layoutSize == Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}


	@Override
	public void onResume() {
		super.onResume();
		mTokenDatabase = TokenDatabase.getInstance(this.getApplicationContext());
		updateTagList();
		Debug.stopMethodTracing();
	}
	
	private void updateTagList() {
		if (this.mTagsInActionBar) {
			ActionBar bar = this.getSupportActionBar(); // bar bar bar..
			bar.addTab(bar.newTab().setText("All Tokens").setTabListener(new TagSelectingTabListener(TokenDatabase.ALL)), true);
			for (String tag : mTokenDatabase.getTags()) {
				bar.addTab(bar.newTab().setText(tag).setTabListener(new TagSelectingTabListener(tag)));
			}
		} else {
			mTagListView.setTagList(mTokenDatabase.getTags());
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		try {
			mTokenDatabase.save(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Given a list of tokens, creates views representing the tokens and lays
	 * them out in a table.
	 * @param tokens The tokens to represent in the view.
	 * @return Composite view that lays out buttons representing all tokens.
	 */
	private View getTokenButtonLayout(final Collection<BaseToken> tokens) {
		GridLayout grid = new GridLayout(this);
		grid.setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT));
		int cellDimension = (int) (
				TOKEN_BUTTON_SIZE * getResources().getDisplayMetrics().density);
		grid.setCellDimensions(cellDimension, cellDimension);
		
		mButtons = Lists.newArrayList();
		for (BaseToken t : tokens) {
			TokenButton b = (TokenButton) mTokenViewFactory.getTokenView(t);
			b.setShouldDrawDark(true);
			b.allowDrag(!this.mTagsInActionBar);
			mButtons.add(b);

			// Remove all views from the parent, if there is one.
			// This is safe because we are totally replacing the view contents
			// here.
			ViewGroup parent = (ViewGroup) b.getParent();
			if (parent != null) {
				parent.removeAllViews();
			}

			b.setLayoutParams(
					new ViewGroup.LayoutParams(
							ViewGroup.LayoutParams.MATCH_PARENT, 
							ViewGroup.LayoutParams.MATCH_PARENT));
			grid.addView(b);
		}
		new TokenLoadTask(mButtons).execute();
		return grid;
	}

	@Override
    public boolean onCreateOptionsMenu(final Menu menu) {
    	MenuInflater inflater = getSupportMenuInflater();
    	inflater.inflate(R.menu.token_manager_menu, menu);
    	return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.token_manager_new_token:
    		startActivity(new Intent(this, TokenCreator.class));
    		return true;
    	case R.id.token_manager_new_tag:
    		showDialog(DIALOG_ID_NEW_TAG);
    		return true;
        case android.R.id.home:
            // app icon in action bar clicked; go home
            Intent intent = new Intent(this, CombatMap.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
    	default:
    		return false;
    	}
    }

    @Override
    public Dialog onCreateDialog(final int id) {
    	switch(id) {
    	case DIALOG_ID_NEW_TAG:
    		 return new TextPromptDialog(this,
    				 new TextPromptDialog.OnTextConfirmedListener() {
				public void onTextConfirmed(final String text) {
					mTokenDatabase.addEmptyTag(text);
					updateTagList();
				}
			}, getString(R.string.new_tag), getString(R.string.create));
    	default:
        	return null;

    	}
    }


    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v,
                                    final ContextMenuInfo menuInfo) {
    	if (mTagListView.isViewAChild(v)) {
    		TextView tv = (TextView) v;
    		this.mContextMenuTag = tv.getText().toString();
    		this.getMenuInflater().inflate(R.menu.tag_context_menu, menu);
    	}
      	if (v == this.mTrashButton) {
      		Collection<BaseToken> tokens = this.mTrashButton.getManagedTokens();
      		if (tokens.size() > 0) {
      			String deleteText = "";
      			if (tokens.size() == 1) {
      				deleteText = getString(R.string.delete_token);
      			} else {
      				deleteText = "Delete " + Integer.toString(tokens.size())
      					+ " Tokens";
      			}

	      		menu.add(Menu.NONE, R.id.token_delete_entire_token, Menu.NONE,
	      				deleteText);
      		}

      		if (!this.mTagListView.getTag().equals(TokenDatabase.ALL)) {
      			String removeText = "Remove '" + mTagListView.getTag() + "' Tag";

      			if (tokens.size() > 1) {
      				removeText += " from " + Integer.toString(tokens.size())
      					+ " tokens";
      			}
      			menu.add(Menu.NONE, R.id.token_delete_from_tag, Menu.NONE,
      					 removeText);
      		}
      	}
    }

    @Override
    public boolean onContextItemSelected(final android.view.MenuItem item) {
    	//TODO: Move more of this functionality into TokenDeleteButton.
    	switch (item.getItemId()) {
    	case R.id.token_delete_entire_token:
    		deleteTokens(this.mTrashButton.getManagedTokens());
    		return true;
    	case R.id.token_delete_from_tag:
    		removeTagFromTokens(
    				this.mTrashButton.getManagedTokens(),
    				this.mTagListView.getTag());
	    	return true;
    	case R.id.tag_context_menu_delete:
    		if (mTagListView.getTag().equals(mContextMenuTag)) {
    			mTagListView.setHighlightedTag(TokenDatabase.ALL);
    		}
    		this.mTokenDatabase.deleteTag(this.mContextMenuTag);
    		updateTagList();
    		return true;
    	default:
    		return super.onContextItemSelected(item);
    	}
    }
    
    /**
     * Removes the given tag from the given tokens.
     * @param tokens Tokens to remove the tag from.
     * @param tag The tag to remove.
     */
    private void removeTagFromTokens(Collection<BaseToken> tokens, String tag) {
		for (BaseToken token : tokens) {
    		this.mTokenDatabase.removeTagFromToken(token.getTokenId(), tag);
    		setScrollViewTag(tag);
		}
    }
    
    /**
     * Deletes the given list of tokens.
     * @param tokens The tokens to delete.
     */
    private void deleteTokens(Collection<BaseToken> tokens) {
		for (BaseToken token : tokens) {
	    	this.mTokenDatabase.removeToken(token);
    		try {
    			token.maybeDeletePermanently();
    		} catch (IOException e) {
    			Toast toast = Toast.makeText(
    					this.getApplicationContext(),
    					"Did not delete the token, probably because "
    					+ "the external storage isn't writable."
    					+ e.toString(),
    					Toast.LENGTH_LONG);
    			toast.show();
    		}
		}
		setScrollViewTag(this.mTagListView.getTag());
    }
    
    /**
     * Callback defining an action mode for selecting multiple tokens.
     * @author Tim
     *
     */
    private class TokenSelectionActionModeCallback 
    		implements ActionMode.Callback {

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			Collection<BaseToken> tokens =
					mTokenViewFactory.getMultiSelectManager()
							.getSelectedTokens();
			switch(item.getItemId()) {
			case R.id.token_manager_action_mode_remove_tag:
	    		removeTagFromTokens(tokens, getActiveTag());
		    	return true;
			case R.id.token_manager_action_mode_delete:
				deleteTokens(tokens);
	    		return true;
			default:
				break;
				
			}
			return false;
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			mode.getMenuInflater().inflate(
					R.menu.token_manager_action_mode, menu);
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mTokenViewFactory.getMultiSelectManager().selectNone();
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			MenuItem removeTag = menu.findItem(
					R.id.token_manager_action_mode_remove_tag);
			removeTag.setVisible(
					!getActiveTag().equals(TokenDatabase.ALL));
			removeTag.setTitle("Remove tag '" + getActiveTag() + "'");
			return true;
		}

    }
    
	/**
	 * @return
	 */
	private String getActiveTag() {
		if (this.mTagsInActionBar) {
			return tagFromActionBar;
		} else {
			return mTagListView.getTag();
		}
	}
	
	private String tagFromActionBar = null;
	
	/**
	 * TabListener for the ActionBar that selects tags.
	 * @author Tim
	 *
	 */
	private class TagSelectingTabListener implements ActionBar.TabListener {
		public TagSelectingTabListener(String tag) {
			mTag = tag;
		}
		private String mTag;
		
		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			
		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			setScrollViewTag(mTag);
			tagFromActionBar = mTag;
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
