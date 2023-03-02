package com.sigmundgranaas.forgero.core.state.composite;

import com.sigmundgranaas.forgero.core.property.*;
import com.sigmundgranaas.forgero.core.property.attribute.AttributeBuilder;
import com.sigmundgranaas.forgero.core.property.attribute.Category;
import com.sigmundgranaas.forgero.core.property.attribute.TypeTarget;
import com.sigmundgranaas.forgero.core.state.IdentifiableContainer;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.SlotContainer;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.match.Context;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import com.sigmundgranaas.forgero.core.util.match.NameMatch;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.sigmundgranaas.forgero.core.state.composite.ConstructedComposite.ConstructBuilder.builder;

public class ConstructedComposite extends BaseComposite implements ConstructedState {
	private final List<State> parts;

	protected ConstructedComposite(SlotContainer slotContainer, IdentifiableContainer id, List<State> parts) {
		super(slotContainer, id);
		this.parts = parts;
	}

	@Override
	public List<State> components() {
		return Stream.of(parts(), upgrades()).flatMap(List::stream).toList();
	}

	@Override
	public ConstructedComposite upgrade(State upgrade) {
		return toBuilder()
				.addUpgrade(upgrade)
				.build();
	}

	@Override
	public @NotNull
	List<Property> applyProperty(Target target) {
		var newTarget = target.combineTarget(new TypeTarget(Set.of(id.type().typeName())));
		return compositeProperties(newTarget);
	}

	@Override
	public @NotNull
	List<Property> getRootProperties() {
		return compositeProperties(Target.EMPTY);
	}


	@Override
	public List<Property> compositeProperties(Target target) {
		var props = new ArrayList<Property>();

		var upgradeProps = super.compositeProperties(target);
		props.addAll(upgradeProps);

		var partProps = parts().stream().map(part -> part.applyProperty(target)).flatMap(List::stream).toList();
		props.addAll(combineCompositeProperties(partProps));

		var otherProps = partProps.stream().filter(this::filterNormalProperties).toList();
		props.addAll(otherProps);

		return props;
	}

	private boolean filterNormalProperties(Property property) {
		if (property instanceof Attribute attribute) {
			if (Category.UPGRADE_CATEGORIES.contains(attribute.getCategory())) {
				return false;
			} else if (attribute.getOrder() == CalculationOrder.COMPOSITE) {
				return false;
			}
		}
		return true;
	}

	private List<Property> combineCompositeProperties(List<Property> props) {
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
			var newBaseAttribute = new AttributeBuilder(type).applyOperation(NumericOperation.ADDITION).applyOrder(CalculationOrder.BASE);
			newBaseAttribute.applyValue(Property.stream(compositeAttributes).applyAttribute(type)).applyCategory(Category.PASS);
			var attribute = newBaseAttribute.build();
			if (attribute.getValue() != 0 && compositeAttributes.stream().filter(prop -> prop instanceof Attribute attribute1 && attribute1.getAttributeType().equals(type.toString())).toList().size() > 1) {
				newValues.add(newBaseAttribute.build());
			}
		}
		return newValues;
	}

	@Override
	public ConstructedComposite removeUpgrade(String id) {
		return this;
	}

	@Override
	public List<State> parts() {
		return parts;
	}

	public ConstructBuilder toBuilder() {
		return builder()
				.addIngredients(parts())
				.addUpgrades(slots())
				.type(type())
				.id(identifier());
	}

	@Override
	public boolean test(Matchable match, Context context) {
		if (match instanceof Type typeMatch) {
			if (this.type().test(typeMatch, context)) {
				return true;
			} else {
				return parts().stream().anyMatch(ingredient -> ingredient.test(match, context));
			}
		}
		if (match instanceof NameMatch name) {
			if (name.test(this, context)) {
				return true;
			} else {
				return parts().stream().anyMatch(ingredient -> ingredient.test(name, context));
			}
		}
		return false;
	}

	@Override
	public ConstructedComposite copy() {
		return toBuilder().build();
	}

	public static class ConstructBuilder extends BaseCompositeBuilder<ConstructBuilder> {
		public ConstructBuilder() {
			this.ingredientList = new ArrayList<>();
			this.upgradeContainer = SlotContainer.of(Collections.emptyList());
		}

		public static ConstructBuilder builder() {
			return new ConstructBuilder();
		}

		@Override
		public ConstructedComposite build() {
			compositeName();
			var id = new IdentifiableContainer(name, nameSpace, type);
			return new ConstructedComposite(upgradeContainer, id, ingredientList);
		}
	}
}
