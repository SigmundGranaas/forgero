package com.sigmundgranaas.forgero.core.scope;

import com.sigmundgranaas.forgero.core.state.Slot;

import java.util.Optional;

public class SlotScope implements Scope {
	private final Slot slot;
	public static String type = "forgero:slot";

	public SlotScope(Slot slot) {
		this.slot = slot;
	}

	@Override
	public String identifier() {
		return type;
	}

	public static Optional<Slot> slot(Scope scope){
		return Optional.of(scope)
				.filter(s-> s.identifier().equals(type))
				.filter(SlotScope.class::isInstance)
				.map(SlotScope.class::cast)
				.map(s -> s.slot);
	}
}
