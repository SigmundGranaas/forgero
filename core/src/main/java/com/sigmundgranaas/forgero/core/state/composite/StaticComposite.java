package com.sigmundgranaas.forgero.core.state.composite;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.context.Contexts;
import com.sigmundgranaas.forgero.core.customdata.DataContainer;
import com.sigmundgranaas.forgero.core.property.Attribute;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.Slot;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.SlotContainer;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
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
	public @NotNull List<Property> getRootProperties(Matchable target, MatchContext context) {
		return compositeProperties(target, context);
	}

	@Override
	public @NotNull
	List<Property> applyProperty(Matchable target, MatchContext context) {
		var newContext = context.add(type);
		return compositeProperties(target, newContext);
	}

	@Override
	public List<Property> compositeProperties(Matchable target, MatchContext context) {
		return Stream.of(slots(), List.of(properties))
				.flatMap(List::stream)
				.map(container -> container.getRootProperties(target, context))
				.flatMap(List::stream)
				.filter(prop -> !(prop instanceof Attribute attribute && attribute.getContext().test(Contexts.LOCAL)))
				.toList();
	}

	@Override
	public State strip() {
		return new StaticComposite(getSlotContainer().strip(), name(), nameSpace(), type(), properties);

	}

	@Override
	public boolean test(Matchable match, MatchContext context) {
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
	public SlotContainer getSlotContainer() {
		return upgrades;
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
		return new StaticComposite(upgrades.copy(), name(), nameSpace(), type(), properties);
	}

	@Override
	public DataContainer customData(Target target) {
		return DataContainer.empty();
	}
}