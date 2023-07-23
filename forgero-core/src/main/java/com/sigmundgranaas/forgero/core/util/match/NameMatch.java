package com.sigmundgranaas.forgero.core.util.match;

import static com.sigmundgranaas.forgero.core.identifier.Common.ELEMENT_SEPARATOR;

import java.util.Arrays;

import com.sigmundgranaas.forgero.core.state.Identifiable;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.FilledSlot;

public record NameMatch(String name) implements Matchable {
	@Override
	public boolean test(Matchable match, MatchContext context) {
		if (match instanceof Identifiable id) {
			return id.name().equals(name) || Arrays.asList(id.name().split(ELEMENT_SEPARATOR)).contains(name);
		} else if (match instanceof FilledSlot slot) {
			return slot.content().name().equals(name);
		} else if (match instanceof NameMatch nameMatch) {
			return nameMatch.name.equals(name);
		}
		return false;
	}
}
