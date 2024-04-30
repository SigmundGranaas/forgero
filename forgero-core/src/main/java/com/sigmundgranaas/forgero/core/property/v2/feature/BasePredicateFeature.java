package com.sigmundgranaas.forgero.core.property.v2.feature;

import java.util.List;
import java.util.Objects;

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
		return this.predicate.test(match, context);
	}

	@Override
	public boolean applyCondition(Matchable target, MatchContext context) {
		return test(target, context);
	}

	public BasePredicateData data() {
		return new BasePredicateData(id, type, predicate, title, description);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		BasePredicateFeature that = (BasePredicateFeature) o;
		return Objects.equals(id, that.id) && Objects.equals(type, that.type) && Objects.equals(predicate, that.predicate) && Objects.equals(title, that.title) && Objects.equals(description, that.description);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, type, predicate, title, description);
	}
}
