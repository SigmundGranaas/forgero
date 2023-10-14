package com.sigmundgranaas.forgero.core.property.v2.feature;

import java.util.List;

import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public abstract class BasePredicateFeature implements Feature, Matchable {
	protected final String id;
	protected final String type;
	protected final Matchable predicate;
	protected final String title;
	protected final List<String> description;

	public BasePredicateFeature(BasePredicateData data) {
		this.id = data.id();
		this.type = data.type();
		this.predicate = data.predicate();
		this.title = data.title();
		this.description = data.description();
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
