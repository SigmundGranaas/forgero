package com.sigmundgranaas.forgero.core.model;

import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

public record ModelMatchEntry(String entry) implements Matchable {
	@Override
	public boolean test(Matchable match, MatchContext context) {
		if (match instanceof ModelMatchEntry entry) {
			return entry.entry.equals(this.entry);
		}
		return false;
	}
}
