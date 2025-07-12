package com.sigmundgranaas.forgero.core.property.condition;

import static com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.attribute.impl.AttributeEngine;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import com.sigmundgranaas.forgero.core.component.impl.StaticComponent;
import com.sigmundgranaas.forgero.core.component.impl.StructuredPart;
import com.sigmundgranaas.forgero.core.feature.api.Feature;
import com.sigmundgranaas.forgero.core.feature.impl.FeatureEngine;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import com.sigmundgranaas.forgero.core.property.context.ResolutionContext;
import com.sigmundgranaas.forgero.core.property.engine.ResolverEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * A test suite dedicated to validating advanced conditional properties that rely on the enriched
 * {@link ResolutionContext} with the new, decoupled {@link Resolver} system.
 */
public class AdvancedConditionalPropertyTest extends ForgeroTest {

	private Resolver resolver;

	@BeforeEach
	void setUp() {
		// The resolver is configured with all available data type engines.
		resolver = new ResolverEngine();
	}

	@Test
	void testIsRootConditionForSchematicRecipe() {
		var recipeFeature = new Feature(idFactory.of("custom_recipe"), new Condition(List.of(StaticConditions.isRoot()), List.of()));
		var schematic = new StaticComponent(idFactory.of("pickaxe_head_schematic"), Set.of(), List.of(recipeFeature));
		var iron = material(IRON_ID, METAL_TAG);
		// Update ComponentStructure to use Map.of
		var head = new StructuredPart(PICKAXE_HEAD_ID, Set.of(), List.of(), new ComponentStructure(slotsMap(slot(idFactory.of("material_slot"), MATERIAL_ID, iron), slot(idFactory.of("schematic_slot"), SCHEMATIC_ID, schematic))));
		var handle = part(HANDLE_ID, WOOD_TAG);
		// Update ComponentStructure to use Map.of
		var pickaxe = new StructuredPart(PICKAXE_ID, Set.of(), List.of(), new ComponentStructure(slotsMap(slot(HEAD_SLOT_ID, PICKAXE_HEAD_TAG, head), slot(HANDLE_SLOT_ID, HANDLE_TAG, handle))));

		// When the schematic is part of a tool, the 'isRoot' condition is false for it.
		List<Feature> pickaxeFeatures = resolver.resolve(pickaxe, FeatureEngine.KEY).orElse(Collections.emptyList());
		assertTrue(pickaxeFeatures.stream().noneMatch(f -> f.type().path().equals("custom_recipe")), "The schematic's recipe feature should be inactive when it's part of a tool.");

		// When resolving the schematic directly, 'isRoot' is true.
		List<Feature> schematicFeatures = resolver.resolve(schematic, FeatureEngine.KEY).orElse(Collections.emptyList());
		assertEquals(1, schematicFeatures.size(), "Schematic should have one active feature when resolved as root.");
		assertEquals("custom_recipe", schematicFeatures.get(0).type().path());
	}

	@Test
	void testSelfInSlotConditionForVersatileGem() {
		var OFFENSIVE_SLOT_TYPE = idFactory.of("offensive_slot_type");
		var UTILITY_SLOT_TYPE = idFactory.of("utility_slot_type");
		var damageBonus = new SimpleAttribute(ATTACK_DAMAGE, 10f, new Condition(List.of(StaticConditions.selfInSlot(OFFENSIVE_SLOT_TYPE)), List.of()));
		var speedBonus = new SimpleAttribute(MINING_SPEED, 5f, new Condition(List.of(StaticConditions.selfInSlot(UTILITY_SLOT_TYPE)), List.of()));
		var powerCrystal = new StaticComponent(idFactory.of("power_crystal"), Set.of(GEM_TAG), List.of(damageBonus, speedBonus));

		var blade = part(BLADE_ID, METAL_TAG, List.of(new SimpleAttribute(ATTACK_DAMAGE, 5f)));
		// Update ComponentStructure to use Map.of
		var swordHilt = new StructuredPart(idFactory.of("hilt"), Set.of(), List.of(), new ComponentStructure(slotsMap(slot(idFactory.of("offensive_slot"), OFFENSIVE_SLOT_TYPE, powerCrystal))));
		// Update ComponentStructure to use Map.of
		var sword = new StructuredPart(SWORD_ID, Set.of(), List.of(), new ComponentStructure(slotsMap(slot(idFactory.of("blade_slot"), BLADE_TAG, blade), slot(idFactory.of("hilt_slot"), idFactory.of("hilt_type"), swordHilt))));

		var pickHead = part(PICKAXE_HEAD_ID, METAL_TAG, List.of(new SimpleAttribute(MINING_SPEED, 2f)));
		// Update ComponentStructure to use Map.of
		var pickHandle = new StructuredPart(HANDLE_ID, Set.of(), List.of(), new ComponentStructure(slotsMap(slot(idFactory.of("utility_slot"), UTILITY_SLOT_TYPE, powerCrystal))));
		// Update ComponentStructure to use Map.of
		var pickaxe = new StructuredPart(PICKAXE_ID, Set.of(), List.of(), new ComponentStructure(slotsMap(slot(HEAD_SLOT_ID, PICKAXE_HEAD_TAG, pickHead), slot(HANDLE_SLOT_ID, HANDLE_TAG, pickHandle))));

		Optional<AttributeQueryResult> swordAttributes = resolver.resolve(sword, AttributeEngine.KEY);
		assertTrue(swordAttributes.isPresent());
		assertEquals(15f, swordAttributes.get().getValue(ATTACK_DAMAGE), "Sword damage should be 5 (base) + 10 (crystal).");
		assertEquals(0f, swordAttributes.get().getValue(MINING_SPEED), "Sword should get no mining speed bonus.");

		Optional<AttributeQueryResult> pickaxeAttributes = resolver.resolve(pickaxe, AttributeEngine.KEY);
		assertTrue(pickaxeAttributes.isPresent());
		assertEquals(7f, pickaxeAttributes.get().getValue(MINING_SPEED), "Pickaxe speed should be 2 (base) + 5 (crystal).");
		assertEquals(0f, pickaxeAttributes.get().getValue(ATTACK_DAMAGE), "Pickaxe should get no damage bonus.");
	}

