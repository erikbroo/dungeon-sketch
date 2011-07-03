package com.tbocek.android.combatmap;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class TextPromptDialog extends Dialog {
	public interface OnTextConfirmedListener {
		public void onTextConfirmed(String text);
	}
	
	TextView nameText;
	Button confirmButton;
	OnTextConfirmedListener listener;
	
	public TextPromptDialog(Context context, OnTextConfirmedListener listener, String title, String confirmText) {
		super(context);
		this.setContentView(R.layout.save);
		this.setTitle(title);
		this.listener = listener;
		
		confirmButton = (Button) this.findViewById(R.id.button_save);
		confirmButton.setText(confirmText);
		nameText = (TextView) this.findViewById(R.id.save_file_name);
		nameText.requestFocus();
		nameText.setText("");
		
		confirmButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String name = (String) nameText.getText().toString();
				dismiss();
				TextPromptDialog.this.listener.onTextConfirmed(name);
			}
		});
	}
	
	protected void onResume() {
		nameText.setText("");
	}

}
