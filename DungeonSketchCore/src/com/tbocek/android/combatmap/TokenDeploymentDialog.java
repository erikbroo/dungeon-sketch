package com.tbocek.android.combatmap;

import android.app.Dialog;
import android.content.Context;

public class TokenDeploymentDialog extends Dialog {

	public TokenDeploymentDialog(Context context) {
		super(context);
		this.setTitle("Deploy Tokens");
		this.setContentView(R.layout.token_deployment_dialog);
	}

}
