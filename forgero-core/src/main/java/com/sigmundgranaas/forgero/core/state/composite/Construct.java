package com.sigmundgranaas.forgero.core.state.composite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.customdata.DataContainer;
import com.sigmundgranaas.forgero.core.property.Attribute;
import com.sigmundgranaas.forgero.core.property.AttributeType;
import com.sigmundgranaas.forgero.core.property.CalculationOrder;
import com.sigmundgranaas.forgero.core.property.NumericOperation;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.property.attribute.AttributeBuilder;
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

@SuppressWarnings("ClassCanBeRecord")
public class Construct implements Composite, ConstructedState {
	private final List<State> ingredientList;
	private final SlotContainer upgrades;
	private final String name;
	private final String nameSpace;
	private final Type type;

	protected Construct(List<State> ingredientList, SlotContainer upgrades, String name, Type type) {
		this.name = name;
		this.ingredientList = ingredientList;
		this.upgrades = upgrades;
		this.type = type;
		this.nameSpace = Forgero.NAMESPACE;
	}

	protected Construct(List<State> ingredientList, SlotContainer upgrades, String name, String nameSpace, Type type) {
		this.name = name;
		this.ingredientList = ingredientList;
		this.upgrades = upgrades;
		this.type = type;
		this.nameSpace = nameSpace;
	}

	public static ConstructBuilder builder() {
		return new ConstructBuilder();
	}

