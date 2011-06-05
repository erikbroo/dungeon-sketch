package com.tbocek.android.combatmap;

import com.tbocek.android.combatmap.R;
import com.tbocek.android.combatmap.graphicscore.Token;
import com.tbocek.android.combatmap.view.CombatView;
import com.tbocek.android.combatmap.view.DrawOptionsView;
import com.tbocek.android.combatmap.view.TokenSelectorView;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;

public class CombatMap extends Activity {
	private CombatView mCombatView;
	private TokenSelectorView mTokenSelector;
	private FrameLayout mBottomControlFrame;
	private DrawOptionsView mDrawOptionsView;
	
	private TokenSelectorView.OnTokenSelectedListener mOnTokenSelectedListener = new TokenSelectorView.OnTokenSelectedListener() {
		@Override
		public void onTokenSelected(Token t) {
			mCombatView.placeTokenRandomly(t);
		}
	};
	
	private DrawOptionsView.OnChangeDrawToolListener mOnChangeDrawToolListener = new DrawOptionsView.OnChangeDrawToolListener() {
		
		@Override
		public void onChooseEraser() {
			mCombatView.setEraseMode();
		}
		
		@Override
		public void onChooseColoredPen(int color) {
			// TODO Auto-generated method stub
			mCombatView.setDrawMode();
			mCombatView.newLineColor = color;
		}

		@Override
		public void onChoosePanTool() {
			// TODO Auto-generated method stub
			mCombatView.setZoomPanMode();
		}
	};
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.combat_map_layout);
        
        mCombatView = new CombatView(this);
        mTokenSelector = new TokenSelectorView(this.getApplicationContext());
        mTokenSelector.setOnTokenSelectedListener(mOnTokenSelectedListener);
        
        mDrawOptionsView = new DrawOptionsView(this.getApplicationContext());
        mDrawOptionsView.setOnChangeDrawToolListener(mOnChangeDrawToolListener);
        
        FrameLayout mainContentFrame = (FrameLayout) this.findViewById(R.id.mainContentFrame);
        mBottomControlFrame = (FrameLayout) this.findViewById(R.id.bottomControlAreaFrame);
        
        mainContentFrame.addView(mCombatView);
        mBottomControlFrame.addView(mTokenSelector);
        
        mCombatView.requestFocus();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.combat_map_menu, menu);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.edit_background:
        	mCombatView.setDrawMode();
        	mCombatView.useBackgroundLayer();
        	mBottomControlFrame.removeAllViews();
        	mBottomControlFrame.addView(this.mDrawOptionsView);
        	return true;
        case R.id.edit_annotations:
        	mCombatView.setDrawMode();
        	mCombatView.useAnnotationLayer();
        	mBottomControlFrame.removeAllViews();
        	mBottomControlFrame.addView(this.mDrawOptionsView);
        	return true;
        case R.id.combat_on:
        	mCombatView.setTokenManipulationMode();
        	mBottomControlFrame.removeAllViews();
        	mBottomControlFrame.addView(mTokenSelector);
        	return true;
        }
        
        return false;
    }
}