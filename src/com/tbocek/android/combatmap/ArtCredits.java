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
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;
public class ArtCredits extends Activity {

	private ArtCreditsView creditsView;
	
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        this.setContentView(R.layout.art_credits);
        FrameLayout frame = (FrameLayout)this.findViewById(R.id.art_credits_frame);
        creditsView = new ArtCreditsView(this);
        
        try {
        	InputStream is = getResources().openRawResource(R.raw.art_credits);
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
        
        frame.addView(creditsView);
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
				int id = getResources().getIdentifier(
						atts.getValue("res"), 
						"drawable", 
						"com.tbocek.android.combatmap");
				creditsView.addArtCredit(currentArtist, id);
			}
		} 
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                Intent intent = new Intent(this, CombatMap.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
