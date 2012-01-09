package com.tbocek.android.combatmap;

import java.util.jar.Attributes;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.tbocek.android.combatmap.view.ArtCreditsView;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.widget.FrameLayout;
public class ArtCredits extends Dialog {

	private ArtCreditsView creditsView;
	public ArtCredits(Context context) {
		super(context);
        
        this.setTitle("Art Credits");
        
        creditsView = new ArtCreditsView(this.getContext());
        this.setContentView(creditsView);
	}

}
