package com.sigmundgranaas.forgero.core.state;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.attribute.Category;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public interface Slot extends PropertyContainer, Matchable, CopyAble<Slot> {
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
