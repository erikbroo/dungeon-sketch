package com.tbocek.android.combatmap.view;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.tbocek.android.combatmap.TokenDatabase;
import com.tbocek.android.combatmap.graphicscore.BaseToken;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

public class TokenLoadTask
	extends AsyncTask<Void, String, Void> {

	// Map from token ID to the token button to show once that token ID loads.
	private Map<String, TokenButton> tokenButtonMap;
	private Collection<TokenButton> tokenButtons;

	public TokenLoadTask(Collection<TokenButton> buttons) {
		super();
		tokenButtons = buttons;
	}


	@Override
	protected Void doInBackground(Void... args) {
		for (TokenButton b : tokenButtonMap.values()) {
			BaseToken t = b.getClone();
			if (t.needsLoad()) {
				t.load();
			}
			publishProgress(b.getTokenId());
		}
		return null;
	}

	@Override
	protected void onPreExecute() {
		tokenButtonMap = new HashMap<String, TokenButton>();
		for (TokenButton b : tokenButtons) {
			if (b.getClone().needsLoad()) {
				b.setVisibility(View.GONE);
				tokenButtonMap.put(b.getTokenId(), b);
			}
		}
	}

	@Override
	protected void onProgressUpdate(String... progress) {
		String tokenId = progress[0];
		TokenButton b = tokenButtonMap.get(tokenId);
		if (b != null) {
			b.setVisibility(View.VISIBLE);
			b.invalidate();
		}
	}

}
