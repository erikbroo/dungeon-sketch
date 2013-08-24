package com.tbocek.android.combatmap.view;

import com.tbocek.android.combatmap.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TokenDeploymentLineItem extends LinearLayout {

	private Button mDecreaseButton;
	private Button mIncreaseButton;
	private TextView mAmountTextView;
	private ImageView mTokenImageView;

	public TokenDeploymentLineItem(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.token_deployment_line_item, this);
		
	}
	
	

}
