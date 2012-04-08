package com.tbocek.android.combatmap;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class GmScreenPinDialog extends Dialog {
	public interface OnPasswordPickedListener {
		public void onPasswordPicked(String password);
	}
	
	private class AddCharacterToPasswordListener implements View.OnClickListener{
		String mToAdd;
		public AddCharacterToPasswordListener(String toAdd) {
			mToAdd = toAdd;
		}
		
		@Override
		public void onClick(View v) {
			mPasswordView.append(mToAdd);
			mOkButton.setEnabled(true);
		}
	}
	
	private TextView mPasswordView;
	private Button mOkButton;
	
	private OnPasswordPickedListener mOnPasswordPicked;
	
	public GmScreenPinDialog(Context context) {
		super(context);
		this.setContentView(R.layout.gm_screen_pinpad);
		
		mPasswordView = (TextView) this.findViewById(R.id.gm_screen_pin_display);
		mOkButton = (Button) this.findViewById(R.id.gm_screen_ok);
		
		hookUpNumberButton(R.id.gm_screen_0, "0");
		hookUpNumberButton(R.id.gm_screen_1, "1");
		hookUpNumberButton(R.id.gm_screen_2, "2");
		hookUpNumberButton(R.id.gm_screen_3, "3");
		hookUpNumberButton(R.id.gm_screen_4, "4");
		hookUpNumberButton(R.id.gm_screen_5, "5");
		hookUpNumberButton(R.id.gm_screen_6, "6");
		hookUpNumberButton(R.id.gm_screen_7, "7");
		hookUpNumberButton(R.id.gm_screen_8, "8");
		hookUpNumberButton(R.id.gm_screen_9, "9");
		
		this.findViewById(R.id.gm_screen_bksp).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mPasswordView.getText().length() > 0) {
					mPasswordView.setText(mPasswordView.getText().subSequence(0, mPasswordView.getText().length() - 1));
				}
				mOkButton.setEnabled(mPasswordView.getText().length() > 0);
			}
		});
		
		this.findViewById(R.id.gm_screen_cancel).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				cancel();
			}
			
		});
		
	this.findViewById(R.id.gm_screen_cancel).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mOnPasswordPicked != null) {
					mOnPasswordPicked.onPasswordPicked((String) mPasswordView.getText());
				}
				dismiss();
			}
	
		});
	}
	
	private void hookUpNumberButton(int id, String textToAppend) {
		Button b = (Button) this.findViewById(id);
		b.setOnClickListener(new AddCharacterToPasswordListener(textToAppend));
	}
	
	public GmScreenPinDialog setOnPasswordPickedListener(OnPasswordPickedListener password) {
		mOnPasswordPicked = password;
		return this;
	}
	
	public GmScreenPinDialog setExplanatoryText(int stringResourceId) {
		((TextView) this.findViewById(R.id.gm_screen_title_text)).setText(stringResourceId);
		return this;
	}
}
