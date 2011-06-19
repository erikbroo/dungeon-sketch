package com.tbocek.android.combatmap;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SaveDialog extends Dialog {
	TextView nameText;
	Button saveButton;
	FilenameSelectedListener listener;
	
	public SaveDialog(Context context, FilenameSelectedListener listener) {
		super(context);
		this.setContentView(R.layout.save);
		this.setTitle("Save");
		this.listener = listener;
		
		saveButton = (Button) this.findViewById(R.id.button_save);
		nameText = (TextView) this.findViewById(R.id.save_file_name);
		nameText.requestFocus();
		
		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String name = (String) nameText.getText().toString();
				dismiss();
				SaveDialog.this.listener.onSaveFilenameSelected(name);
			}
		});
	}
	
	protected void onResume() {
		nameText.setText("");
	}

}
