package com.sigmundgranaas.forgero.core.state.composite;

import static com.sigmundgranaas.forgero.core.state.composite.ConstructedComposite.ConstructBuilder.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.sigmundgranaas.forgero.core.context.Contexts;
import com.sigmundgranaas.forgero.core.customdata.DataContainer;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.property.attribute.TypeTarget;
import com.sigmundgranaas.forgero.core.property.v2.CompositePropertyProcessor;
import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.IdentifiableContainer;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.SlotContainer;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import com.sigmundgranaas.forgero.core.util.match.NameMatch;
import org.jetbrains.annotations.NotNull;

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
	public ConstructedComposite removeUpgrade(String id) {
		return this;
	}

	@Override
	public List<Property> compositeProperties(Target target) {
		var propertyProcessor = new CompositePropertyProcessor();
		var props = new ArrayList<>(super.compositeProperties(target));

		var partProps = parts().stream()
				.map(part -> part.applyProperty(target))
				.flatMap(List::stream)
				.toList();

		props.addAll(propertyProcessor.process(partProps));

		var otherProps = partProps.stream()
				.filter(this::filterNormalProperties)
				.toList();
		props.addAll(otherProps);

		return props;
	}

	private boolean filterNormalProperties(Property property) {
		if (property instanceof com.sigmundgranaas.forgero.core.property.Attribute attribute) {
			return attribute.getContext().test(Contexts.UNDEFINED);
		}
		return true;
	}

	@Override
	public List<State> parts() {
		return parts;
	}

	public ConstructBuilder toBuilder() {
		return builder()
				.addIngredients(parts().stream().map(part -> {
					if (part instanceof Composite composite) {
						return composite.copy();
					} else {
						return part;
					}
				}).toList())
				.addSlotContainer(slotContainer.copy())
				.type(type())
				.id(identifier());
	}

	@Override
	public State strip() {
		var builder = builder();
		builder.addSlotContainer(getSlotContainer().strip());
		parts().stream().map(State::strip).forEach(builder::addIngredient);
		builder.type(type());
		builder.id(identifier());
		return builder.build();
	}

	@Override
	public boolean test(Matchable match, MatchContext context) {
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

	@Override
	public DataContainer customData(Target target) {
		var combinedTarget = target.combineTarget(new TypeTarget(Set.of(type().typeName())));
		return components().stream().map(state -> state.customData(combinedTarget)).reduce(DataContainer.empty(), (dataContainer1, dataContainer2) -> DataContainer.transitiveMerge(dataContainer1, dataContainer2, combinedTarget));
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
