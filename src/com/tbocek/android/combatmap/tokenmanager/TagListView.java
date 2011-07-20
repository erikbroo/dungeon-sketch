package com.tbocek.android.combatmap.tokenmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.tbocek.android.combatmap.graphicscore.BaseToken;
import com.tbocek.android.combatmap.graphicscore.PointF;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public final class TagListView extends ScrollView {
	public interface OnTagListActionListener {
		public void onChangeSelectedTag(String newTag);
		public void onDragTokenToTag(BaseToken token, String tag);
	}

	public static final String ALL = "All";
	
	LinearLayout innerLayout;
	List<TextView> textViews = new ArrayList<TextView>();
	String highlightedText = "";
	private OnTagListActionListener onTagListAction;
	
	public TagListView(Context context) {
		super(context);
		innerLayout = new LinearLayout(this.getContext());
		addView(innerLayout);
		innerLayout.setOrientation(LinearLayout.VERTICAL);
	}
	
	public void setTagList(Collection<String> collection) {
		innerLayout.removeAllViews();
		innerLayout.addView(createTextView(ALL));
		for (String tag : collection) {
			innerLayout.addView(createTextView(tag));
		}
		
		if (highlightedText == "")
			setHighlightedText(ALL);
		if (onTagListAction != null) {
			onTagListAction.onChangeSelectedTag(highlightedText);
		}
	}
	
	private TextView createTextView(String text) {
		TextView v = new TextView(this.getContext());
		v.setText(text);
		v.setTextSize(30);
		v.setPadding(0, 8, 0, 8);
		textViews.add(v);
		v.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				TextView v = (TextView) arg0;
				setHighlightedText(v.getText().toString());
				if (onTagListAction != null) {
					onTagListAction.onChangeSelectedTag(v.getText().toString());
				}
			}
		});
		v.setOnDragListener(mOnDrag);
		return v;
	}
	
	public void setOnTagListActionListener(OnTagListActionListener onTagListAction) {
		this.onTagListAction = onTagListAction;
	}

	public void setHighlightedText(String text) {
		highlightedText = text;
		for (TextView v:textViews) {
			setTextViewColorToCorrectHighlight(v);
		}
	}

	private void setTextViewColorToCorrectHighlight(TextView v) {
		if (v.getText() == highlightedText) {
			v.setTextColor(Color.WHITE);
		} else {
			v.setTextColor(Color.GRAY);
		}
	}
	
	public String getTag() {
		return this.highlightedText;
	}
	
	public View.OnDragListener mOnDrag = new View.OnDragListener() {
		@Override
		public boolean onDrag(View view, DragEvent event) {
			Log.d("DRAG", Integer.toString(event.getAction()));
			TextView tv = (TextView)view;
			if (event.getAction() == DragEvent.ACTION_DROP) {
				BaseToken toAdd = (BaseToken) event.getLocalState();
				if (onTagListAction != null) {
					onTagListAction.onDragTokenToTag(toAdd, tv.getText().toString());
				}
				setTextViewColorToCorrectHighlight(tv);
				return true;
			}
			else if (event.getAction() == DragEvent.ACTION_DRAG_ENTERED) {
				tv.setTextColor(Color.rgb(41, 162, 255));
				return true;
			}
			else if (event.getAction() == DragEvent.ACTION_DRAG_EXITED) {
				setTextViewColorToCorrectHighlight(tv);
				return true;
			}
			else if (event.getAction() == DragEvent.ACTION_DRAG_STARTED) {
				return true;
			}
			return true;
		}	
	};
}
