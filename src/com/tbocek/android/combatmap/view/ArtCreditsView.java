package com.tbocek.android.combatmap.view;

import java.util.Map;

import com.google.common.collect.Maps;
import com.tbocek.android.combatmap.model.primitives.BuiltInImageToken;

import android.content.Context;
import android.graphics.Color;
import android.text.util.Linkify;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ArtCreditsView extends LinearLayout {

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
		nameView.setTextSize(24);
		this.addView(nameView);
		
		
		// Add the URL, if given
		if (url != null) {
			TextView urlView = new TextView(getContext());
			urlView.setTextSize(12);
			urlView.setAutoLinkMask(Linkify.WEB_URLS);
			urlView.setText(url);
			this.addView(urlView);
		}
		
		// Add the license text
		TextView licenseView = new TextView(getContext());
		licenseView.setText(licenseText);
		licenseView.setTextSize(12);
		this.addView(licenseView);
		
		// Add a scrollable view of contributed tokens
		HorizontalScrollView scroller = new HorizontalScrollView(this.getContext());
		LinearLayout tokenView = new LinearLayout(this.getContext());
		scroller.addView(tokenView);
		this.addView(scroller);
		mTokenViewsForArtist.put(name, tokenView);
		
		// Add some vertical padding
		ImageView seperator = new ImageView(this.getContext());
        seperator.setLayoutParams(
                new LinearLayout.LayoutParams(
                		LinearLayout.LayoutParams.MATCH_PARENT,
                        (int)(getResources().getDisplayMetrics().density * 32)));
        this.addView(seperator);
	}
	
	private Map<String, ViewGroup> mTokenViewsForArtist = Maps.newHashMap();

	public void addArtCredit(String name, int resource) {
		TokenButton b = new TokenButton(this.getContext(), new BuiltInImageToken(resource));
		b.allowDrag = false;
		b.setLayoutParams(new LinearLayout.LayoutParams(
				(int)(getResources().getDisplayMetrics().density * 128), 
				(int)(getResources().getDisplayMetrics().density * 128)));
		mTokenViewsForArtist.get(name).addView(b);
	}
	
}
