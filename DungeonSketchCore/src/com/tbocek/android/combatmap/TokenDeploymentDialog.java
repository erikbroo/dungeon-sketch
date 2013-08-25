package com.tbocek.android.combatmap;

import com.tbocek.android.combatmap.model.primitives.BaseToken;
import com.tbocek.android.combatmap.view.TokenDeploymentLineItem;

import android.app.Dialog;
import android.content.Context;
import android.widget.LinearLayout;

public class TokenDeploymentDialog extends Dialog {
	
	private LinearLayout mLineItems;

	public TokenDeploymentDialog(Context context) {
		super(context);
		this.setTitle("Deploy Tokens");
		this.setContentView(R.layout.token_deployment_dialog);
		
		mLineItems = (LinearLayout) this.findViewById(R.id.token_deployment_dialog_line_items);
	}
	
	public void setTag(TokenDatabase database, String tag) {
		for (BaseToken t: database.getTokensForTag(tag)) {
			TokenDeploymentLineItem li = new TokenDeploymentLineItem(this.getContext());
			mLineItems.addView(li);
			li.setToken(t);
			li.setNumberToDeploy(1);
		}
	}

}
