package com.sigmundgranaas.forgero.core.util;

import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

public class TypeMatcher implements Matchable {

	@Override
	public boolean test(Matchable match, MatchContext context) {
		return true;
	}
}
