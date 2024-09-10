package com.sigmundgranaas.forgero.core.state.composite;

import static com.sigmundgranaas.forgero.core.condition.Conditions.UNBREAKABLE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import com.sigmundgranaas.forgero.core.condition.Conditional;
import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.soul.Soul;
import com.sigmundgranaas.forgero.core.soul.SoulBindable;
import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.IdentifiableContainer;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.SlotContainer;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class ConstructedTool extends ConstructedComposite implements SoulBindable, Conditional<ConstructedTool> {
	private final State head;
	private final State handle;

	private final List<PropertyContainer> conditions;

	private Integer hashCode;

	public ConstructedTool(State head, State handle, SlotContainer slots, IdentifiableContainer id) {
		super(slots, id, List.of(head, handle));
		this.conditions = Collections.emptyList();
		this.head = head;
		this.handle = handle;
	}

	public ConstructedTool(State head, State handle, SlotContainer slots, IdentifiableContainer id, List<PropertyContainer> conditions) {
		super(slots, id, List.of(head, handle));
		this.conditions = conditions;
		this.head = head;
		this.handle = handle;
	}

	@Override
	public ConstructedTool upgrade(State upgrade) {
		return toolBuilder().addUpgrade(upgrade).build();
	}

	@Override
	public ConstructedTool removeUpgrade(String id) {
		if (upgrades().stream().anyMatch(state -> state.identifier().contains(id))) {
			return toolBuilder()
					.addSlotContainer(slotContainer.remove(id))
					.build();
		} else {
			for (int i = 0; i < parts().size(); i++) {
				if (parts().get(i) instanceof Composite construct) {
					var compositeRemoved = construct.removeUpgrade(id);
					if (construct != compositeRemoved) {
						var ingredients = new ArrayList<>(parts());
						ingredients.set(i, compositeRemoved);
						var optBuilder = ToolBuilder.builder(ingredients).map(builder -> builder.addSlotContainer(slotContainer.copy())
								.conditions(conditions)
								.type(type())
								.id(identifier()));
						if (optBuilder.isPresent()) {
							return optBuilder.get().build();
						}
					}
				}
			}
			for (int i = 0; i < upgrades().size(); i++) {
				if (upgrades().get(i) instanceof Composite construct) {
					var compositeRemoved = construct.removeUpgrade(id);
					if (construct != compositeRemoved) {
						var slot = slots().get(i);
						var slots = new ArrayList<>(slots());
						slots.set(i, slot.empty().fill(compositeRemoved, slot.category()).orElse(slot.empty()));
						return toolBuilder()
								.addSlotContainer(SlotContainer.of(slots))
								.build();
					}
				}
			}
		}
		return this;
	}

	public State getHead() {
		return head;
	}

	public State getHandle() {
		return handle;
	}

	public ToolBuilder toolBuilder() {
		var head = getHead();
		if (head instanceof Composite composite) {
			head = composite.copy();
		}

		var handle = getHandle();
		if (handle instanceof Composite composite) {
			handle = composite.copy();
		}
		return ToolBuilder.builder(head, handle)
				.addSlotContainer(slotContainer.copy())
				.conditions(conditions)
				.type(type())
				.id(identifier());
	}

	@Override
	public @NotNull
	List<Property> applyProperty(Matchable target, MatchContext context) {
		return Stream.of(super.applyProperty(target, context), conditionProperties(target, context))
				.flatMap(List::stream)
				.toList();
	}

	@Override
	public @NotNull
	List<Property> getRootProperties() {
		return Stream.of(super.getRootProperties(), conditionProperties(Matchable.DEFAULT_TRUE, MatchContext.of())).flatMap(List::stream).toList();
	}

	@Override
	public @NotNull
	List<Property> getRootProperties(Matchable target, MatchContext context) {
		return Stream.of(super.getRootProperties(target, context), conditionProperties(target, context)).flatMap(List::stream).toList();
	}


	@Override
	public @NotNull
	List<Property> conditionProperties(Matchable target, MatchContext context) {
		return Conditional.super.conditionProperties(target, context);
	}

	@Override
	public ConstructedTool copy() {
		return toolBuilder().build();
	}

	@Override
	public State bind(Soul soul) {
		return toolBuilder().soul(soul).build();
	}

	@Override
	public List<PropertyContainer> localConditions() {
		List<PropertyContainer> customConditions = new ArrayList<>();
		if (ForgeroConfigurationLoader.configuration.enableUnbreakableTools && conditions.stream().noneMatch(condition -> condition == UNBREAKABLE)) {
			customConditions.add(UNBREAKABLE);
		}
		return Stream.of(conditions, customConditions).flatMap(List::stream).toList();
	}

	@Override
	public ConstructedTool applyCondition(PropertyContainer container) {
		return toolBuilder().condition(container).build();
	}

	@Override
	public ConstructedTool removeCondition(String identifier) {
		return toolBuilder().conditions(Conditional.removeConditions(conditions, identifier)).build();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ConstructedTool that)) return false;
		if (!super.equals(o)) return false;
		return Objects.equals(conditions, that.conditions);
	}

	@Override
	public int hashCode() {
		if (hashCode == null) {
			this.hashCode = Objects.hash(super.hashCode(), conditions);
		}
		return hashCode;
	}

	@Getter
	public static class ToolBuilder extends BaseCompositeBuilder<ToolBuilder> {
		protected State head;
		protected State handle;
		protected List<PropertyContainer> conditions;

		public ToolBuilder(State head, State handle) {
			this.head = head;
			this.handle = handle;
			this.upgradeContainer = SlotContainer.of(Collections.emptyList());
			this.ingredientList = List.of(head, handle);
			this.conditions = new ArrayList<>();
		}

		public static ToolBuilder builder(State head, State handle) {
			return new ToolBuilder(head, handle);
		}

		public static Optional<ToolBuilder> builder(List<State> parts) {
			var head = parts.stream().filter(part -> part.test(Type.TOOL_PART_HEAD) || part.test(Type.WEAPON_HEAD) || part.test(Type.BOW_LIMB)).findFirst();
			var handle = parts.stream().filter(part -> head.orElse(null) != part).findFirst();
			if (head.isPresent() && handle.isPresent()) {
				return Optional.of(builder(head.get(), handle.get()));
			}
			return Optional.empty();
		}

		public ToolBuilder head(State head) {
			this.head = head;
			return this;
		}

		public ToolBuilder handle(State handle) {
			this.handle = handle;
			return this;
		}

		public ToolBuilder condition(PropertyContainer condition) {
			this.conditions.add(condition);
			return this;
		}

		public ToolBuilder conditions(List<PropertyContainer> conditions) {
			this.conditions = conditions;
			return this;
		}

		public SoulBoundTool.SoulBoundToolBuilder soul(Soul soul) {
			return SoulBoundTool.SoulBoundToolBuilder.of(this, soul);
		}

		public ConstructedTool build() {
			compositeName();
			var id = new IdentifiableContainer(name, nameSpace, type);
			return new ConstructedTool(head, handle, upgradeContainer, id, conditions);
		}
	}
}
