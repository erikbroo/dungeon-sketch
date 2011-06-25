package com.tbocek.android.combatmap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

import com.tbocek.android.combatmap.view.TokenCreatorView;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class TokenCreator extends Activity {
	static final int PICK_IMAGE_REQUEST = 0;
	
	TokenCreatorView view;
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	view = new TokenCreatorView(this);
    	setContentView(view);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.token_image_creator, menu);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.token_image_creator_pick:
            startActivityForResult(new Intent(
            		Intent.ACTION_PICK,
            		MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
                PICK_IMAGE_REQUEST);
    		return true;
    	case R.id.token_image_creator_accept:
    		//view.setImage(new BitmapDrawable(view.getClippedBitmap()));
    		try {
    			saveToInternalImage(Integer.toString(new Random().nextInt()));
    			setResult(Activity.RESULT_OK);
    			finish();
    		} catch (IOException e) {
    			logException(e);
    		}
    		return true;
    	}
		return false;
    }
    
    private void saveToInternalImage(String name) throws IOException {
   		Bitmap bitmap = view.getClippedBitmap();
   		if (bitmap == null) return;
		new DataManager(this.getApplicationContext()).saveImage(name, bitmap);
	}

	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
    		try {
				Bitmap bitmap = MediaStore.Images.Media.getBitmap(
						this.getContentResolver(), data.getData());
				view.setImage(new BitmapDrawable(bitmap));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logException(e);
			}
    	}
    }
	
	private void logException(Exception e) {
		e.printStackTrace();
		Toast toast = Toast.makeText(this.getApplicationContext(), "Couldn't load image: " + e.toString(), Toast.LENGTH_LONG);
		toast.show();		
	}

}
