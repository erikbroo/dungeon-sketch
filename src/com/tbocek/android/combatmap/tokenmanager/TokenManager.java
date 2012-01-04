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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.tbocek.android.combatmap.DeveloperMode;
import com.tbocek.android.combatmap.R;
import com.tbocek.android.combatmap.TextPromptDialog;
import com.tbocek.android.combatmap.TokenDatabase;
import com.tbocek.android.combatmap.model.primitives.BaseToken;
import com.tbocek.android.combatmap.model.primitives.BuiltInImageToken;
import com.tbocek.android.combatmap.view.TagListView;
import com.tbocek.android.combatmap.view.TokenButton;
import com.tbocek.android.combatmap.view.TokenLoadTask;
import com.tbocek.android.combatmap.view.TokenViewFactory;

/**
 * This activity lets the user view their library of tokens and manage which
 * tags apply to which tokens.
 * @author Tim Bocek
 *
 */
public final class TokenManager extends Activity {
	/**
	 * The proportion of the screen that the token layout should take up.
	 */
	private static final float TOKEN_LAYOUT_RELATIVE_SIZE = .75f;

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
	private TagListView tagListView;

	/**
	 * The database to load tags and tokens from.
	 */
    private TokenDatabase tokenDatabase;

    /**
     * Scroll view that wraps the layout of tokens for the currently selected
     * tag and allows it to scroll.
     */
    private ScrollView scrollView;

    /**
     * Drag target to delete tokens.
     */
    private TokenDeleteButton trashButton;

    /**
     * Factory that creates views to display tokens and implements a caching
     * scheme.
     */
    private MultiSelectTokenViewFactory mTokenViewFactory;

    /**
     * Listener that reloads the token view when a new tag is selected, and
     * tags a token when a token is dragged onto that tag.
     */
    private TagListView.OnTagListActionListener onTagListActionListener =
    	new TagListView.OnTagListActionListener() {
		@Override
		public void onChangeSelectedTag(final String newTag) {
			setScrollViewTag(newTag);
		}

		@Override
		public void onDragTokensToTag(
				final Collection<BaseToken> tokens, final String tag) {
			for (BaseToken t : tokens) {
				tokenDatabase.tagToken(t.getTokenId(), tag);
			}

		}
    };

    /**
     * Sets the tag that the scroll view will display tokens from.
     * @param tag The new tag to display tokens for.
     */
	private void setScrollViewTag(final String tag) {
		mTokenViewFactory.getMultiSelectManager().selectNone();
		scrollView.removeAllViews();
		Collection<BaseToken> tokens = null;
		if (tag == TokenDatabase.ALL) {
			scrollView.addView(
					getTokenButtonLayout(tokenDatabase.getAllTokens()));
		} else {
			scrollView.addView(
					getTokenButtonLayout(tokenDatabase.getTokensForTag(tag)));
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

    	tagListView = new TagListView(this);
    	tagListView.setOnTagListActionListener(onTagListActionListener);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
	    	trashButton = new TokenDeleteButton(this);
	    	this.registerForContextMenu(trashButton);
	    	((FrameLayout) this.findViewById(
	    			R.id.token_manager_delete_button_frame))
	    			.addView(trashButton);
        }


    	FrameLayout tagListFrame =
    		(FrameLayout) this.findViewById(R.id.token_manager_taglist_frame);
    	tagListFrame.addView(tagListView);

    	scrollView =
    		(ScrollView) this.findViewById(R.id.token_manager_scroll_view);

    }

	/**
	 * @return Width of the activity for layout purposes.
	 */
	private int getWidth() {
		return this.getWindowManager().getDefaultDisplay().getWidth();
	}


	@Override
	public void onResume() {
		super.onResume();
		tokenDatabase = TokenDatabase.getInstance(this.getApplicationContext());
		tagListView.setTagList(tokenDatabase.getTags());
    	Debug.stopMethodTracing();
	}

	@Override
	public void onPause() {
		super.onPause();
		try {
			tokenDatabase.save(this);
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
		int width =  (int) (TOKEN_LAYOUT_RELATIVE_SIZE * this.getWidth());
		int tokenWidth = TOKEN_BUTTON_SIZE;
		int tokenHeight = TOKEN_BUTTON_SIZE;
		int tokensPerRow = width / tokenWidth;


		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		LinearLayout currentRow = null;
		ArrayList<TokenButton> allButtons = new ArrayList<TokenButton>();
		int i = 0;
		for (BaseToken t : tokens) {
			TokenButton b = (TokenButton) mTokenViewFactory.getTokenView(t);
			b.setShouldDrawDark(true);
			allButtons.add(b);

			// Remove all views from the parent, if there is one.
			// This is safe because we are totally replacing the view contents
			// here.
			LinearLayout parent = (LinearLayout) b.getParent();
			if (parent != null) {
				parent.removeAllViews();
			}

			b.setLayoutParams(
					new LinearLayout.LayoutParams(tokenWidth, tokenHeight));
			if (i % tokensPerRow == 0) {
				currentRow = new LinearLayout(this);
				currentRow.setOrientation(LinearLayout.HORIZONTAL);
				layout.addView(currentRow);
			}
			currentRow.addView(b);
			++i;
		}
		new TokenLoadTask(allButtons).execute();
		return layout;
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
					tokenDatabase.addEmptyTag(text);
					tagListView.setTagList(tokenDatabase.getTags());
				}
			}, "New Tag", "Create");
    	default:
        	return null;

    	}
    }


    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v,
                                    final ContextMenuInfo menuInfo) {
      	if (v == this.trashButton) {
      		Collection<BaseToken> tokens = this.trashButton.getManagedTokens();
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

      		if (this.tagListView.getTag() != TokenDatabase.ALL) {
      			String removeText = "Remove '" + tagListView.getTag() + "' Tag";

      			if (tokens.size() > 1) {
      				removeText += " from " + Integer.toString(tokens.size())
      					+ " tokens";
      			}
      			menu.add(Menu.NONE, R.id.token_delete_from_tag, Menu.NONE,
      					 removeText);
      		}
      	}
    }

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
    		Collection<BaseToken> tokens = this.trashButton.getManagedTokens();
    		for (BaseToken token : tokens) {
    			if (!token.isBuiltIn()) {
		    		this.tokenDatabase.removeToken(token);
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
    		setScrollViewTag(this.tagListView.getTag());
    		return true;
    	case R.id.token_delete_from_tag:
    		tokens = this.trashButton.getManagedTokens();
    		String tag = this.tagListView.getTag();
    		for (BaseToken token : tokens) {
	    		this.tokenDatabase.removeTagFromToken(token.getTokenId(), tag);
	    		setScrollViewTag(tag);
    		}
	    	return true;
    	default:
    		return super.onContextItemSelected(item);
    	}
    }
}
