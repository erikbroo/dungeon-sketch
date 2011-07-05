package com.tbocek.android.combatmap;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.tbocek.android.combatmap.graphicscore.MapData;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

/**
 * This class manages saved map and token data and provides an interface to query it.
 * @author Tim
 *
 */
public class DataManager {
	private static final String MAP_EXTENSION = ".map";
	private static final String IMAGE_EXTENSION = ".jpg";
	private static final String PREVIEW_TAG = ".preview";
	private static final String PREVIEW_EXTENSION = PREVIEW_TAG + IMAGE_EXTENSION;
	private Context context;
	public DataManager(Context context) {
		this.context = context;
		ensureExternalDirectoriesCreated();
	}
	
	/**
	 * Loads the map from the given name.  This takes care of looking up the full path.
	 * @param name
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 */
	public void loadMapName(String name) throws IOException, ClassNotFoundException {
		FileInputStream s = context.openFileInput(name + MAP_EXTENSION);
		MapData.loadFromStream(s);
		s.close();
	}
	
	/**
	 * Saves the map to the given name.  This takes care of looking up the full path
	 * @param name
	 * @throws IOException 
	 */
	public void saveMapName(String name) throws IOException {
		FileOutputStream s = context.openFileOutput(name + MAP_EXTENSION, Context.MODE_PRIVATE);
		MapData.saveToStream(s);
		s.close();
	}
	
	public List<String> savedFiles() {
		String[] files = context.fileList();
		ArrayList<String> mapFiles = new ArrayList<String>();
		for (String file : files) {
			if (!file.equals("tmp" + MAP_EXTENSION) && file.endsWith(MAP_EXTENSION)) {
				mapFiles.add(file.replace(MAP_EXTENSION, ""));
			}
		}
		return mapFiles;
	}
	
	public List<String> tokenFiles() {
		String[] files = getTokenImageDir().list();
		ArrayList<String> imageFiles = new ArrayList<String>();
		for (String file : files) {
			Log.d("tokenFiles", file);
			if ((isImageFileName(file)) && !file.endsWith(PREVIEW_EXTENSION)) {
				imageFiles.add(file);
			}
		}
		return imageFiles;
	}

	public boolean isImageFileName(String file) {
		return file.endsWith(IMAGE_EXTENSION);
	}
	
	private File getTokenImageFile(String filename) {
		File sdcard = getTokenImageDir();
		return new File(sdcard, filename);
	}
	
	private File getTokenImageDir() {
		File sdcard = Environment.getExternalStorageDirectory();
		File dir = new File(sdcard, "DungeonSketch/Tokens");
		return dir;
	}
	
	private void ensureExternalDirectoriesCreated() {
		getTokenImageDir().mkdirs();
	}
	
	public void saveTokenImage(String name, Bitmap image) throws IOException {
		FileOutputStream s = new FileOutputStream(getTokenImageFile(name + IMAGE_EXTENSION));
		BufferedOutputStream buf = new BufferedOutputStream(s);
		image.compress(Bitmap.CompressFormat.JPEG, 75, buf);
		buf.close();
		s.close();
	}
	
	public void savePreviewImage(String name, Bitmap preview) throws IOException {
		FileOutputStream s = context.openFileOutput(name + PREVIEW_EXTENSION, Context.MODE_PRIVATE);
		BufferedOutputStream buf = new BufferedOutputStream(s);
		preview.compress(Bitmap.CompressFormat.JPEG, 75, buf);
		buf.close();
		s.close();
	}
	
	public Bitmap loadTokenImage(String filename) throws IOException {
		FileInputStream s = new FileInputStream(getTokenImageFile(filename));
		Bitmap b = BitmapFactory.decodeStream(s);
		s.close();
		return b;
	}
	
	public Bitmap loadPreviewImage(String saveFile) throws IOException {
		FileInputStream s = context.openFileInput(saveFile + PREVIEW_EXTENSION);
		Bitmap b = BitmapFactory.decodeStream(s);
		s.close();
		return b;
	}

	/**
	 * Deletes the save file and the associated preview image
	 * @param fileName
	 */
	public void deleteSaveFile(String fileName) {
		context.deleteFile(fileName + MAP_EXTENSION);
		context.deleteFile(fileName + PREVIEW_EXTENSION);
	}

	public void deleteTokenImage(String fileName) {
		getTokenImageFile(fileName).delete();
	}


}
