package com.tbocek.android.combatmap.tokenmanager;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.tbocek.android.combatmap.CombatMap;
import com.tbocek.android.combatmap.DeveloperMode;
import com.tbocek.android.combatmap.Help;
import com.tbocek.android.combatmap.R;
import com.tbocek.android.combatmap.TextPromptDialog;
import com.tbocek.android.combatmap.TokenDatabase;
import com.tbocek.android.combatmap.TokenDatabase.TagTreeNode;
import com.tbocek.android.combatmap.model.MultiSelectManager;
import com.tbocek.android.combatmap.model.primitives.BaseToken;
import com.tbocek.android.combatmap.view.GridLayout;
import com.tbocek.android.combatmap.view.TagNavigator;
import com.tbocek.android.combatmap.view.TokenButton;
import com.tbocek.android.combatmap.view.TokenLoadTask;

/**
 * This activity lets the user view their library of tokens and manage which
 * tags apply to which tokens.
 * 
 * @author Tim Bocek
 * 
 */
public final class TokenManager extends SherlockActivity {

    /**
     * The minimum number of tokens to display in the grid across the smallest
     * screen dimension.
     */
    private static final int MINIMUM_TOKENS_SHOWN = 3;

    /**
     * The width and height of each token button.
     */
    private static final int TOKEN_BUTTON_SIZE = 150;

    /**
     * The list of token buttons that are managed by this activity.
     */
    private Collection<TokenButton> mButtons = Lists.newArrayList();

    /**
     * When a context menu for a tag is opened, stores the tag that the menu
     * opened on.
     */
    private String mContextMenuTag;

    private MenuItem mDeleteTagMenuItem;

    /**
     * The action mode that was started to manage the selection.
     */
    private ActionMode mMultiSelectActionMode;
    
    private TagNavigator.TagSelectedListener mTagSelectedListener = 
    		new TagNavigator.TagSelectedListener() {
				@Override
				public void onTagSelected(TagTreeNode selectedTag) {
					TokenManager.this.setScrollViewTag(selectedTag.getPath());
				}

				@Override
				public void onDragTokensToTag(Collection<BaseToken> tokens,
						TagTreeNode tag) {
		            for (BaseToken t : tokens) {
		            	if (!tag.isSystemTag()) {
			                TokenManager.this.mTokenDatabase.tagToken(
			                        t.getTokenId(), tag.getPath());
			                TokenManager.this.setScrollViewTag(tag.getPath());
		            	} else {
		            		Toast toast = Toast.makeText(
		            				TokenManager.this, 
		            				"Cannot add token to tag " + tag.getName(), 
		            				Toast.LENGTH_LONG);
		            		toast.show();
		            	}
		            }
					
				}
			};

    /**
     * Scroll view that wraps the layout of tokens for the currently selected
     * tag and allows it to scroll.
     */
    private ScrollView mScrollView;

    boolean mSuspendViewUpdates = false;
    
    private TagNavigator mTagNavigator;

    /**
     * Whether token tags are loaded into the action bar as tabs.
     */
    private boolean mTagsInActionBar = false;

    /**
     * The database to load tags and tokens from.
     */
    private TokenDatabase mTokenDatabase;

    /**
     * Factory that creates views to display tokens and implements a caching
     * scheme.
     */
    private MultiSelectTokenViewFactory mTokenViewFactory;

    /**
     * Drag target to delete tokens.
     */
    private TokenDeleteButton mTrashButton;

    private String tagFromActionBar = null;

	private MenuItem mTagActiveMenuItem;

    /**
     * Deletes the given list of tokens.
     * 
     * @param tokens
     *            The tokens to delete.
     */
    private void deleteTokens(Collection<BaseToken> tokens) {
        for (BaseToken token : tokens) {
            this.mTokenDatabase.removeToken(token);
            try {
                token.maybeDeletePermanently();
            } catch (IOException e) {
                Toast toast =
                        Toast.makeText(
                                this.getApplicationContext(),
                                "Did not delete the token, probably because "
                                        + "the external storage isn't writable."
                                        + e.toString(), Toast.LENGTH_LONG);
                toast.show();
            }
        }
        this.setScrollViewTag(this.mTagNavigator.getCurrentTag());
    }

    /**
     * @return
     */
    private String getActiveTag() {
        if (this.mTagsInActionBar) {
            return this.tagFromActionBar;
        } else {
            return this.mTagNavigator.getCurrentTag();
        }
    }
    
    private String getActiveTagPath() {
        if (this.mTagsInActionBar) {
            return this.tagFromActionBar;
        } else {
            return this.mTagNavigator.getCurrentTagPath();
        }
    }

