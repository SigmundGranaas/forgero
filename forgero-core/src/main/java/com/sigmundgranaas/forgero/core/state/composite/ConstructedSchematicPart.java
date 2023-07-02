package com.sigmundgranaas.forgero.core.state.composite;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.sigmundgranaas.forgero.core.condition.ConditionContainer;
import com.sigmundgranaas.forgero.core.condition.Conditional;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.state.IdentifiableContainer;
import com.sigmundgranaas.forgero.core.state.MaterialBased;
import com.sigmundgranaas.forgero.core.state.SchematicBased;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.SlotContainer;
import com.sigmundgranaas.forgero.core.type.Type;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class ConstructedSchematicPart extends ConstructedComposite implements MaterialBased, SchematicBased, Conditional<ConstructedSchematicPart> {
	private final State schematic;
	private final State baseMaterial;

	private final ConditionContainer conditions;

	public ConstructedSchematicPart(State schematic, State baseMaterial, SlotContainer slots, IdentifiableContainer id) {
		super(slots, id, List.of(schematic, baseMaterial));
		this.schematic = schematic;
		this.baseMaterial = baseMaterial;
		this.conditions = EMPTY;
	}

	public ConstructedSchematicPart(State schematic, State baseMaterial, SlotContainer slots, IdentifiableContainer id, ConditionContainer conditions) {
		super(slots, id, List.of(schematic, baseMaterial));
		this.schematic = schematic;
		this.baseMaterial = baseMaterial;
		this.conditions = conditions;
	}

	@Override
	public @NotNull
	List<Property> applyProperty(Target target) {
		return Stream.of(super.applyProperty(target), conditionProperties())
				.flatMap(List::stream)
				.toList();
	}

	@Override
	public @NotNull
	List<Property> getRootProperties() {
		return Stream.of(super.getRootProperties(), conditionProperties())
				.flatMap(List::stream)
				.toList();
	}

	@Override
	public State baseMaterial() {
		return baseMaterial;
	}

	@Override
	public ConstructedSchematicPart upgrade(State upgrade) {
		return partBuilder().addUpgrade(upgrade).build();
	}


	@Override
	public ConstructedSchematicPart removeUpgrade(String id) {
		if (upgrades().stream().anyMatch(state -> state.identifier().contains(id))) {
			return partBuilder()
					.addSlotContainer(slotContainer.remove(id))
					.build();
		}
		return this;
	}

	@Override
	public List<PropertyContainer> conditions() {
		return conditions.conditions();
	}

	@Override
	public ConstructedSchematicPart applyCondition(PropertyContainer container) {
		return partBuilder().condition(container).build();
	}

	public SchematicPartBuilder partBuilder() {
		return SchematicPartBuilder.builder(schematic(), baseMaterial())
				.addSlotContainer(slotContainer.copy())
				.conditions(conditions())
				.type(type())
				.id(identifier());
	}

	@Override
	public ConstructedSchematicPart removeCondition(String identifier) {
		return partBuilder().conditions(Conditional.removeConditions(conditions(), identifier)).build();
	}

	@Override
	public String name() {
		return id.name();
	}

	@Override
	public State schematic() {
		return schematic;
	}

	@Getter
	public static class SchematicPartBuilder extends BaseCompositeBuilder<SchematicPartBuilder> {
		protected State schematic;
		protected State baseMaterial;

		protected ConditionContainer conditions;

		public SchematicPartBuilder(State schematic, State material) {
			this.baseMaterial = material;
			this.schematic = schematic;
			this.upgradeContainer = SlotContainer.of(Collections.emptyList());
			this.ingredientList = List.of(schematic, material);
			this.conditions = Conditional.EMPTY;
		}

		public static SchematicPartBuilder builder(State schematic, State material) {
			return new SchematicPartBuilder(schematic, material);
		}

		public static Optional<SchematicPartBuilder> builder(List<State> parts) {
			var schematic = parts.stream().filter(part -> part.test(Type.SCHEMATIC)).findFirst();
			var material = parts.stream().filter(part -> part.test(Type.MATERIAL)).findFirst();
			if (schematic.isPresent() && material.isPresent()) {
				return Optional.of(builder(schematic.get(), material.get()));
			}
			return Optional.empty();
		}

		public SchematicPartBuilder condition(PropertyContainer condition) {
			this.conditions = conditions.applyCondition(condition);
			return this;
		}

		public SchematicPartBuilder conditions(List<PropertyContainer> conditions) {
			this.conditions = new ConditionContainer(conditions);
			return this;
		}

		public ConstructedSchematicPart build() {
			compositeName();
			var id = new IdentifiableContainer(name, nameSpace, type);
			return new ConstructedSchematicPart(schematic, baseMaterial, upgradeContainer, id, conditions);
		}
	}
}
