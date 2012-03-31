package com.tbocek.android.combatmap.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.common.collect.Lists;
import com.tbocek.android.combatmap.R;
import com.tbocek.android.combatmap.TokenDatabase;
import com.tbocek.android.combatmap.model.primitives.BaseToken;
import com.tbocek.android.combatmap.tokenmanager.TokenStackDragShadow;

/**
 * Provides a horizontally scrolling list of tokens, allowing the user to
 * pick one.
 * @author Tim Bocek
 *
 */
public final class TokenSelectorView extends LinearLayout {
	
	private static final int TOKENS_PER_ROW = 50;
	/**
	 * Percentage to scale the radius.
	 */
	private static final float RADIUS_SCALE = .9f;
	
	/**
     * The inner layout that contains the tokens.
     */
	private LinearLayout mTokenLayout;

	/**
	 * Button that represents opening the tag selector.
	 */
    private ImageButton mGroupSelector;

    /**
     * Button that represents opening the token manager.
     */
    private ImageButton mTokenManager;

    /**
     * Whether this control is being superimposed over a dark background.
     */
    private boolean mDrawDark;
       
    /**
     * The current list of tokens.  Must correspond to the order they appear in
     * mBitmap.
     */
    private List<BaseToken> mTokens;
    
    /**
     * Listener that fires when a token is selected.
     */
    private OnTokenSelectedListener mOnTokenSelectedListener;

    /**
     * Database to load tokens from.
     */
    private TokenDatabase mTokenDatabase;
    
    /**
     * Constructor.
     * @param context The context to create this view in.
     */
    public TokenSelectorView(final Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.token_selector, this);
        //Create and add the child layout, which will be a linear layout of
        // tokens.
        this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mTokenLayout = new LinearLayout(context);
        ((HorizontalScrollView) findViewById(R.id.token_scroll_view))
        		.addView(mTokenLayout);

