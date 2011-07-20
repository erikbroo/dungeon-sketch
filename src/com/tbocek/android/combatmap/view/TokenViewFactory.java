package com.tbocek.android.combatmap.view;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.view.View;

import com.tbocek.android.combatmap.graphicscore.BaseToken;

public final class TokenViewFactory {
    public Map<BaseToken, View> cachedViews = new HashMap<BaseToken, View>();
    private Context context;

    public TokenViewFactory(Context context) {
        this.context = context;
    }

    public View getTokenView(BaseToken prototype) {
        if (cachedViews.containsKey(prototype)) return cachedViews.get(prototype);

        TokenButton b = new TokenButton(context, prototype);
        cachedViews.put(prototype, b);
        return b;
    }
}