	public static ConstructBuilder builder(List<? extends Slot> slots) {
		return new ConstructBuilder(slots);
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
	public @NotNull
	List<Property> getRootProperties() {
		return compositeProperties(Target.EMPTY);
	}

	@Override
	public @NotNull
	List<Property> applyProperty(Target target) {
		var newTarget = target.combineTarget(new TypeTarget(Set.of(type.typeName())));
		return compositeProperties(newTarget);
	}

	public List<Property> compositeProperties(Target target) {
		var props = Stream.of(ingredients(), slots())
				.flatMap(List::stream)
				.map(prop -> prop.applyProperty(target))
				.flatMap(List::stream)
				.filter(prop -> !(prop instanceof Attribute attribute && attribute.getCategory() == Category.LOCAL))
				.collect(Collectors.toList());

		var upgradeProps = ingredients()
				.stream()
				.map(state -> state.applyProperty(target))
				.flatMap(List::stream)
				.filter(this::filterAttribute)
				.toList();

		var compositeAttributes = Property.stream(props)
				.getAttributes()
				.collect(Collectors.toMap(Attribute::toString, attribute -> attribute, (attribute1, attribute2) -> attribute1.getPriority() > attribute2.getPriority() ? attribute1 : attribute2))
				.values()
				.stream()
				.filter(attribute -> attribute.getOrder() == CalculationOrder.COMPOSITE)
				.map(Property.class::cast)
				.toList();

		var newValues = new ArrayList<Property>();
		for (AttributeType type : AttributeType.values()) {
			var newBaseAttribute = new AttributeBuilder(type.toString()).applyOperation(NumericOperation.ADDITION).applyOrder(CalculationOrder.BASE);
			newBaseAttribute.applyValue(Property.stream(compositeAttributes).applyAttribute(type)).applyCategory(Category.PASS);
			var attribute = newBaseAttribute.build();
			if (attribute.getValue() != 0 && compositeAttributes.stream().filter(prop -> prop instanceof Attribute attribute1 && attribute1.getAttributeType().equals(type.toString())).toList().size() > 1) {
				newValues.add(newBaseAttribute.build());
			}
		}

		var other = new ArrayList<>(props);
		compositeAttributes.forEach(other::remove);
		upgradeProps.forEach(other::remove);
		other.addAll(newValues);
		return other;
	}

	@Override
	public Optional<State> has(String id) {
		return components().stream().map(comp -> recursiveComponentHas(comp, id)).flatMap(Optional::stream).findFirst();
	}

	private Optional<State> recursiveComponentHas(State target, String id) {
		if (target.identifier().contains(id)) {
			return Optional.of(target);
		} else if (target instanceof Composite comp) {
			if (comp.has(id).isPresent()) {
				return comp.has(id);
			}
		}
		return Optional.empty();
	}

	private boolean filterAttribute(Property property) {
		if (property instanceof Attribute attribute) {
			return Category.UPGRADE_CATEGORIES.contains(attribute.getCategory());
		}
		return false;
	}

	@Override
	public boolean test(Matchable match, Context context) {
		if (match instanceof Type typeMatch) {
			if (this.type().test(typeMatch, context)) {
				return true;
			} else {
				return ingredientList.stream().anyMatch(ingredient -> ingredient.test(match, context));
			}

		}
		if (match instanceof NameMatch name) {
			if (name.test(this, context)) {
				return true;
			} else {
				return ingredientList.stream().anyMatch(ingredient -> ingredient.test(name, context));
			}
		}
		return false;
	}

	public List<State> ingredients() {
		return ingredientList;
	}

	@Override
	public Construct removeUpgrade(String id) {
		if (upgrades().stream().anyMatch(state -> state.identifier().contains(id))) {
			var originalSlots = slots().stream().filter(slot -> !slot.filled() || (slot.get().isPresent() && !slot.get().get().identifier().contains(id))).toList();
			var emptySlots = slots().stream().filter(slot -> (slot.get().isPresent() && slot.get().get().identifier().contains(id))).map(Slot::empty).toList();
			return builder()
					.addIngredients(ingredients())
					.addUpgrades(originalSlots)
					.addUpgrades(emptySlots)
					.type(type())
					.id(identifier())
					.build();
		} else {
			for (int i = 0; i < ingredientList.size(); i++) {
				if (ingredientList.get(i) instanceof Composite construct) {
					var compositeRemoved = construct.removeUpgrade(id);
					if (construct != compositeRemoved) {
						var ingredients = new ArrayList<>(ingredientList);
						ingredients.set(i, compositeRemoved);
						return builder()
								.addIngredients(ingredients)
								.addUpgrades(slots())
								.type(type())
								.id(identifier())
								.build();
					}
				}
			}
			for (int i = 0; i < slots().size(); i++) {
				var slot = slots().get(i);
				if (slot.get().isPresent() && slot.get().get() instanceof Composite construct) {
					var compositeRemoved = construct.removeUpgrade(id);
					if (construct != compositeRemoved) {
						var slots = new ArrayList<>(slots());
						slots.set(i, slot.empty().fill(compositeRemoved, slot.category()).orElse(slot.empty()));
						return builder()
								.addIngredients(ingredients())
								.addUpgrades(slots)
								.type(type())
								.id(identifier())
								.build();
					}
				}
			}

		}
		return this;
	}


	public List<State> components() {
		return Stream.of(ingredients(), upgrades()).flatMap(List::stream).filter(state -> !state.name().contains("schematic")).toList();
	}

	@Override
	public Construct upgrade(State upgrade) {
		return builder()
				.addIngredients(ingredients())
				.addUpgrades(upgrades.slots())
				.addUpgrade(upgrade)
				.type(type())
				.id(identifier())
				.build();
	}


	public ConstructBuilder toBuilder() {
		return builder()
				.addIngredients(ingredients())
				.addUpgrades(upgrades.slots())
				.type(type())
				.id(identifier());
	}

	@Override
	public ImmutableList<State> upgrades() {
		return ImmutableList.<State>builder().addAll(upgrades.entries()).build();
	}

	public List<Slot> slots() {
		return upgrades.slots();
	}

	public boolean canUpgrade(State state) {
		return upgrades.canUpgrade(state);
	}

	@Override
	public List<State> parts() {
		return ingredientList;
	}

	@Override
	public Composite copy() {
		return toBuilder().build();
	}

	@Override
	public DataContainer customData(Target target) {
		var combinedTarget = target.combineTarget(new TypeTarget(Set.of(type().typeName())));
		return components().stream().map(state -> state.customData(combinedTarget)).reduce(DataContainer.empty(), (dataContainer1, dataContainer2) -> DataContainer.transitiveMerge(dataContainer1, dataContainer2, combinedTarget));
	}

	public static class ConstructBuilder extends BaseComposite.BaseCompositeBuilder<ConstructBuilder> {

		public ConstructBuilder() {
			this.ingredientList = new ArrayList<>();
			this.upgradeContainer = SlotContainer.of(Collections.emptyList());
		}

		public ConstructBuilder(List<? extends Slot> upgradeSlots) {
			this.ingredientList = new ArrayList<>();
			this.upgradeContainer = SlotContainer.of(upgradeSlots);
		}

		public Construct build() {
			if (name == null && !ingredientList.isEmpty()) {
				this.name = compositor.compositeName(ingredientList);
			}
			return new Construct(ingredientList, upgradeContainer, name, nameSpace, type);
		}
	}
}
