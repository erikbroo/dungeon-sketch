package com.tbocek.android.combatmap.tokenmanager;

import com.tbocek.android.combatmap.TokenDatabase;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;


public class NewTagDialog extends RoboActivity {
	public static String SELECTED_TAG_PATH = "SELECTED_TAG_PATH";
	@InjectView(tag="new_tag_name") TextView tagName;
	@InjectView(tag="new_tag_under_top_level") RadioButton underTopLevel;
	@InjectView(tag="new_tag_under_selected") RadioButton underSelected;
	@InjectView(tag="new_tag_create") Button create;
	
	private TokenDatabase mDatabase;
	private TokenDatabase.TagTreeNode mSelectedTag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	mDatabase = TokenDatabase.getInstance(this);
    	mSelectedTag = mDatabase.getRootNode().getNamedChild(this.getIntent().getStringExtra(SELECTED_TAG_PATH), false);
    	
    	if (mSelectedTag.getParent() == null) {
    		underTopLevel.setVisibility(View.INVISIBLE);
    		underSelected.setVisibility(View.INVISIBLE);
    		underTopLevel.setChecked(true);
    	} else {
    		underTopLevel.setVisibility(View.VISIBLE);
    		underSelected.setVisibility(View.VISIBLE);
    		underSelected.setText("Underneath " + mSelectedTag.getName());
    	}
    	
    	create.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				createTagClicked();
				
			}
		});
    }
    
    protected void createTagClicked() {
    	if (underTopLevel.isChecked()) {
    		mDatabase.getRootNode().getNamedChild((String) tagName.getText(), true);
    	} else {
    		mSelectedTag.getNamedChild((String) tagName.getText(), true);
    	}
    	finish();
    }
 }

