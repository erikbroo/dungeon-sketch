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

public class ArtCreditsView extends LinearLayout {

	private static final int SMALL_TEXT_SIZE = 12;
	
	private static final int LARGE_TEXT_SIZE = 24;
	
	private static final int SEPERATOR_SIZE = 32;
	
	private static final int TOKEN_SIZE = 128;
	
	public ArtCreditsView(Context context) {
		super(context);
		this.setOrientation(LinearLayout.VERTICAL);
	}
	
	public void addArtist(String name, String licenseText) {
		addArtist(name, licenseText, null);
	}
	
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
	
	private Map<String, ViewGroup> mTokenViewsForArtist = Maps.newHashMap();

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
