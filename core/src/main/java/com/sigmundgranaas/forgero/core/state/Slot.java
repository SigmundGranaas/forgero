package com.sigmundgranaas.forgero.core.state;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.attribute.Category;
import com.sigmundgranaas.forgero.core.util.match.ContextKey;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

public interface Slot extends PropertyContainer, Matchable, CopyAble<Slot> {
	ContextKey<Slot> SLOT_CONTEXT_KEY = ContextKey.of("slot", Slot.class);

	int index();

	boolean filled();

	Optional<State> get();

	Slot empty();

	Optional<Slot> fill(State slottable, Set<Category> categories);

	String identifier();

	default Set<Category> category() {
		return Collections.emptySet();
	}

	String description();

	String typeName();
}
