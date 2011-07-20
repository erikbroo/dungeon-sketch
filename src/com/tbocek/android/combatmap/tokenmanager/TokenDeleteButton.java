package com.tbocek.android.combatmap.tokenmanager;

import com.tbocek.android.combatmap.R;
import com.tbocek.android.combatmap.graphicscore.BaseToken;

import android.content.Context;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.DragEvent;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnDragListener;
import android.widget.ImageView;


public final class TokenDeleteButton extends ImageView {

	public BaseToken managedToken;

	public TokenDeleteButton(Context context) {
		super(context);
		setImageResource(R.drawable.trashcan);
		setOnDragListener(this.onDragToTrashCanListener);
	}
	
   private OnDragListener onDragToTrashCanListener = new OnDragListener() {
		@Override
		public boolean onDrag(View view, DragEvent event) {
			Log.d("DRAG", Integer.toString(event.getAction()));
			ImageView iv = (ImageView) view;
			BaseToken t = (BaseToken) event.getLocalState();
			if (event.getAction() == DragEvent.ACTION_DROP) {
				managedToken = t;
				iv.showContextMenu();			
				iv.setImageResource(R.drawable.trashcan);
			}
			else if (event.getAction() == DragEvent.ACTION_DRAG_ENTERED) {
				iv.setImageResource(R.drawable.trashcan_hover_over);
				return true;
			}
			else if (event.getAction() == DragEvent.ACTION_DRAG_EXITED) {
				iv.setImageResource(R.drawable.trashcan);
				return true;
			}
			else if (event.getAction() == DragEvent.ACTION_DRAG_STARTED) {
				return true;
			}
			return true;
		}
    };
}