    /**
     * Given a list of tokens, creates views representing the tokens and lays
     * them out in a table.
     * 
     * @param tokens
     *            The tokens to represent in the view.
     * @return Composite view that lays out buttons representing all tokens.
     */
    private View getTokenButtonLayout(final Collection<BaseToken> tokens) {
        GridLayout grid = new GridLayout(this);
        grid.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        int smallerDimension =
                Math.min(
                        this.getWindowManager().getDefaultDisplay().getWidth(),
                        this.getWindowManager().getDefaultDisplay().getHeight());

        // Make tokens at most TOKEN_BUTTON_SIZE DiP large, but fit at least
        // three across the smallest screen dimension.
        int cellDimension =
                Math.min(smallerDimension / MINIMUM_TOKENS_SHOWN,
                        (int) (TOKEN_BUTTON_SIZE * this.getResources()
                                .getDisplayMetrics().density));
        grid.setCellDimensions(cellDimension, cellDimension);

        this.mButtons = Lists.newArrayList();
        for (BaseToken t : tokens) {
            TokenButton b =
                    (TokenButton) this.mTokenViewFactory.getTokenView(t);
            b.setShouldDrawDark(true);
            b.allowDrag(!this.mTagsInActionBar);
            this.mButtons.add(b);

            // Remove all views from the parent, if there is one.
            // This is safe because we are totally replacing the view contents
            // here.
            ViewGroup parent = (ViewGroup) b.getParent();
            if (parent != null) {
                parent.removeAllViews();
            }

            b.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            grid.addView(b);
        }
        new TokenLoadTask(this.mButtons).execute();
        return grid;
    }

