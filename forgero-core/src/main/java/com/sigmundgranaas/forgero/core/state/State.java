package com.sigmundgranaas.forgero.core.state;

import java.util.List;

import com.sigmundgranaas.forgero.core.customdata.DataContainer;
import com.sigmundgranaas.forgero.core.customdata.DataSupplier;
import com.sigmundgranaas.forgero.core.customdata.Visitable;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.state.composite.Construct;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.match.Context;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import com.sigmundgranaas.forgero.core.util.match.NameMatch;
import org.jetbrains.annotations.NotNull;


public interface State extends PropertyContainer, Matchable, Identifiable, Comparable<Object>, Typed, Visitable, DataSupplier {
	static State of(Construct construct) {
		return new CompositeIngredient(construct);
	}

	static State of(String name, Type type, List<Property> properties) {
		return new SimpleState(name, type, properties);
	}

	static State of(String name, String nameSpace, Type type, List<Property> properties) {
		return new SimpleState(name, nameSpace, type, properties);
	}

	static State of(String name, String nameSpace, Type type, List<Property> properties, DataContainer custom) {
		return new SimpleState(name, nameSpace, type, properties, custom);
	}

	default boolean equals(State s) {
		return false;
	}

	@Override
	default boolean test(Matchable match, Context context) {
		if (match instanceof Type typeMatch) {
			if (this.type().test(typeMatch, context)) {
				return true;
			}
		}
		if (match instanceof NameMatch name) {
			return name.test(this, context);
		}
		return false;
	}

	@Override
	default int compareTo(@NotNull Object o) {
		if (o instanceof State state) {
			int compare = PropertyContainer.super.compareTo(state);
			if (compare == 0) {
				return this.name().compareTo(state.name());
			} else {
				return compare;
			}
		} else {
			return 0;
		}
	}
}
