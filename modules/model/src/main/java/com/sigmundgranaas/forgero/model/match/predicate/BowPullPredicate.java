package com.sigmundgranaas.forgero.model.match.predicate;

import com.sigmundgranaas.forgero.model.api.ModelResolutionContext;
import com.sigmundgranaas.forgero.model.match.Predicate;

import org.jetbrains.annotations.Nullable;

/**
 * Matches on a bow's draw state.
 *
 * <p>The {@code pull} threshold is always checked against the {@code pull}
 * progress in the resolution context. The {@code pulling} flag is optional:
 * when specified it must equal the context's {@code pulling} state, but when
 * {@code null} the predicate ignores the pulling boolean entirely and matches
 * purely on the {@code pull} threshold. This lets an equipped arrow show its
 * in-bow variant based on draw progress regardless of whether the bow reports
 * an active pulling state.
 */
public record BowPullPredicate(float pull, @Nullable Boolean pulling) implements Predicate {
	@Override
	public boolean test(ModelResolutionContext context) {
		if (this.pulling != null) {
			boolean pullingMatch = context.get("pulling")
					.map(val -> this.pulling.equals(val))
					.orElse(!this.pulling);

			if (!pullingMatch) {
				return false;
			}
		}

		return context.get("pull")
				.map(val -> (Float) val >= this.pull)
				.orElse(this.pull <= 0f);
	}
}
