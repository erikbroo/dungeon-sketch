package com.tbocek.android.combatmap.model;

import com.tbocek.android.combatmap.model.primitives.CoordinateTransformer;

import android.graphics.Canvas;

public class MapDrawer {
	/**
	 * Options for what to do with the fog of war.
	 *
	 * @author Tim
	 *
	 */
	public enum FogOfWarMode {
		/**
		 * Ignore the fog of war.
		 */
		NOTHING,

		/**
		 * Draw the fog of war as an overlay.
		 */
		DRAW,

		/**
		 * Use the fog of war to clip the background.
		 */
		CLIP
	}
	
	private boolean mDrawGridLines;
	private boolean mDrawGmNotes;
	private boolean mDrawTokens;
	private boolean mAreTokensManipulable;
	private boolean mDrawAnnotations;
	private FogOfWarMode mBackgroundFogOfWar;
	private FogOfWarMode mGmNoteFogOfWar;
	
	public MapDrawer drawGridLines(boolean val) {
		mDrawGridLines = val;
		return this;
	}
	
	public MapDrawer drawGmNotes(boolean val) {
		mDrawGmNotes = val;
		return this;
	}
	
	public MapDrawer drawTokens(boolean val) {
		mDrawTokens = val;
		return this;
	}	
	
	public MapDrawer areTokensManipulable(boolean val) {
		mAreTokensManipulable = val;
		return this;
	}
	
	public MapDrawer drawAnnotations(boolean val) {
		mDrawAnnotations = val;
		return this;
	}
	
	public MapDrawer backgroundFogOfWar(FogOfWarMode val) {
		mBackgroundFogOfWar = val;
		return this;
	}
	
	public MapDrawer gmNotesFogOfWar(FogOfWarMode val) {
		mGmNoteFogOfWar = val;
		return this;
	}
	
	public void draw(Canvas canvas, MapData m) {
		m.getGrid().drawBackground(canvas);
		
		canvas.save();
		m.getWorldSpaceTransformer().setMatrix(canvas);
		if (mBackgroundFogOfWar == FogOfWarMode.CLIP 
				&& !m.getBackgroundFogOfWar().isEmpty()) {
			m.getBackgroundFogOfWar().clipFogOfWar(canvas);
		}
		m.getBackgroundLines().drawAllLinesBelowGrid(canvas);
		m.getBackgroundImages().draw(canvas, m.getWorldSpaceTransformer());
		canvas.restore();

		if (mDrawGridLines) {
			m.getGrid().draw(canvas, m.getWorldSpaceTransformer());
		}
		
		canvas.save();
		m.getWorldSpaceTransformer().setMatrix(canvas);
		if (mBackgroundFogOfWar == FogOfWarMode.CLIP 
				&& !m.getBackgroundFogOfWar().isEmpty()) {
			m.getBackgroundFogOfWar().clipFogOfWar(canvas);
		}
		m.getBackgroundLines().drawAllLinesAboveGrid(canvas);
		if (mBackgroundFogOfWar == FogOfWarMode.DRAW) {
			m.getBackgroundFogOfWar().drawFogOfWar(canvas);
		}
		canvas.restore();

		canvas.save();
		m.getWorldSpaceTransformer().setMatrix(canvas);
		

		if (mDrawGmNotes) {
			canvas.save();
			if (this.mGmNoteFogOfWar == FogOfWarMode.CLIP) {
				m.getGmNotesFogOfWar().clipFogOfWar(canvas);
			}
			m.getGmNoteLines().drawAllLines(canvas);
			if (this.mGmNoteFogOfWar == FogOfWarMode.CLIP){
				canvas.restore();
			} else if (this.mGmNoteFogOfWar == FogOfWarMode.DRAW){
				m.getGmNotesFogOfWar().drawFogOfWar(canvas);
			}
			canvas.restore();
		}
		
		if (this.mDrawAnnotations) {

			m.getAnnotationLines().drawAllLines(canvas);
		}
		canvas.restore();

		canvas.save();
		if (mBackgroundFogOfWar == FogOfWarMode.CLIP
				&& !m.getBackgroundFogOfWar().isEmpty()) {
			m.getWorldSpaceTransformer().setMatrix(canvas);
			m.getBackgroundFogOfWar().clipFogOfWar(canvas);
			m.getWorldSpaceTransformer().setInverseMatrix(canvas);
		}
		CoordinateTransformer gridSpace = m.getGrid().gridSpaceToScreenSpaceTransformer(
				m.getWorldSpaceTransformer());
		if (mDrawTokens) {
			m.getTokens().drawAllTokens(canvas, gridSpace,
					m.getGrid().isDark(), mAreTokensManipulable);
		}
		canvas.restore();
	}
	
}
