package com.sigmundgranaas.forgero.model.match.predicate;

import com.sigmundgranaas.forgero.model.api.ModelResolutionContext;
import com.sigmundgranaas.forgero.model.match.Predicate;

public record BowPullPredicate(float pull, boolean pulling) implements Predicate {
	@Override
	public boolean test(ModelResolutionContext context) {
		boolean pullingMatch = context.get("pulling")
				.map(val -> (Boolean) val == this.pulling)
				.orElse(!this.pulling);

		if (!pullingMatch) {
			return false;
		}

		return context.get("pull")
				.map(val -> (Float) val >= this.pull)
				.orElse(this.pull <= 0f);
	}
}
