package com.tbocek.android.combatmap.model;

import java.util.List;

import com.google.common.collect.Lists;
import com.tbocek.android.combatmap.model.primitives.BackgroundImage;
import com.tbocek.android.combatmap.model.primitives.CoordinateTransformer;
import com.tbocek.android.combatmap.model.primitives.PointF;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

public class BackgroundImageCollection implements UndoRedoTarget {

    /**
     * Undo/Redo History.
     */
    private CommandHistory mCommandHistory;
    
    private List<BackgroundImage> mImages = Lists.newArrayList();
    
	public BackgroundImageCollection(CommandHistory commandHistory) {
		mCommandHistory = commandHistory;
	}

	@Override
	public void undo() {
		// TODO Auto-generated method stub

	}

	@Override
	public void redo() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canUndo() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canRedo() {
		// TODO Auto-generated method stub
		return false;
	}

	public void addImage(BackgroundImage backgroundImage) {
		mImages.add(backgroundImage);
	}

	public void draw(Canvas canvas, CoordinateTransformer transformer) {
		// Because of the way image drawing works, we need to be able to make
		// the assumption that the canvas is *untransformed*.  But, we still
		// want to respect the fog of war.  So, we let the calling code assume
		// that the canvas is transformed, and undo the transformation here.
		canvas.save();
		transformer.setInverseMatrix(canvas);
		
		for (BackgroundImage i: mImages) {
			i.draw(canvas, transformer);
		}
		canvas.restore();
	}
	
	/**
	 * Finds the object underneath the given point in world space.
	 * @param point Location to check in world space.
	 * @return
	 */
	public BackgroundImage getImageOnPoint(PointF point) {
		for (BackgroundImage i: mImages) {
			if (i.getBoundingRectangle().contains(point)) {
				return i;
			}
		}
		return null;
	}
}
