package com.sigmundgranaas.forgero.state;

import java.util.List;

import com.sigmundgranaas.forgero.core.api.identity.DefaultRules;
import com.sigmundgranaas.forgero.core.api.identity.ModificationRuleRegistry;
import com.sigmundgranaas.forgero.core.api.identity.sorting.SortingRuleRegistry;
import com.sigmundgranaas.forgero.core.state.NameCompositor;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.testutil.Materials;
import com.sigmundgranaas.forgero.testutil.Schematics;
import com.sigmundgranaas.forgero.testutil.ToolParts;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CombinedNameConditionTest {
	private ModificationRuleRegistry modificationRuleRegistry;
	private SortingRuleRegistry sorting;
	private NameCompositor compositor;


	@BeforeEach
	public void prepareRegistry() {
		modificationRuleRegistry = ModificationRuleRegistry.staticRegistry();
		modificationRuleRegistry.registerRule("forgero:schematic", DefaultRules.schematic.build());
		sorting = SortingRuleRegistry.staticRegistry();
		compositor = new NameCompositor(modificationRuleRegistry, sorting);
	}

	@Test
	public void testPickaxe() {
		modificationRuleRegistry.registerRule("forgero:handle", DefaultRules.handle.build());
		modificationRuleRegistry.registerRule("forgero:pickaxe", DefaultRules.pickaxe.build());

		List<State> parts = List.of(ToolParts.PICKAXE_HEAD, ToolParts.HANDLE);

		Assertions.assertEquals("iron-pickaxe", compositor.compositeName(parts));
	}

	@Test
	public void testSword() {
		modificationRuleRegistry.registerRule("forgero:handle", DefaultRules.handle.build());
		modificationRuleRegistry.registerRule("forgero:sword", DefaultRules.sword.build());

		List<State> parts = List.of(ToolParts.SWORD_BLADE, ToolParts.HANDLE);

		Assertions.assertEquals("iron-sword", compositor.compositeName(parts));
	}


	@Test
	public void testPickaxeHead() {
		List<State> parts = List.of(Materials.IRON, Schematics.PICKAXE_HEAD_SCHEMATIC);

		Assertions.assertEquals("iron-pickaxe_head", compositor.compositeName(parts));
	}

	@Test
	public void testSaber() {
		modificationRuleRegistry.registerRule("forgero:schematic", DefaultRules.schematic.build());
		modificationRuleRegistry.registerRule("forgero:handle", DefaultRules.handle.build());
		modificationRuleRegistry.registerRule("forgero:sword", DefaultRules.sword.build());

		List<State> parts = List.of(ToolParts.SABER_BLADE, ToolParts.HANDLE);

		Assertions.assertEquals("iron-sword", compositor.compositeName(parts));
	}

	@Test
	public void testMastercraftedSword() {
		modificationRuleRegistry.registerRule("forgero:handle", DefaultRules.handle.build());
		modificationRuleRegistry.registerRule("forgero:sword", DefaultRules.sword.build());

		List<State> parts = List.of(ToolParts.MASTERCRAFTED_SWORD_BLADE, ToolParts.HANDLE);

		Assertions.assertEquals("iron-sword", compositor.compositeName(parts));
	}
}
