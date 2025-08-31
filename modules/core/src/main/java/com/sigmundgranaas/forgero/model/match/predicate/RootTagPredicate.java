package com.sigmundgranaas.forgero.model.match.predicate;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.api.ModelResolutionContext;
import com.sigmundgranaas.forgero.model.match.Predicate;

public record RootTagPredicate(OpenIdentifier tag) implements Predicate {
	@Override
	public boolean test(ModelResolutionContext context) {
		return context.root().getTags().contains(tag);
	}
}
