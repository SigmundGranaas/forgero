package com.sigmundgranaas.forgeroforge.state;

import static com.sigmundgranaas.forgeroforge.testutil.Conditions.SHARP;
import static com.sigmundgranaas.forgeroforge.testutil.Schematics.PICKAXE_HEAD_SCHEMATIC;
import static com.sigmundgranaas.forgeroforge.testutil.ToolParts.HANDLE;
import static com.sigmundgranaas.forgeroforge.testutil.ToolParts.PICKAXE_HEAD;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.sigmundgranaas.forgero.core.property.attribute.Category;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.composite.ConstructedSchematicPart;
import com.sigmundgranaas.forgero.core.state.composite.ConstructedTool;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.EmptySlot;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.SlotContainer;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgeroforge.testutil.Materials;
import com.sigmundgranaas.forgeroforge.testutil.Upgrades;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DeterministicHashCodeStateTest {
	@Test
	void hashCodeIsSameWhenEqual() {
		var defaultState = State.of("teststate", Type.UNDEFINED, Collections.emptyList());
		var copy = State.of("teststate", Type.UNDEFINED, Collections.emptyList());

		Assertions.assertEquals(defaultState, copy);
		Assertions.assertEquals(defaultState.hashCode(), copy.hashCode());
	}

	@Test
	void schematicPartHashCode() {
		var defaultState = ConstructedSchematicPart.SchematicPartBuilder
				.builder(PICKAXE_HEAD_SCHEMATIC, Materials.IRON)
				.id("forgero:part")
				.type(Type.TOOL_PART_HEAD)
				.conditions(List.of(SHARP))
				.addSlotContainer(SlotContainer.of(List.of(new EmptySlot(0, Type.GEM, "gem", Set.of(Category.ALL)))))
				.build();

		var copy = ConstructedSchematicPart.SchematicPartBuilder
				.builder(PICKAXE_HEAD_SCHEMATIC, Materials.IRON)
				.id("forgero:part")
				.type(Type.TOOL_PART_HEAD)
				.conditions(List.of(SHARP))
				.addSlotContainer(SlotContainer.of(List.of(new EmptySlot(0, Type.GEM, "gem", Set.of(Category.ALL)))))
				.build();

		Assertions.assertEquals(defaultState, copy);
		Assertions.assertEquals(defaultState.hashCode(), copy.hashCode());

		var upgraded = defaultState.upgrade(Upgrades.REDSTONE_GEM);
		var upgradedCopy = copy.upgrade(Upgrades.REDSTONE_GEM);

		Assertions.assertEquals(upgraded, upgradedCopy);
		Assertions.assertEquals(upgraded.hashCode(), upgradedCopy.hashCode());

		Assertions.assertNotEquals(defaultState, upgraded);
		Assertions.assertNotEquals(defaultState.hashCode(), upgraded.hashCode());
	}

	@Test
	void toolHashCode() {
		var defaultState = ConstructedTool.ToolBuilder
				.builder(PICKAXE_HEAD, HANDLE)
				.id("forgero:pickaxe")
				.type(Type.PICKAXE)
				.conditions(List.of(SHARP))
				.addSlotContainer(SlotContainer.of(List.of(new EmptySlot(0, Type.BINDING, "binding", Set.of(Category.ALL)))))
				.build();

		var copy = ConstructedTool.ToolBuilder
				.builder(PICKAXE_HEAD, HANDLE)
				.id("forgero:pickaxe")
				.type(Type.PICKAXE)
				.conditions(List.of(SHARP))
				.addSlotContainer(SlotContainer.of(List.of(new EmptySlot(0, Type.BINDING, "binding", Set.of(Category.ALL)))))
				.build();

		Assertions.assertEquals(defaultState, copy);
		Assertions.assertEquals(defaultState.hashCode(), copy.hashCode());

		var upgraded = defaultState.upgrade(Upgrades.BINDING);
		var upgradedCopy = copy.upgrade(Upgrades.BINDING);

		Assertions.assertEquals(upgraded, upgradedCopy);
		Assertions.assertEquals(upgraded.hashCode(), upgradedCopy.hashCode());

		Assertions.assertNotEquals(defaultState, upgraded);
		Assertions.assertNotEquals(defaultState.hashCode(), upgraded.hashCode());
	}
}
