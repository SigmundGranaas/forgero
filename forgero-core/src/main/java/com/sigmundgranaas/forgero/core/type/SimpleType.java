package com.sigmundgranaas.forgero.core.type;

import java.util.List;
import java.util.Optional;

import com.sigmundgranaas.forgero.core.util.TypeMatcher;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

public class SimpleType implements Type {

	private final String name;
	private final List<Type> parent;
	private final TypeMatcher matcher;

	public SimpleType(String name, List<Type> parent, TypeMatcher matcher) {
		this.name = name;
		this.parent = parent;
		this.matcher = matcher;
	}
	public SimpleType(String name, Optional<Type> parent, TypeMatcher matcher) {
		this.name = name;
		this.parent = parent.map(List::of).orElse(List.of());
		this.matcher = matcher;
	}

	@Override
	public boolean test(Matchable match, MatchContext context) {
		if (match instanceof Type type) {
			if (name.equals(type.typeName())) {
				return matcher.test(match, context);
			} else if (!parent.isEmpty()) {
				return parent.stream().anyMatch(parent -> matcher.test(parent, context)) ;
			}
		}
		return false;
	}

	@Override
	public String typeName() {
		return name;
	}

	@Override
	public List<Type> parent() {
		return parent;
	}
}