        mGroupSelector = (ImageButton) findViewById(
        		R.id.token_category_selector_button);
        mGroupSelector.setAlpha(1.0f);
        mTokenManager = (ImageButton) findViewById(R.id.token_manager_button);
        mTokenManager.setAlpha(1.0f);
        
    }
    
    /**
     * Listener that fires when a token is selected.
     * @author Tim Bocek
     *
     */
    public interface OnTokenSelectedListener {
    	/**
    	 *
    	 * @param t The selected token.  This is already a unique clone.
    	 */
        void onTokenSelected(BaseToken t);
    }

    /**
     * Sets the listener to fire when a token is selected.
     * @param onTokenSelectedListener The listener.
     */
    public void setOnTokenSelectedListener(
    		final OnTokenSelectedListener onTokenSelectedListener) {
        this.mOnTokenSelectedListener = onTokenSelectedListener;
    }



    /**
     * Sets an OnClickListener for the tag selector button.
     * @param listener The listener to use when the tag selector is clicked.
     */
    public void setOnClickGroupSelectorListener(
    		final View.OnClickListener listener) {
        mGroupSelector.setOnClickListener(listener);
    }

    /**
     * Sets an OnClickLIstener for the token manager button.
     * @param listener The listener to use when the token manager button is
     * 		clicked.
     */
    public void setOnClickTokenManagerListener(
    		final View.OnClickListener listener) {
        mTokenManager.setOnClickListener(listener);
    }


    /**
     * Sets the token database to query for tokens.
     * @param database The token database.
     * @param combatView The CombatView to refresh when tokens are loaded.
     */
    public void setTokenDatabase(
    		final TokenDatabase database, final CombatView combatView) {
        this.mTokenDatabase = database;
        setTokenList(mTokenDatabase.getAllTokens(), combatView);
    }

    /**
     * Sets the tag currently being displayed.
     * @param checkedTag The tag currently being displayed.
     * @param combatView The CombatView to refresh when tokens are loaded.
     */
    public void setSelectedTag(
    		final String checkedTag, final CombatView combatView) {
        setTokenList(mTokenDatabase.getTokensForTag(checkedTag), combatView);
    }

    /**
     * Sets the list of tokens displayed to the given collection.
     * @param tokens Tokens to offer in the selector.
     * @param combatView The CombatView to refresh when tokens are loaded.
     */
    @SuppressWarnings("unchecked")
	private void setTokenList(
    		final Collection<BaseToken> tokens, final CombatView combatView) {
    	if (tokens instanceof List<?>) {
    		mTokens = (List<BaseToken>) tokens;
    	} else {
    		this.mTokens = new ArrayList<BaseToken>(tokens);
    	}
    	// TODO: Make this work if height is then changed.
	    new CreateImageTask(this.getHeight(), this.mDrawDark, combatView)
	    	.execute(mTokens);
    }
    
    @SuppressWarnings("unchecked")
	@Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
    	if (h != oldh && mTokens != null) {
    		new CreateImageTask(h, this.mDrawDark, null).execute(mTokens);
    	}
    }
    
    private class TokenImageResult {
    	public TokenImageResult(Bitmap b, List<BaseToken> tokens) {
    		bitmap = b;
    		tokenList = tokens;
    	}
    	
    	public Bitmap bitmap;
    	public List<BaseToken> tokenList;
    }
    
    /**
     * Async task to create the image used.
     * @author Tim
     *
     */
    private class CreateImageTask 
    		extends AsyncTask<List<BaseToken>, Integer, List<TokenImageResult>> {

    	/**
    	 * Height of the view.
    	 */
    	private int mHeight;
    	
    	/**
    	 * Whether drawn on a dark background.
    	 */
    	private boolean mDrawDark;

    	/**
    	 * The combat view to refresh when loading is finished.
    	 */
    	private CombatView mCombatView;
    	
    	/**
    	 * Progress bar to show when loading.
    	 */
    	private ProgressBar mProgressBar;
    	
    	/**
    	 * Constructor.
    	 * @param height Height of the control (width will be determined by the
    	 * 		number of tokens loaded).
    	 * @param drawDark Whether there is a dark background.
    	 * @param toRefresh The CombatView to refresh when loading is finished.
    	 */
    	public CreateImageTask(
    			int height, boolean drawDark, CombatView toRefresh) {
    		mHeight = height;
    		mDrawDark = drawDark;
    		mCombatView = toRefresh;
    	}
    	
    	@Override
    	protected void onPreExecute() {
    		mTokenLayout.removeAllViews();
    		
    		TextView explanation = new TextView(getContext());
    		explanation.setText("Loading Tokens");
    		mTokenLayout.addView(explanation);
    		
    		mProgressBar = new ProgressBar(getContext());
    		mTokenLayout.addView(mProgressBar);
    	}
    	
    	@Override
    	protected void onProgressUpdate(Integer... args) {
    		int progress = args[0];
    		int max = args[1];
    		
    		mProgressBar.setMax(max);
    		mProgressBar.setProgress(progress);
    	}
    	
		@Override
		protected List<TokenImageResult> doInBackground(List<BaseToken>... args) {
			List<BaseToken> tokens = args[0];
	        List<TokenImageResult> res = Lists.newArrayList();
	        
	        for (List<BaseToken> partition: partitionTokens(tokens, TOKENS_PER_ROW)) {
	        	res.add(new TokenImageResult(createBitmapFromTokens(partition), partition));
	        }
	        
	        return res;
		}
		
		private Bitmap createBitmapFromTokens(List<BaseToken> tokens) {
	        Bitmap b = mHeight > 0 ? Bitmap.createBitmap(mHeight * tokens.size(), mHeight, Bitmap.Config.ARGB_4444) : null;
	        
	        int drawY = mHeight / 2;
	        int drawX = mHeight / 2;
	        
	        if (b != null) {
	        	b.eraseColor(Color.TRANSPARENT);
	        }
	        
	        Canvas c = b != null ? new Canvas(b) : null;
	        int i = 0;
	        for (BaseToken t: tokens) {
	        	t.load();
	        	if (b != null) {
		        	t.draw(c, drawX, drawY, RADIUS_SCALE * mHeight / 2, 
		        		   mDrawDark, true);
		        	drawX += mHeight;
	        	}
	        	i += 1;
	        	publishProgress(i, tokens.size());
	        }
	        return b;
		}

		/**
		 * Divide the given list of tokens into sublists of no greater than the
		 * given size.
		 * @param tokens
		 * @return
		 */
		private List<List<BaseToken>> partitionTokens(List<BaseToken> tokens, int maxSize) {
			List<List<BaseToken>> ret = Lists.newArrayList();
			
			for (int i = 0; i < tokens.size(); i += maxSize) {
				ret.add(tokens.subList(i, Math.min(tokens.size(), i + maxSize)));
			}
			
			return ret;
		}
		
		@Override
		protected void onPostExecute(List<TokenImageResult> results) {
	        mTokenLayout.removeAllViews();
	        for(TokenImageResult r: results) {
	        	mTokenLayout.addView(new TokenSelectorViewRow(getContext(), r.bitmap, r.tokenList));
	        }
	        
	        if (mCombatView != null) {
	        	mCombatView.refreshMap();
	        }
	        
		}
    }
    
	/**
	 * @param drawDark Whether tokens are drawn on a dark background.
	 */
	public void setShouldDrawDark(boolean drawDark) {
		this.mDrawDark = drawDark;
	}

	/**
	 * @return Whether tokens are drawn on a dark background.
	 */
	public boolean shouldDrawDark() {
		return mDrawDark;
	}
	
	private class TokenSelectorViewRow extends ImageView {
		private List<BaseToken> mTokens;
		
	    /**
	     * Gesture detector for tapping or long pressing the list of tokens.
	     */
	    private GestureDetector mGestureDetector;

		public TokenSelectorViewRow(
				Context context, Bitmap tokenRowImage, List<BaseToken> tokens) {
			super(context);
			this.setImageBitmap(tokenRowImage);
			mTokens = tokens;
	        mGestureDetector = new GestureDetector(getContext(), new TouchTokenListener());
		}
		
		@Override
		public boolean onTouchEvent(MotionEvent event) {
			mGestureDetector.onTouchEvent(event);
			return true;
		}

	    /**
	     * Gesture listener for tapping and long pressing the token list.
	     * @author Tim
	     *
	     */
	    private class TouchTokenListener 
	    		extends GestureDetector.SimpleOnGestureListener {
	    	@Override
	    	public void onLongPress(MotionEvent e) {
	    		if (android.os.Build.VERSION.SDK_INT
	        			>= android.os.Build.VERSION_CODES.HONEYCOMB) {
		    		BaseToken t = getTouchedToken(e.getX());
		    		List<BaseToken> stack = new ArrayList<BaseToken>();
		    		stack.add(t);
		            startDrag(null, 
		            		  new TokenStackDragShadow(stack, 
		            				(int) (RADIUS_SCALE * getHeight() / 2)),
		            		 t , 0);
	    		}
	    	}
	    	
	    	@Override
	    	public boolean onSingleTapUp(MotionEvent e) {
	    		if (mOnTokenSelectedListener != null) {
	    			mOnTokenSelectedListener.onTokenSelected(getTouchedToken(e.getX()));
	    		}
	    		return true;
	    	}
	    	
	    	/**
	    	 * Returns the token touched in the token list given an X coordinate.
	    	 * @param x The coordinate touched.
	    	 * @return Clone of the token touched.
	    	 */
	    	private BaseToken getTouchedToken(float x) {
	    		int tokenIndex = ((int) x) / getHeight();
	    		
	    		return mTokens.get(tokenIndex).clone();
	    	}
	    }


	}
}