	@Test
	void testSiblingAndDepthConditionsForSynergyAndMaterials() {
		var SAPPHIRE_ID = idFactory.of("sapphire_of_ice");
		var fireDamageType = idFactory.of("fire_damage");
		var baseFireDamage = new SimpleAttribute(fireDamageType, 2f);
		var synergyFireDamage = new SimpleAttribute(fireDamageType, 3f, new Condition(List.of(StaticConditions.hasSibling(SAPPHIRE_ID)), List.of()));
		var ruby = new StaticComponent(idFactory.of("ruby_of_fire"), Set.of(GEM_TAG), List.of(baseFireDamage, synergyFireDamage));
		var sapphire = new StaticComponent(SAPPHIRE_ID, Set.of(GEM_TAG), List.of());

		var oakDurability = new SimpleAttribute(DURABILITY, 50f, new Condition(List.of(StaticConditions.atDepth(2)), List.of()));
		var oakSpeed = new SimpleAttribute(ATTACK_SPEED, 10f, new Condition(List.of(StaticConditions.atDepth(1)), List.of()));
		var enchantedOak = new StaticComponent(OAK_ID, Set.of(WOOD_TAG), List.of(oakDurability, oakSpeed));

		// Update ComponentStructure to use Map.of
		var hiltWithTwoGems = new StructuredPart(idFactory.of("synergy_hilt"), Set.of(), List.of(), new ComponentStructure(slotsMap(slot(idFactory.of("gem_slot_1"), GEM_SLOT_TYPE_TAG, ruby), slot(idFactory.of("gem_slot_2"), GEM_SLOT_TYPE_TAG, sapphire))));
		// Update ComponentStructure to use Map.of
		var swordWithSynergy = new StructuredPart(SWORD_ID, Set.of(), List.of(), new ComponentStructure(slotsMap(slot(idFactory.of("blade_slot"), BLADE_TAG, part(BLADE_ID, METAL_TAG)), slot(idFactory.of("hilt_slot"), idFactory.of("hilt_type"), hiltWithTwoGems))));
		// Update ComponentStructure to use Map.of
		var hiltWithOneGem = new StructuredPart(idFactory.of("lonely_hilt"), Set.of(), List.of(), new ComponentStructure(slotsMap(slot(idFactory.of("gem_slot_1"), GEM_SLOT_TYPE_TAG, ruby))));
		// Update ComponentStructure to use Map.of
		var swordWithoutSynergy = new StructuredPart(idFactory.of("lonely_sword"), Set.of(), List.of(), new ComponentStructure(slotsMap(slot(idFactory.of("blade_slot"), BLADE_TAG, part(BLADE_ID, METAL_TAG)), slot(idFactory.of("hilt_slot"), idFactory.of("hilt_type"), hiltWithOneGem))));

		// Update ComponentStructure to use Map.of
		var handleMadeOfOak = new StructuredPart(HANDLE_ID, Set.of(), List.of(), new ComponentStructure(slotsMap(slot(idFactory.of("material_slot"), MATERIAL_ID, enchantedOak))));
		// Update ComponentStructure to use Map.of
		var pickaxeWithOakMaterial = new StructuredPart(PICKAXE_ID, Set.of(), List.of(), new ComponentStructure(slotsMap(slot(HEAD_SLOT_ID, PICKAXE_HEAD_TAG, part(PICKAXE_HEAD_ID, METAL_TAG)), slot(HANDLE_SLOT_ID, HANDLE_TAG, handleMadeOfOak))));
		// Update ComponentStructure to use Map.of
		var wand = new StructuredPart(idFactory.of("wand"), Set.of(), List.of(), new ComponentStructure(slotsMap(slot(idFactory.of("core_slot"), idFactory.of("core_type"), enchantedOak))));

		float synergyDamage = resolver.resolve(swordWithSynergy, AttributeEngine.KEY).map(res -> res.getValue(fireDamageType)).orElse(0f);
		assertEquals(5f, synergyDamage, "Ruby with Sapphire sibling should have 2+3=5 fire damage.");

		float noSynergyDamage = resolver.resolve(swordWithoutSynergy, AttributeEngine.KEY).map(res -> res.getValue(fireDamageType)).orElse(0f);
		assertEquals(2f, noSynergyDamage, "Ruby without Sapphire sibling should only have 2 fire damage.");

		AttributeQueryResult pickaxeResult = resolver.resolve(pickaxeWithOakMaterial, AttributeEngine.KEY).orElseThrow();
		assertEquals(50f, pickaxeResult.getValue(DURABILITY), "Oak at depth 2 should provide 50 durability.");
		assertEquals(0f, pickaxeResult.getValue(ATTACK_SPEED), "Oak at depth 2 should provide 0 attack speed.");

		AttributeQueryResult wandResult = resolver.resolve(wand, AttributeEngine.KEY).orElseThrow();
		assertEquals(10f, wandResult.getValue(ATTACK_SPEED), "Oak at depth 1 should provide 10 attack speed.");
		assertEquals(0f, wandResult.getValue(DURABILITY), "Oak at depth 1 should provide 0 durability.");
	}
}
