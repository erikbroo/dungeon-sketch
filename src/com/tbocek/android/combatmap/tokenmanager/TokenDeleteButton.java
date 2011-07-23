package com.tbocek.android.combatmap.tokenmanager;

import android.content.Context;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.ImageView;

import com.tbocek.android.combatmap.R;
import com.tbocek.android.combatmap.graphicscore.BaseToken;


/**
 * This view defines a region that tokens can be dragged onto to delete them
 * or remove tags.
 * @author Tim Bocek
 *
 */
public final class TokenDeleteButton extends ImageView {

	/**
	 * The token that was last dropped onto the button.
	 */
    private BaseToken managedToken;

    /**
     * Constructor.
     * @param context The context to construct in.
     */
    public TokenDeleteButton(final Context context) {
        super(context);
        setImageResource(R.drawable.trashcan);
        setOnDragListener(this.onDragToTrashCanListener);
    }

    /**
	 * @param token The token to manage.
	 */
	public void setManagedToken(final BaseToken token) {
		this.managedToken = token;
	}

	/**
	 * @return The managed token.
	 */
	public BaseToken getManagedToken() {
		return managedToken;
	}

	/**
     * On drag listener that manages changing the color of the button and
     * opening the context menu.
     */
   private OnDragListener onDragToTrashCanListener = new OnDragListener() {
        @Override
        public boolean onDrag(final View view, final DragEvent event) {
            Log.d("DRAG", Integer.toString(event.getAction()));
            ImageView iv = (ImageView) view;
            BaseToken t = (BaseToken) event.getLocalState();
            if (event.getAction() == DragEvent.ACTION_DROP) {
                managedToken = t;
                iv.showContextMenu();
                iv.setImageResource(R.drawable.trashcan);
            } else if (event.getAction() == DragEvent.ACTION_DRAG_ENTERED) {
                iv.setImageResource(R.drawable.trashcan_hover_over);
                return true;
            } else if (event.getAction() == DragEvent.ACTION_DRAG_EXITED) {
                iv.setImageResource(R.drawable.trashcan);
                return true;
            } else if (event.getAction() == DragEvent.ACTION_DRAG_STARTED) {
                return true;
            }
            return true;
        }
    };
}
