package com.sigmundgranaas.forgero.core.util;

import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

public class SchematicMatcher extends TypeMatcher {
	@Override
	public boolean test(Matchable match, MatchContext context) {
		if (match instanceof Composite) {
			return false;
		} else if (match instanceof State state) {
			return state.type().test(Type.of("SCHEMATIC"), context);
		} else if (match instanceof Type type) {
			if (type.typeName().equals("SCHEMATIC")) {
				return true;
			} else {
				return type.parent().map(parent -> test(parent, context)).orElse(false);
			}
		}
		return false;
	}
}
