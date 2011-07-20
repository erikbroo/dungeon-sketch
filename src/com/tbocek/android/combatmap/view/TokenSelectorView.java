package com.tbocek.android.combatmap.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.tbocek.android.combatmap.graphicscore.BaseToken;
import com.tbocek.android.combatmap.graphicscore.BuiltInImageToken;
import com.tbocek.android.combatmap.graphicscore.CustomBitmapToken;
import com.tbocek.android.combatmap.graphicscore.LetterToken;
import com.tbocek.android.combatmap.graphicscore.SolidColorToken;
import com.tbocek.android.combatmap.DataManager;
import com.tbocek.android.combatmap.R;
import com.tbocek.android.combatmap.TokenDatabase;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;

public final class TokenSelectorView extends LinearLayout {
    LinearLayout tokenLayout;

    Button groupSelector;
    Button tokenManager;

    TokenViewFactory mTokenViewFactory;

    public TokenSelectorView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.token_selector, this);
        //Create and add the child layout, which will be a linear layout of tokens.

        tokenLayout = new LinearLayout(context);
        ((HorizontalScrollView) findViewById(R.id.token_scroll_view)).addView(tokenLayout);

        groupSelector = (Button)findViewById(R.id.token_category_selector_button);
        tokenManager = (Button)findViewById(R.id.token_manager_button);

        mTokenViewFactory = new TokenViewFactory(context);
    }



    public View createTokenPrototype(BaseToken prototype) {


        View b = this.mTokenViewFactory.getTokenView(prototype);
        b.setOnClickListener(onClickListener);
        b.setLayoutParams(new LinearLayout.LayoutParams(80, 80));
        return b;
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TokenButton clicked = (TokenButton) v;
            if (onTokenSelectedListener != null)
                onTokenSelectedListener.onTokenSelected(clicked.getClone());
        }
    };

    public interface OnTokenSelectedListener {
        void onTokenSelected(BaseToken t);
    }

    OnTokenSelectedListener onTokenSelectedListener = null;

    private TokenDatabase tokenDatabase;

    public void setOnTokenSelectedListener(OnTokenSelectedListener onTokenSelectedListener) {
        this.onTokenSelectedListener = onTokenSelectedListener;
    }



    public void setOnClickGroupSelectorListener(View.OnClickListener listener) {
        groupSelector.setOnClickListener(listener);
    }

    public void setOnClickTokenManagerListener(View.OnClickListener listener) {
        tokenManager.setOnClickListener(listener);
    }



    public void setTokenDatabase(TokenDatabase tokenDatabase) {
        this.tokenDatabase = tokenDatabase;
        setTokenList(tokenDatabase.getAllTokens());
    }



    public void setSelectedTags(List<String> checkedTags) {
        setTokenList(tokenDatabase.tokensForTags(checkedTags));
    }

    public void setTokenList(Collection<BaseToken> tokens) {
        tokenLayout.removeAllViews();
        for (BaseToken token : tokens) {
            tokenLayout.addView(createTokenPrototype(token));
        }

    }
}
