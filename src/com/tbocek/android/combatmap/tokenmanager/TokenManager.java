package com.tbocek.android.combatmap.tokenmanager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.tbocek.android.combatmap.CombatMap;
import com.tbocek.android.combatmap.DeveloperMode;
import com.tbocek.android.combatmap.R;
import com.tbocek.android.combatmap.TextPromptDialog;
import com.tbocek.android.combatmap.TokenDatabase;
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
public final class TokenManager extends Activity {
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
	    	mTrashButton = new TokenDeleteButton(this);
	    	this.registerForContextMenu(mTrashButton);
	    	((FrameLayout) this.findViewById(
	    			R.id.token_manager_delete_button_frame))
	    			.addView(mTrashButton);
        }


    	FrameLayout tagListFrame =
    		(FrameLayout) this.findViewById(R.id.token_manager_taglist_frame);
    	tagListFrame.addView(mTagListView);

    	mScrollView =
    		(ScrollView) this.findViewById(R.id.token_manager_scroll_view);

    }

	@Override
	public void onResume() {
		super.onResume();
		mTokenDatabase = TokenDatabase.getInstance(this.getApplicationContext());
		mTagListView.setTagList(mTokenDatabase.getTags());
    	Debug.stopMethodTracing();
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
		
		ArrayList<TokenButton> allButtons = new ArrayList<TokenButton>();
		for (BaseToken t : tokens) {
			TokenButton b = (TokenButton) mTokenViewFactory.getTokenView(t);
			b.setShouldDrawDark(true);
			allButtons.add(b);

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
		new TokenLoadTask(allButtons).execute();
		return grid;
	}

	@Override
    public boolean onCreateOptionsMenu(final Menu menu) {
    	MenuInflater inflater = getMenuInflater();
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
					mTagListView.setTagList(mTokenDatabase.getTags());
				}
			}, "New Tag", "Create");
    	default:
        	return null;

    	}
    }


    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v,
                                    final ContextMenuInfo menuInfo) {
      	if (v == this.mTrashButton) {
      		Collection<BaseToken> tokens = this.mTrashButton.getManagedTokens();
      		int builtInTokens = countBuiltInTokens(tokens);
      		int customTokens = tokens.size() - builtInTokens;
      		if (customTokens > 0) {
      			String deleteText = "";
      			if (customTokens == 1) {
      				deleteText = "Delete Token";
      			} else {
      				deleteText = "Delete " + Integer.toString(customTokens)
      					+ " Tokens";
      			}

      			if (builtInTokens == 1) {
      				deleteText += " (1 token can't be deleted)";
      			} else if (builtInTokens > 1) {
      				deleteText += " (" + Integer.toString(builtInTokens)
      					+ " tokens can't be deleted)";
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

    /**
     * Given a list of tokens, provides a count of the number that are built in.
     * @param tokens The tokens to examine.
     * @return The number of tokens that are built in.
     */
    private int countBuiltInTokens(Collection<BaseToken> tokens) {
    	int count = 0;
    	for (BaseToken t : tokens) {
    		if (t.isBuiltIn()) {
    			count++;
    		}
    	}
    	return count;
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
    	//TODO: Move more of this functionality into TokenDeleteButton.
    	switch (item.getItemId()) {
    	case R.id.token_delete_entire_token:
    		Collection<BaseToken> tokens = this.mTrashButton.getManagedTokens();
    		for (BaseToken token : tokens) {
    			if (!token.isBuiltIn()) {
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
    		}
    		setScrollViewTag(this.mTagListView.getTag());
    		return true;
    	case R.id.token_delete_from_tag:
    		tokens = this.mTrashButton.getManagedTokens();
    		String tag = this.mTagListView.getTag();
    		for (BaseToken token : tokens) {
	    		this.mTokenDatabase.removeTagFromToken(token.getTokenId(), tag);
	    		setScrollViewTag(tag);
    		}
	    	return true;
    	default:
    		return super.onContextItemSelected(item);
    	}
    }
}