    private boolean isLargeScreen() {
        int layout = this.getResources().getConfiguration().screenLayout;
        int layoutSize = layout & Configuration.SCREENLAYOUT_SIZE_MASK;
        return layoutSize == Configuration.SCREENLAYOUT_SIZE_LARGE
                || layoutSize == Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    @Override
    public boolean onContextItemSelected(final android.view.MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.token_delete_entire_token) {
            this.deleteTokens(this.mTrashButton.getManagedTokens());
            return true;
        } else if (itemId == R.id.token_delete_from_tag) {
            this.removeTagFromTokens(this.mTrashButton.getManagedTokens(),
                    this.mTagNavigator.getCurrentTagPath());
            return true;
        } else if (itemId == R.id.tag_context_menu_delete) {
            if (this.mTagNavigator.getCurrentTag().equals(this.mContextMenuTag)) {
                this.mTagNavigator.selectRoot();
            }
            this.mTokenDatabase.deleteTag(this.mContextMenuTag);
            this.updateTagList();
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        DeveloperMode.strictMode();
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.token_manager_layout);

        this.mTokenViewFactory = new MultiSelectTokenViewFactory(this);
        
        this.mTagNavigator = new TagNavigator(this);
        this.mTagNavigator.setTagSelectedListener(this.mTagSelectedListener);
        this.mTagNavigator.setAllowContextMenu(true);

        // On large screens, set up a seperate column of token tags and possibly
        // set up a trash can to drag tokens too if drag&drop is an option on
        // the platform.
        // Otherwise, use tabs in the action bar to display & select token tags.
        if (this.isLargeScreen()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                this.mTrashButton = new TokenDeleteButton(this);
                this.registerForContextMenu(this.mTrashButton);
                ((FrameLayout) this
                        .findViewById(R.id.token_manager_delete_button_frame))
                        .addView(this.mTrashButton);
            } else {
                // If not Honeycomb, no need for the trash button.
                this.findViewById(R.id.token_manager_delete_button_frame)
                .setVisibility(View.GONE);
            }

            FrameLayout tagListFrame =
                    (FrameLayout) this
                    .findViewById(R.id.token_manager_taglist_frame);
            tagListFrame.addView(this.mTagNavigator);
        } else {
            this.mTagsInActionBar = true;
            this.getSupportActionBar().setNavigationMode(
                    ActionBar.NAVIGATION_MODE_LIST);
            this.getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        this.mScrollView =
                (ScrollView) this.findViewById(R.id.token_manager_scroll_view);

        this.mTokenViewFactory.getMultiSelectManager()
        .setSelectionChangedListener(
                new MultiSelectManager.SelectionChangedListener() {

                    @Override
                    public void selectionChanged() {
                        int numTokens =
                                TokenManager.this.mTokenViewFactory
                                .getMultiSelectManager()
                                .getSelectedTokens().size();
                        if (TokenManager.this.mMultiSelectActionMode != null) {
                            TokenManager.this.mMultiSelectActionMode
                            .setTitle(Integer
                                    .toString(numTokens)
                                    + (numTokens == 1
                                    ? " Token "
                                            : " Tokens ")
                                            + "Selected.");
                        }
                    }

                    @Override
                    public void selectionEnded() {
                        if (TokenManager.this.mMultiSelectActionMode != null) {
                            ActionMode m =
                                    TokenManager.this.mMultiSelectActionMode;
                            TokenManager.this.mMultiSelectActionMode =
                                    null;
                            m.finish();
                        }

                        for (TokenButton b : TokenManager.this.mButtons) {
                            MultiSelectTokenButton msb =
                                    (MultiSelectTokenButton) b;
                            msb.setSelected(false);
                        }
                    }

                    @Override
                    public void selectionStarted() {
                        TokenManager.this.mMultiSelectActionMode =
                                TokenManager.this
                                .startActionMode(new TokenSelectionActionModeCallback());
                    }
                });

    }

    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v,
            final ContextMenuInfo menuInfo) {
        if (this.mTagNavigator.isViewAChild(v)) {
            TextView tv = (TextView) v;
            this.mContextMenuTag = tv.getText().toString();
            this.getMenuInflater().inflate(R.menu.tag_context_menu, menu);
        }
        if (v == this.mTrashButton) {
            Collection<BaseToken> tokens = this.mTrashButton.getManagedTokens();
            if (tokens.size() > 0) {
                String deleteText = "";
                if (tokens.size() == 1) {
                    deleteText = this.getString(R.string.delete_token);
                } else {
                    deleteText =
                            "Delete " + Integer.toString(tokens.size())
                            + " Tokens";
                }

                menu.add(Menu.NONE, R.id.token_delete_entire_token, Menu.NONE,
                        deleteText);
            }

            if (!this.mTagNavigator.getCurrentTag().equals(TokenDatabase.ALL)) {
                String removeText =
                        "Remove '" + this.mTagNavigator.getCurrentTag() + "' Tag";

                if (tokens.size() > 1) {
                    removeText +=
                            " from " + Integer.toString(tokens.size())
                            + " tokens";
                }
                menu.add(Menu.NONE, R.id.token_delete_from_tag, Menu.NONE,
                        removeText);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = this.getSupportMenuInflater();
        inflater.inflate(R.menu.token_manager_menu, menu);
        this.mDeleteTagMenuItem = menu.findItem(R.id.token_manager_delete_tag);
        this.mTagActiveMenuItem = menu.findItem(R.id.token_manager_is_active);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.token_manager_new_token) {
            this.startActivity(new Intent(this, TokenCreator.class));
            return true;
        } else if (itemId == R.id.token_manager_new_tag) {
            Intent i = new Intent(this, NewTagDialog.class);
            i.putExtra(NewTagDialog.SELECTED_TAG_PATH, this.getActiveTagPath());
            this.startActivity(i);
            return true;
        } else if (itemId == R.id.token_manager_delete_tag) {
            // Confirm the tag deletion.
            new AlertDialog.Builder(this)
            .setMessage(
                    "Really delete the " + this.getActiveTag()
                    + " tag?  This won't delete any tokens.")
                    .setCancelable(false)
                    .setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog,
                                int which) {
                            TokenManager.this.mTokenDatabase
                            .deleteTag(TokenManager.this
                                    .getActiveTagPath());
                            TokenManager.this.updateTagList();
                        }
                    })
                    .setNegativeButton("No",
                            new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog,
                                int which) {
                            dialog.cancel();

                        }
                    }).create().show();
            return true;
        } else if (itemId == R.id.token_manager_help) {
            Help.openHelp(this);
            return true;
        } else if (itemId == android.R.id.home) {
            // app icon in action bar clicked; go home
            Intent intent = new Intent(this, CombatMap.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            this.startActivity(intent);
            return true;
        } else if (itemId == R.id.token_manager_is_active) {
        	mTagActiveMenuItem.setChecked(!mTagActiveMenuItem.isChecked());
        	TokenManager.this.mTagNavigator.setCurrentTagIsActive(mTagActiveMenuItem.isChecked());
        	return true;
        } else {
            return false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            this.mTokenDatabase.save(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        this.mTokenDatabase =
                TokenDatabase.getInstance(this.getApplicationContext());
        this.updateTagList();
        Debug.stopMethodTracing();
    }

    /**
     * Removes the given tag from the given tokens.
     * 
     * @param tokens
     *            Tokens to remove the tag from.
     * @param tag
     *            The tag to remove.
     */
    private void removeTagFromTokens(Collection<BaseToken> tokens, String tag) {
        for (BaseToken token : tokens) {
            this.mTokenDatabase.removeTagFromToken(token.getTokenId(), tag);
            this.setScrollViewTag(tag);
        }
    }

    /**
     * Sets the tag that the scroll view will display tokens from.
     * 
     * @param tag
     *            The new tag to display tokens for.
     */
    private void setScrollViewTag(final String tag) {
        if (!this.mSuspendViewUpdates) {
            this.mTokenViewFactory.getMultiSelectManager().selectNone();
            this.mScrollView.removeAllViews();
            if (tag.equals(TokenDatabase.ALL)) {
                this.mScrollView.addView(this
                        .getTokenButtonLayout(this.mTokenDatabase
                                .getAllTokens()));
                if (this.mDeleteTagMenuItem != null) {
                    this.mDeleteTagMenuItem.setVisible(false);
                }
                if (this.mTagActiveMenuItem != null) {
                	this.mTagActiveMenuItem.setVisible(false);
                }	
            } else {
                this.mScrollView.addView(this
                        .getTokenButtonLayout(this.mTokenDatabase
                                .getTokensForTag(tag)));
                this.mDeleteTagMenuItem.setVisible(true);
                this.mTagActiveMenuItem.setChecked(mTokenDatabase.isTagActive(tag));
                this.mTagActiveMenuItem.setVisible(true);
            }
            
        }
    }

    private void setSuspendViewUpdates(boolean b) {
        this.mSuspendViewUpdates = b;

    }

    private void updateTagList() {
        if (this.mTagsInActionBar) {
            ActionBar bar = this.getSupportActionBar(); // bar bar bar..
            List<String> tags = this.mTokenDatabase.getTags();

            ArrayAdapter<String> adapter =
                    new ArrayAdapter<String>(this,
                            android.R.layout.simple_spinner_item);
            adapter.add("All Tokens");
            adapter.addAll(tags);
            int oldIndex = tags.indexOf(this.tagFromActionBar) + 1; // If not
            // found,
            // will
            // result in
            // "All Tokens"
            // which is
            // just what
            // we want.
            bar.setListNavigationCallbacks(adapter, new TagNavigationListener(
                    tags));
            this.setSuspendViewUpdates(oldIndex > 0); // If we are staying on
            // the same tag (i.e. not
            // changing back to
            // "All Tokens" because of
            // a deleted tag), do not
            // refresh the view so we
            // don't lose the scroll
            // position.
            bar.setSelectedNavigationItem(oldIndex);
            this.setSuspendViewUpdates(false);
        } else {
            this.mTagNavigator.setTokenDatabase(this.mTokenDatabase);
        }
    }

    private class TagNavigationListener implements
    ActionBar.OnNavigationListener {
        List<String> mTags;

        public TagNavigationListener(List<String> tags) {
            this.mTags = tags;
        }

        @Override
        public boolean onNavigationItemSelected(int itemPosition, long itemId) {
            if (itemPosition == 0) {
                TokenManager.this.setScrollViewTag(TokenDatabase.ALL);
                TokenManager.this.tagFromActionBar = TokenDatabase.ALL;
            } else {
                TokenManager.this.setScrollViewTag(this.mTags
                        .get(itemPosition - 1));
                TokenManager.this.tagFromActionBar =
                        this.mTags.get(itemPosition - 1);
            }
            return true;
        }
    }

    /**
     * Callback defining an action mode for selecting multiple tokens.
     * 
     * @author Tim
     * 
     */
    private class TokenSelectionActionModeCallback implements
    ActionMode.Callback {

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            final Collection<BaseToken> tokens =
                    TokenManager.this.mTokenViewFactory.getMultiSelectManager()
                    .getSelectedTokens();
            int itemId = item.getItemId();
            if (itemId == R.id.token_manager_action_mode_remove_tag) {
                TokenManager.this.removeTagFromTokens(tokens,
                        TokenManager.this.getActiveTagPath());
                return true;
            } else if (itemId == R.id.token_manager_action_mode_delete) {
                TokenManager.this.deleteTokens(tokens);
                return true;
            } else if (itemId == R.id.token_manager_action_mode_add_to_tag) {
                List<String> tags = TokenManager.this.mTokenDatabase.getTags();
                final ArrayAdapter<String> adapter =
                        new ArrayAdapter<String>(TokenManager.this,
                                R.layout.selection_dialog_text_view);
                adapter.addAll(tags);
                new AlertDialog.Builder(TokenManager.this)
                .setTitle("Select a Tag")
                .setAdapter(adapter,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                            int which) {
                        Set<String> tags =
                                Sets.newHashSet(adapter
                                        .getItem(which));

                        for (BaseToken token : tokens) {
                            TokenManager.this.mTokenDatabase
                            .tagToken(token, tags);
                        }
                    }
                }).create().show();
                return true;
            } else {
            	
            }
            return false;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.token_manager_action_mode,
                    menu);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            TokenManager.this.mTokenViewFactory.getMultiSelectManager()
            .selectNone();
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            MenuItem removeTag =
                    menu.findItem(R.id.token_manager_action_mode_remove_tag);
            removeTag.setVisible(!TokenManager.this.getActiveTag().equals(
                    TokenDatabase.ALL));
            removeTag.setTitle("Remove tag '"
                    + TokenManager.this.getActiveTag() + "'");
            return true;
        }

    }
}
