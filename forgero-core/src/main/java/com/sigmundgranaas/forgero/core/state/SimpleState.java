package com.sigmundgranaas.forgero.core.state;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
	public List<PropertyContainer> localConditions() {
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SimpleState that = (SimpleState) o;
		return Objects.equals(name, that.name) && Objects.equals(nameSpace, that.nameSpace) && Objects.equals(type, that.type) && Objects.equals(properties, that.properties) && Objects.equals(customData, that.customData);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, nameSpace, type, this.identifier() + "_properties", customData);
	}

	@Override
	public String toString() {
		return "SimpleState{" +
				"name='" + name + '\'' +
				", nameSpace='" + nameSpace + '\'' +
				", type=" + type +
				", properties=" + properties.stream()
				.map(Property::toString)
				.collect(Collectors.joining(", ", "[", "]")) +
				", customData=" + customData.toString() +
				'}';
	}

}




