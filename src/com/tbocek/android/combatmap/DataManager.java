package com.tbocek.android.combatmap;

import java.io.BufferedOutputStream;
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

/**
 * This class manages saved map and token data and provides an interface to query it.
 * @author Tim
 *
 */
public class DataManager {
	private Context context;
	public DataManager(Context context) {
		this.context = context;
	}
	
	/**
	 * Loads the map from the given name.  This takes care of looking up the full path.
	 * @param name
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 */
	public void loadMapName(String name) throws IOException, ClassNotFoundException {
		FileInputStream s = context.openFileInput(name + ".map");
		MapData.loadFromStream(s);
		s.close();
	}
	
	/**
	 * Saves the map to the given name.  This takes care of looking up the full path
	 * @param name
	 * @throws IOException 
	 */
	public void saveMapName(String name) throws IOException {
		FileOutputStream s = context.openFileOutput(name + ".map", Context.MODE_PRIVATE);
		MapData.saveToStream(s);
		s.close();
	}
	
	public List<String> savedFiles() {
		String[] files = context.fileList();
		ArrayList<String> mapFiles = new ArrayList<String>();
		for (String file : files) {
			if (!file.equals("tmp.map") && file.endsWith(".map")) {
				mapFiles.add(file.replace(".map", ""));
			}
		}
		return mapFiles;
	}
	
	public List<String> tokenFiles() {
		String[] files = context.fileList();
		ArrayList<String> imageFiles = new ArrayList<String>();
		for (String file : files) {
			if ((isImageFileName(file)) && !file.contains("preview")) {
				imageFiles.add(file);
			}
		}
		return imageFiles;
	}

	public boolean isImageFileName(String file) {
		return file.endsWith(".jpg") || file.endsWith(".png");
	}
	
	public void saveImage(String name, Bitmap image) throws IOException {
		FileOutputStream s = context.openFileOutput(name + ".jpg", Context.MODE_PRIVATE);
		BufferedOutputStream buf = new BufferedOutputStream(s);
		image.compress(Bitmap.CompressFormat.JPEG, 75, buf);
		buf.close();
		s.close();
	}
	
	public Bitmap loadImage(String filename) throws IOException {
		FileInputStream s = context.openFileInput(filename);
		Bitmap b = BitmapFactory.decodeStream(s);
		s.close();
		return b;
	}

	/**
	 * Deletes the save file and the associated preview image
	 * @param fileName
	 */
	public void deleteSaveFile(String fileName) {
		context.deleteFile(fileName + ".map");
		context.deleteFile(fileName + ".preview.jpg");
	}
}
