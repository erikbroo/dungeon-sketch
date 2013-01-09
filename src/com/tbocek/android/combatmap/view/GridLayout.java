package com.tbocek.android.combatmap.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/**
 * This composite view lays out controls in a grid.  It is given a size for
 * the cells in the grid, and lays controls out, one per cell, in rows.  The
 * control grows as much as needed vertically to accommodate the child controls.
 * It does not guarantee a particular relative layout; the row in which a
 * child appears will depend on the width of the control.
 * @author Tim
 *
 */
public class GridLayout extends ViewGroup {

	/**
	 * The width of each cell.
	 */
	private int mCellWidth;
	
	/**
	 * The height of each cell.
	 */
	private int mCellHeight;
	
	/**
	 * Constructor.
	 * @param context Context that this view uses.
	 */
	public GridLayout(Context context) {
		super(context);
	}
	
	/**
	 * Sets the width and height for each cell.
	 * @param width The width of each cell.
	 * @param height The height of each cell.
	 */
	public void setCellDimensions(int width, int height) {
		mCellWidth = width;
		mCellHeight = height;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int cellsPerRow = (r - l) / mCellWidth;
		
		for (int i = 0; i < this.getChildCount(); ++i) {
			int row = i / cellsPerRow;
			int col = i % cellsPerRow;
			
			int childLeft = col * mCellWidth;
			int childTop = row * mCellHeight;
			
			this.getChildAt(i).layout(
					childLeft, childTop, childLeft + mCellWidth, 
					childTop + mCellHeight);
		}

	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = View.MeasureSpec.getSize(widthMeasureSpec);
		
		int cellsPerRow = width / mCellWidth;
		int numRows = (int) Math.ceil(
				((float) this.getChildCount()) / ((float) cellsPerRow));
		
		int height = numRows * mCellHeight;
		
		setMeasuredDimension(width, height);
		
		// Measure children to give them the dimensions allowed.
		int childWidthSpec = View.MeasureSpec.makeMeasureSpec(
				mCellWidth, View.MeasureSpec.EXACTLY);
		int childHeightSpec = View.MeasureSpec.makeMeasureSpec(
				mCellHeight, View.MeasureSpec.EXACTLY);		
		for (int i = 0; i < this.getChildCount(); ++i) {
			this.getChildAt(i).measure(childWidthSpec, childHeightSpec);
		}
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
	}

}
