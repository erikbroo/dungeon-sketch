package com.tbocek.android.combatmap;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
 * This class manages saved map and token data and provides an interface to
 * query it.
 * @author Tim Bocek
 *
 */
public final class DataManager {
	
	/**
	 * Extension to use for map files.
	 */
	private static final String MAP_EXTENSION = ".map";
	
	/**
	 * Extension to use for image files.
	 */
	private static final String IMAGE_EXTENSION = ".jpg";
	
	/**
	 * Tag to add to files that are map previews.
	 */
	private static final String PREVIEW_TAG = ".preview";
	
	/**
	 * JPEG compression to use when saving images.
	 */
	private static final int JPEG_COMPRESSION = 75;
	
	/**
	 * Extension to use for preview files.
	 */
	private static final String PREVIEW_EXTENSION =
		PREVIEW_TAG + IMAGE_EXTENSION;
	
	/**
	 * The context that this data manager goes through to read and write data.
	 */
	private Context mContext;
	
	/**
	 * Constructor.
	 * @param context The context to go through when reading and writing data.
	 */
	public DataManager(final Context context) {
		this.mContext = context;
		ensureExternalDirectoriesCreated();
	}
	
	/**
	 * Loads the map from the given name.  This takes care of looking up the
	 * full path.
	 * @param name The name of the map to load, without the extension.
	 * @throws ClassNotFoundException On deserialize error.
	 * @throws IOException On read error.
	 */
	public void loadMapName(final String name) 
			throws IOException, ClassNotFoundException {
		FileInputStream s = mContext.openFileInput(name + MAP_EXTENSION);
		MapData.loadFromStream(s);
		s.close();
	}
	
	/**
	 * Saves the map to the given name.  This takes care of looking up the full
	 * path.
	 * @param name Name of the map to save, without the extension.
	 * @throws IOException On write error.
	 */
	public void saveMapName(final String name) throws IOException {
		FileOutputStream s = mContext.openFileOutput(
				name + MAP_EXTENSION, Context.MODE_PRIVATE);
		MapData.saveToStream(s);
		s.close();
	}
	
	/**
	 * Gets a list of saved map names, without the extensions.
	 * @return A list of available saved maps.
	 */
	public List<String> savedFiles() {
		String[] files = mContext.fileList();
		ArrayList<String> mapFiles = new ArrayList<String>();
		for (String file : files) {
			if (!file.equals("tmp" + MAP_EXTENSION) 
						&& file.endsWith(MAP_EXTENSION)) {
				mapFiles.add(file.replace(MAP_EXTENSION, ""));
			}
		}
		return mapFiles;
	}
	
	/**
	 * Gets a list of token files available to load.
	 * @return A list of token files, with the extensions.
	 */
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

	/**
	 * Returns true if the filename is an image, false otherwise.
	 * @param file Filename to check.
	 * @return True if an image.
	 */
	public boolean isImageFileName(final String file) {
		return file.endsWith(IMAGE_EXTENSION);
	}
	
	/**
	 * Opens a file for the given token image.
	 * @param filename Name of the file to open, with extension but without
	 * 		directory.
	 * @return Opened file object.
	 */
	private File getTokenImageFile(final String filename) {
		File sdcard = getTokenImageDir();
		return new File(sdcard, filename);
	}
	
	/**
	 * @return File object representing the directory containing token images.
	 */
	private File getTokenImageDir() {
		File sdcard = Environment.getExternalStorageDirectory();
		File dir = new File(sdcard, "DungeonSketch/Tokens");
		return dir;
	}
	
	/**
	 * Creates the directory that contains external token images, if it has
	 * not been created.
	 */
	private void ensureExternalDirectoriesCreated() {
		getTokenImageDir().mkdirs();
	}
	
	/**
	 * Saves the given image as a token image file.
	 * @param name Name of the file to save, without extension.
	 * @param image Bitmap to save to this file.
	 * @return The saved file name, with extension.
	 * @throws IOException On write error.
	 */
	public String saveTokenImage(final String name, final Bitmap image) 
			throws IOException {
		String filename = name + IMAGE_EXTENSION;
		FileOutputStream s = new FileOutputStream(getTokenImageFile(filename));
		BufferedOutputStream buf = new BufferedOutputStream(s);
		image.compress(Bitmap.CompressFormat.JPEG, JPEG_COMPRESSION, buf);
		buf.close();
		s.close();
		return filename;
	}
	
	/**
	 * Saves a preview of a map.
	 * @param name The name of the map, without the extension.
	 * @param preview Preview image for the map.
	 * @throws IOException On write error.
	 */
	public void savePreviewImage(final String name, final Bitmap preview) 
			throws IOException {
		FileOutputStream s = mContext.openFileOutput(
				name + PREVIEW_EXTENSION, Context.MODE_PRIVATE);
		BufferedOutputStream buf = new BufferedOutputStream(s);
		preview.compress(Bitmap.CompressFormat.JPEG, JPEG_COMPRESSION, buf);
		buf.close();
		s.close();
	}
	
	/**
	 * Loads the given token image.
	 * @param filename Filename to load, with extension.
	 * @return Bitmap of the loaded image.
	 * @throws IOException On read error.
	 */
	public Bitmap loadTokenImage(final String filename) throws IOException {
		FileInputStream s = new FileInputStream(getTokenImageFile(filename));
		Bitmap b = BitmapFactory.decodeStream(s);
		s.close();
		return b;
	}
	
	/**
	 * Loads a preview image for the given save file.
	 * @param saveFile Save file to load a preview for.  Do not provide a file
	 * 		extension.
	 * @return Loaded image.
	 * @throws IOException On read error.
	 */
	public Bitmap loadPreviewImage(final String saveFile) throws IOException {
		FileInputStream s = 
			mContext.openFileInput(saveFile + PREVIEW_EXTENSION);
		Bitmap b = BitmapFactory.decodeStream(s);
		s.close();
		return b;
	}

	/**
	 * Deletes the save file and the associated preview image.
	 * @param fileName Name of the save file without the extension to delete.
	 */
	public void deleteSaveFile(final String fileName) {
		mContext.deleteFile(fileName + MAP_EXTENSION);
		mContext.deleteFile(fileName + PREVIEW_EXTENSION);
	}

	/**
	 * Deletes the given token image.
	 * @param fileName Name of the token to delete.
	 */
	public void deleteTokenImage(final String fileName) {
		getTokenImageFile(fileName).delete();
	}


}
