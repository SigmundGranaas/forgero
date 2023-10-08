package com.sigmundgranaas.forgero.core.state;

import java.util.Collections;
import java.util.List;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.condition.Conditional;
import com.sigmundgranaas.forgero.core.customdata.DataContainer;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import com.sigmundgranaas.forgero.core.util.match.NameMatch;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ClassCanBeRecord")
public final class SimpleState implements Ingredient, Conditional<State> {
	private final String name;
	private final String nameSpace;
	private final Type type;
	private final List<Property> properties;
	private final DataContainer customData;

	public SimpleState(String name, Type type, List<Property> properties) {
		this.name = name;
		this.type = type;
		this.properties = properties;
		this.nameSpace = Forgero.NAMESPACE;
		this.customData = DataContainer.empty();
	}

	public SimpleState(String name, String nameSpace, Type type, List<Property> properties) {
		this.name = name;
		this.nameSpace = nameSpace;
		this.type = type;
		this.properties = properties;
		this.customData = DataContainer.empty();
	}

	public SimpleState(String name, String nameSpace, Type type, List<Property> properties, DataContainer custom) {
		this.name = name;
		this.nameSpace = nameSpace;
		this.type = type;
		this.properties = properties;
		this.customData = custom;
	}

	@Override
	public @NotNull
	List<Property> getRootProperties() {
		return properties;
	}

	@Override
	public @NotNull List<Property> getRootProperties(Matchable target, MatchContext context) {
		return getProperties();
	}

	@Override
	public @NotNull
	Type type() {
		return type;
	}

	@Override
	public @NotNull
	String name() {
		return name;
	}

	@Override
	public String nameSpace() {
		return nameSpace;
	}

	@Override
	public State strip() {
		return this;
	}

	@Override
	public boolean test(Matchable match, MatchContext context) {
		if (match instanceof NameMatch matcher) {
			return matcher.name().equals(name);
		}
		return type.test(match, context);
	}

	@Override
	public List<PropertyContainer> conditions() {
		return Collections.emptyList();
	}

	@Override
	public State applyCondition(PropertyContainer container) {
		return ConditionedState.of(this).applyCondition(container);
	}

	@Override
	public State removeCondition(String identifier) {
		return this;
	}

	@Override
	public DataContainer customData(Target target) {
		return customData;
	}
}
