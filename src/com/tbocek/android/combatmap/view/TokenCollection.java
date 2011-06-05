package com.tbocek.android.combatmap.view;

import java.util.ArrayList;
import java.util.List;

import android.graphics.PointF;

import com.tbocek.android.combatmap.graphicscore.CoordinateTransformer;
import com.tbocek.android.combatmap.graphicscore.Token;
import com.tbocek.android.combatmap.graphicscore.Util;

public class TokenCollection {
	private List<Token> tokens = new ArrayList<Token>();
	
	public List<Token> list() {
		return tokens;
	}
	
	public Token getTokenUnderPoint(PointF p, CoordinateTransformer transformer) {
		for (int i=0;i<tokens.size();++i) {
			if (Util.distance(p, transformer.worldSpaceToScreenSpace(tokens.get(i).location)) < transformer.worldSpaceToScreenSpace(tokens.get(i).radius)) {
				return tokens.get(i);
			}
		}
		return null;
	}

	public void addToken(Token t) {
		tokens.add(t);
	}
}
