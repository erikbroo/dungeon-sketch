package com.tbocek.android.combatmap;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.tbocek.android.combatmap.view.ArtCreditsView;

/**
 * Activity that loads and displays art credits for each built-in token.
 * @author Tim
 *
 */
public class ArtCredits extends Activity {

	/**
	 * View to display art credit info; credit data will be dynamically added
	 * here.
	 */
	private ArtCreditsView mCreditsView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        this.setContentView(R.layout.art_credits);
        FrameLayout frame = (FrameLayout) this.findViewById(
        		R.id.art_credits_frame);
        mCreditsView = new ArtCreditsView(this);
        mCreditsView.setTokenButtonClickListener(
        		new ArtCreditsView.TokenButtonClickListener() {
			@Override
			public void onTokenButtonClick(String url) {
				if (url != null) {
					Intent browserIntent = new Intent(
							Intent.ACTION_VIEW, Uri.parse(url));
					startActivity(browserIntent);
				}
			}
		});
        
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
        
        frame.addView(mCreditsView);
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
	
	/**
	 * SAX handler that parses the art credit file and sets up the credits
	 * view.
	 * @author Tim
	 *
	 */
	private class ArtCreditHandler extends DefaultHandler {
		
		/**
		 * Name of the artist currently being parsed.
		 */
		private String mCurrentArtist;
		
		@Override
		public void startElement(
				String namespaceURI, String localName, String qName, 
		        org.xml.sax.Attributes atts) throws SAXException {
			if (localName.equalsIgnoreCase("artist")) {
				mCurrentArtist = atts.getValue("name");
				mCreditsView.addArtist(
						mCurrentArtist,
						atts.getValue("copyright"),
						atts.getValue("url"));
			} else if (localName.equalsIgnoreCase("token")) {
				int id = getResources().getIdentifier(
						atts.getValue("res"), 
						"drawable", 
						"com.tbocek.android.combatmap");
				mCreditsView.addArtCredit(
						mCurrentArtist, id, atts.getValue("url"));
			}
		} 
	}
}
