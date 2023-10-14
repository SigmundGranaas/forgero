package com.sigmundgranaas.forgero.core.property.v2.feature;

import javax.annotation.Nullable;

import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

public abstract class BasePredicateFeature implements Feature, Matchable {
	protected String id;
	protected String type;
	protected Matchable predicate;
	@Nullable
	protected String title;
	@Nullable
	protected String description;

	public BasePredicateFeature(String id, String type, Matchable predicate) {
		this.id = id;
		this.type = type;
		this.predicate = predicate;
	}

	public BasePredicateFeature(BasePredicateData data) {
		this.id = data.id();
		this.type = data.type();
		this.predicate = data.predicate();
	}

	@Override
	public String type() {
		return type;
	}


	@Override
	public boolean test(Matchable match, MatchContext context) {
		return match.test(match, context);
	}
}
