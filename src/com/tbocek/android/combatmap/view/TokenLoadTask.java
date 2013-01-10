package com.tbocek.android.combatmap.view;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import android.os.AsyncTask;
import android.view.View;

import com.tbocek.android.combatmap.model.primitives.BaseToken;

/**
 * This task loads custom token images on a separate thread. This allows faster
 * startup times for activities that need to load the entire token library.
 * 
 * @author Tim
 * 
 */
public class TokenLoadTask extends AsyncTask<Void, String, Void> {

    /**
     * Map from token ID to the token button to show once that token ID loads.
     */
    private Map<String, TokenButton> mTokenButtonMap;

    /**
     * The list of token buttons that are being loaded.
     */
    private Collection<TokenButton> mTokenButtons;

    /**
     * Combat view in which these tokens are being drawn.
     */
    private CombatView mCombatView;

    /**
     * Constructor.
     * 
     * @param buttons
     *            The list of token buttons to load.
     */
    public TokenLoadTask(Collection<TokenButton> buttons) {
        super();
        mTokenButtons = buttons;
    }

    /**
     * Constructor.
     * 
     * @param buttons
     *            The list of token buttons to load.
     * @param combatView
     *            CombatView to refresh when a token is loaded.
     */
    public TokenLoadTask(Collection<TokenButton> buttons, CombatView combatView) {
        super();
        mTokenButtons = buttons;
        mCombatView = combatView;
    }

    @Override
    protected Void doInBackground(Void... args) {
        for (TokenButton b : mTokenButtonMap.values()) {
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
        mTokenButtonMap = new HashMap<String, TokenButton>();
        for (TokenButton b : mTokenButtons) {
            if (b.getClone().needsLoad()) {
                b.setVisibility(View.INVISIBLE);
                mTokenButtonMap.put(b.getTokenId(), b);
            }
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        if (mCombatView != null) {
            mCombatView.refreshMap();
        }
    }

    @Override
    protected void onProgressUpdate(String... progress) {
        String tokenId = progress[0];
        TokenButton b = mTokenButtonMap.get(tokenId);
        if (b != null) {
            b.setVisibility(View.VISIBLE);
            b.invalidate();
        }
    }

}
