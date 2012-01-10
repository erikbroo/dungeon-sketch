package com.tbocek.android.combatmap.view;

import java.util.Map;

import com.google.common.collect.Maps;
import com.tbocek.android.combatmap.model.primitives.BuiltInImageToken;

import android.content.Context;
import android.text.util.Linkify;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * This view displays art credit details for the art credits activity.  It
 * provides an interface to semantically add credits.
 * @author Tim
 *
 */
public class ArtCreditsView extends LinearLayout {

	/**
	 * Text size to use for text that should be small (url, etc).
	 */
	private static final int SMALL_TEXT_SIZE = 12;
	
	/**
	 * Text size to use for text that should be large (artist name).
	 */
	private static final int LARGE_TEXT_SIZE = 24;
	
	/**
	 * Room to leave between artist spaces, in density-independent pixels. 
	 */
	private static final int SEPERATOR_SIZE = 32;
	
	/**
	 * Size of the token images, in density-independent pixels.
	 */
	private static final int TOKEN_SIZE = 128;
	
	/**
	 * Map that enables lookup of a list of tokens that an artist contributed
	 * given the artist name.
	 */
	private Map<String, ViewGroup> mTokenViewsForArtist = Maps.newHashMap();
	
	/**
	 * Constructor.
	 * @param context Context to construct in.
	 */
	public ArtCreditsView(Context context) {
		super(context);
		this.setOrientation(LinearLayout.VERTICAL);
	}
	
	/**
	 * Add a section for a new artist.  The section is placed at the bottom of
	 * the list.
	 * @param name The name of the artist to add.
	 * @param licenseText The copyright text.
	 * @param url URL for the artist's website, or null if there is no website.
	 */
	public void addArtist(String name, String licenseText, String url) {
		// Add the artist name
		TextView nameView = new TextView(getContext());
		nameView.setText(name);
		nameView.setTextSize(LARGE_TEXT_SIZE);
		this.addView(nameView);
		
		
		// Add the URL, if given
		if (url != null) {
			TextView urlView = new TextView(getContext());
			urlView.setTextSize(SMALL_TEXT_SIZE);
			urlView.setAutoLinkMask(Linkify.WEB_URLS);
			urlView.setText(url);
			this.addView(urlView);
		}
		
		// Add the license text
		TextView licenseView = new TextView(getContext());
		licenseView.setText(licenseText);
		licenseView.setTextSize(SMALL_TEXT_SIZE);
		this.addView(licenseView);
		
		// Add a scrollable view of contributed tokens
		HorizontalScrollView scroller =
				new HorizontalScrollView(this.getContext());
		LinearLayout tokenView = new LinearLayout(this.getContext());
		scroller.addView(tokenView);
		this.addView(scroller);
		mTokenViewsForArtist.put(name, tokenView);
		
		// Add some vertical padding
		ImageView seperator = new ImageView(this.getContext());
        seperator.setLayoutParams(
                new LinearLayout.LayoutParams(
                		LinearLayout.LayoutParams.MATCH_PARENT,
                        (int) (getResources().getDisplayMetrics().density
                        		* SEPERATOR_SIZE)));
        this.addView(seperator);
	}
	
	/**
	 * Add a token credit under the given artist.  The artist must have already
	 * been added with addArtist.
	 * @param name Name of the artist who contributed the token.
	 * @param resource Resource identifier for the contributed token.
	 */
	public void addArtCredit(String name, int resource) {
		TokenButton b = new TokenButton(
				this.getContext(), new BuiltInImageToken(resource));
		b.allowDrag = false;
		b.setLayoutParams(new LinearLayout.LayoutParams(
				(int) (getResources().getDisplayMetrics().density
						* TOKEN_SIZE), 
				(int) (getResources().getDisplayMetrics().density
						* TOKEN_SIZE)));
		mTokenViewsForArtist.get(name).addView(b);
	}
	
}
