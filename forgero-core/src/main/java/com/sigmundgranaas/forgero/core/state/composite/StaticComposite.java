package com.sigmundgranaas.forgero.core.state.composite;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.customdata.DataContainer;
import com.sigmundgranaas.forgero.core.property.Attribute;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.property.attribute.Category;
import com.sigmundgranaas.forgero.core.property.attribute.TypeTarget;
import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.Slot;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.SlotContainer;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.match.Context;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import com.sigmundgranaas.forgero.core.util.match.NameMatch;
import org.jetbrains.annotations.NotNull;

public class StaticComposite implements Composite {
	private final SlotContainer upgrades;
	private final String name;
	private final String nameSpace;
	private final Type type;

	private final PropertyContainer properties;

	public StaticComposite(SlotContainer upgrades, String name, String nameSpace, Type type, PropertyContainer properties) {
		this.upgrades = upgrades;
		this.name = name;
		this.nameSpace = nameSpace;
		this.type = type;
		this.properties = properties;
	}

	@Override
	public List<State> components() {
		return upgrades().stream().filter(state -> !state.name().contains("schematic")).toList();
	}

	@Override
	public @NotNull
	List<Property> applyProperty(Target target) {
		var newTarget = target.combineTarget(new TypeTarget(Set.of(type.typeName())));
		return compositeProperties(newTarget);
	}

	@Override
	public List<Property> compositeProperties(Target target) {
		var props = Stream.of(slots(), List.of(properties))
				.flatMap(List::stream)
				.map(prop -> prop.applyProperty(target))
				.flatMap(List::stream)
				.filter(prop -> !(prop instanceof Attribute attribute && attribute.getCategory() == Category.LOCAL))
				.toList();
		return props;
	}

	@Override
	public boolean test(Matchable match, Context context) {
		if (match instanceof Type typeMatch) {
			if (this.type().test(typeMatch, context)) {
				return true;
			}

		}
		if (match instanceof NameMatch named) {
			return named.test(this, context);
		}
		return false;
	}

	@Override
	public Optional<State> has(String id) {
		if (upgrades().stream().anyMatch(state -> state.identifier().contains(id))) {
			return upgrades().stream().filter(state -> state.identifier().contains(id)).findAny();
		}
		return Optional.empty();
	}

	@Override
	@NotNull
	public String name() {
		return name;
	}

	@Override
	public String nameSpace() {
		return nameSpace;
	}

	@Override
	@NotNull
	public Type type() {
		return type;
	}

	@Override
	public Composite upgrade(State upgrade) {
		return this;
	}

	@Override
	public ImmutableList<State> upgrades() {
		return ImmutableList.<State>builder().addAll(upgrades.entries()).build();
	}

	@Override
	public Composite removeUpgrade(String id) {
		return this;
	}

	@Override
	public boolean canUpgrade(State state) {
		return upgrades.canUpgrade(state);
	}

	@Override
	public List<Slot> slots() {
		return upgrades.slots();
	}

	@Override
	public Composite copy() {
		return this;
	}

	@Override
	public DataContainer customData() {
		return DataContainer.empty();
	}
}
