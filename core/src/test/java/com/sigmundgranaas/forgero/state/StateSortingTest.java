package com.sigmundgranaas.forgero.state;

import java.util.List;

import com.sigmundgranaas.forgero.core.api.v0.identity.sorting.SortingRule;
import com.sigmundgranaas.forgero.core.api.v0.identity.sorting.SortingRuleRegistry;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.testutil.Materials;
import com.sigmundgranaas.forgero.testutil.Schematics;
import com.sigmundgranaas.forgero.testutil.ToolParts;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StateSortingTest {
	private SortingRuleRegistry registry;

	@BeforeEach
	public void prepareRegistry() {
		registry = SortingRuleRegistry.local();
		registry.registerRule("forgero:schematic", SortingRule.of(Type.SCHEMATIC, 20));
		registry.registerRule("forgero:material", SortingRule.of(Type.MATERIAL, 10));
		registry.registerRule("forgero:part", SortingRule.of(Type.PART, 30));
	}

	@Test
	public void materialAndSchematicOrdering() {
		var entries = List.of(Materials.IRON, Schematics.PICKAXE_HEAD_SCHEMATIC);

		var sorted = entries.stream().sorted(registry.getComparator()).toList();

		Assertions.assertEquals(entries, sorted);
	}

	@Test
	public void materialAndSchematicReverseOrdering() {
		var entries = List.of(Schematics.PICKAXE_HEAD_SCHEMATIC, Materials.IRON);

		var sorted = entries.stream().sorted(registry.getComparator()).toList();

		Assertions.assertEquals(List.of(Materials.IRON, Schematics.PICKAXE_HEAD_SCHEMATIC), sorted);
	}

	@Test
	public void partsAfterMaterials() {
		var entries = List.of(ToolParts.PICKAXE_HEAD, Materials.IRON);

		var sorted = entries.stream().sorted(registry.getComparator()).toList();

		Assertions.assertEquals(List.of(Materials.IRON, ToolParts.PICKAXE_HEAD), sorted);
	}

	@Test
	public void overrideRules() {
		registry.registerRule("forgero:part", SortingRule.of(Type.PART, 0));

		var entries = List.of(Materials.IRON, ToolParts.PICKAXE_HEAD);

		var sorted = entries.stream().sorted(registry.getComparator()).toList();

		Assertions.assertEquals(List.of(ToolParts.PICKAXE_HEAD, Materials.IRON), sorted);
	}
}
