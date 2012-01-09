package com.tbocek.android.combatmap;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Attributes;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
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
        
        
        try {
        	InputStream is = context.getResources().openRawResource(R.raw.art_credits);
        	SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			xr.setContentHandler(new ArtCreditHandler());
			xr.parse(new InputSource(is));
			is.close();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
	}
	
	private class ArtCreditHandler extends DefaultHandler {
		private String currentArtist;
		
		@Override
		public void startElement(String namespaceURI, String localName, String qName, 
		            org.xml.sax.Attributes atts) throws SAXException {
			if (localName.equalsIgnoreCase("artist")) {
				currentArtist = atts.getValue("name");
				creditsView.addArtist(
						currentArtist, atts.getValue("copyright"), atts.getValue("url"));
			} else if (localName.equalsIgnoreCase("token")) {
				int id = getContext().getResources().getIdentifier(
						atts.getValue("res"), 
						"drawable", 
						"com.tbocek.android.combatmap");
				creditsView.addArtCredit(currentArtist, id);
			}
		} 
	}

}
