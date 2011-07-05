package com.tbocek.android.combatmap.tokenmanager;

import java.io.IOException;
import java.util.Collection;

import com.tbocek.android.combatmap.DataManager;
import com.tbocek.android.combatmap.R;
import com.tbocek.android.combatmap.TextPromptDialog;
import com.tbocek.android.combatmap.TokenDatabase;
import com.tbocek.android.combatmap.graphicscore.BaseToken;
import com.tbocek.android.combatmap.view.TokenButton;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnDragListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class TokenManager extends Activity {
    private static final int DIALOG_ID_NEW_TOKEN = 0;
	TagListView tagListView;
    private TokenDatabase tokenDatabase;
	
    private ScrollView scrollView;
    
    private TokenDeleteButton trashButton;
    
    private TagListView.OnTagListActionListener onTagListActionListener = new TagListView.OnTagListActionListener() {
		@Override
		public void onChangeSelectedTag(String newTag) {
			reloadScrollView(newTag);
		}


		@Override
		public void onDragTokenToTag(BaseToken token, String tag) {
			tokenDatabase.tagToken(token.getTokenId(), tag);
			
		}
    };
    
	private void reloadScrollView(String tag) {
		scrollView.removeAllViews();
		if (tag == TagListView.ALL)
			scrollView.addView(getTokenButtonLayout(tokenDatabase.getAllTokens()));
		else
			scrollView.addView(getTokenButtonLayout(tokenDatabase.getTokensForTag(tag)));
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.token_manager_layout);
		
    	tagListView = new TagListView(this);
    	tagListView.setOnTagListActionListener(onTagListActionListener);
    	
    	trashButton = new TokenDeleteButton(this);
    	this.registerForContextMenu(trashButton);
    	((FrameLayout)this.findViewById(R.id.token_manager_delete_button_frame)).addView(trashButton);
    	
    	
    	FrameLayout tagListFrame = (FrameLayout) this.findViewById(R.id.token_manager_taglist_frame);
    	tagListFrame.addView(tagListView);
    	
    	scrollView = (ScrollView) this.findViewById(R.id.token_manager_scroll_view);
    	
    }

	private int getWidth() {
		return this.getWindowManager().getDefaultDisplay().getWidth();
	}
	
	private int getHeight() {
		return this.getWindowManager().getDefaultDisplay().getHeight();
	}

	
	@Override
	public void onResume() {
		super.onResume();
    	try {
    		tokenDatabase = TokenDatabase.load(this.getApplicationContext());
    	} catch (Exception e) {
    		tokenDatabase = new TokenDatabase();
    	}
    	
    	tagListView.setTagList(tokenDatabase.getTags());
    	scrollView.removeAllViews();
    	scrollView.addView(getTokenButtonLayout(tokenDatabase.getAllTokens()));
	}
	
	@Override
	public void onPause() {
		super.onPause();
		try {
			tokenDatabase.save(this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private View getTokenButtonLayout(Collection<BaseToken> tokens){
		int width = 3*this.getWidth()/4;
		int tokenWidth = 150;
		int tokenHeight = 150;
		int tokensPerRow = width/tokenWidth;
		
		
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		LinearLayout currentRow = null;
		int i = 0;
		for (BaseToken t : tokens) {
			TokenButton b = new TokenButton(this, t);
			b.setLayoutParams(new LinearLayout.LayoutParams(tokenWidth, tokenHeight));
			if (i%tokensPerRow == 0) {
				currentRow = new LinearLayout(this);
				currentRow.setOrientation(LinearLayout.HORIZONTAL);
				layout.addView(currentRow);
			}
			currentRow.addView(b);
			++i;
		}
		return layout;
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.token_manager_menu, menu);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.token_manager_new_token:
    		startActivity(new Intent(this, TokenCreator.class));
    		return true;
    	case R.id.token_manager_new_tag:
    		showDialog(DIALOG_ID_NEW_TOKEN);
    		return true;
    	}
    	return false;
    }
    
    @Override
    public Dialog onCreateDialog(int id) {
    	switch(id) {
    	case DIALOG_ID_NEW_TOKEN:
    		 return new TextPromptDialog(this, new TextPromptDialog.OnTextConfirmedListener() {
				public void onTextConfirmed(String text) {
					tokenDatabase.addEmptyTag(text);
					tagListView.setTagList(tokenDatabase.getTags());
				}
			}, "New Tag", "Create");
    	}
    	return null;
    }
    
	
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
      	if (v == this.trashButton) {
      		if (!this.trashButton.managedToken.isBuiltIn()) {
      			menu.add(Menu.NONE, R.id.token_delete_entire_token, Menu.NONE, "Delete Token");
      		}
      		if (this.tagListView.getTag() != TagListView.ALL) {
      			menu.add(Menu.NONE, R.id.token_delete_from_tag, Menu.NONE, "Remove '" + tagListView.getTag() + "' Tag");
      		}
      	}
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.token_delete_entire_token:
    		BaseToken token = this.trashButton.managedToken;
    		this.tokenDatabase.removeToken(token);
    		try {
    			token.maybeDeletePermanently();
    			reloadScrollView(this.tagListView.getTag());
    		} catch (IOException e) {
    			Toast toast = Toast.makeText(this.getApplicationContext(), "Did not delete the token, probably because the external storage isn't writable." + e.toString(), Toast.LENGTH_LONG);
    			toast.show();	
    		}
    		return true;
    	case R.id.token_delete_from_tag:
    		token = this.trashButton.managedToken;
    		String tag = this.tagListView.getTag();
    		this.tokenDatabase.removeTagFromToken(token.getTokenId(), tag);
    		reloadScrollView(tag);
    		return true;
    	default:
    		return super.onContextItemSelected(item);	
    	}
    }
}
