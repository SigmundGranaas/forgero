package com.sigmundgranaas.forgero.loader.gametest;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.common.tags.engine.TaggedRegistry;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.core.condition.predicate.HasOtherContributorCondition;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import com.sigmundgranaas.forgero.loader.api.ForgeroInitializedCallback;
import com.sigmundgranaas.forgero.loader.api.ForgeroServices;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Game tests for validating that Forgero data loading works correctly.
 * These tests verify that materials, conditions, and the data pipeline
 * produce expected runtime components.
 */
public class DataLoadingValidationTest {

	private static ForgeroServices services;
	private static TagResolver tagResolver;
	private static ComponentRegistry componentRegistry;
	private static TaggedRegistry<Component> taggedComponents;

	static {
		ForgeroInitializedCallback.EVENT.register(s -> {
			services = s;
			tagResolver = s.tagResolver();
			componentRegistry = s.componentRegistry();
			taggedComponents = s.taggedComponents();
		});
	}

	/**
	 * Verifies that vanilla materials are loaded from minecraft-vanilla-materials content module.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "data_loading", required = true)
	public void testVanillaMaterialsLoaded(TestContext context) {
		assertNotNull(componentRegistry, "ComponentRegistry should be available");

		// Check for iron material - a core vanilla material
		var ironOpt = componentRegistry.get(new OpenIdentifier("forgero", "iron"));
		assertTrue(ironOpt.isPresent(), "Iron material should be loaded");

		context.complete();
	}

	/**
	 * Verifies that materials have attributes with conditions.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "data_loading", required = true)
	public void testMaterialsHaveAttributes(TestContext context) {
		var ironOpt = componentRegistry.get(new OpenIdentifier("forgero", "iron"));
		if (ironOpt.isEmpty()) {
			// Iron not loaded - fail the test
			fail("Iron material not loaded - cannot test attributes");
			return;
		}

		Component iron = ironOpt.get();
		List<? extends Attribute> attributes = iron.properties(Attribute.KEY);

		assertFalse(attributes.isEmpty(), "Iron should have attributes defined");

		// Verify at least one attribute has the HasOtherContributorCondition
		boolean hasOtherContributorCondition = attributes.stream()
				.anyMatch(attr -> attr.condition().map(this::hasOtherContributorCondition).orElse(false));

		assertTrue(hasOtherContributorCondition,
				"At least one attribute should have HasOtherContributorCondition");

		context.complete();
	}

	private boolean hasOtherContributorCondition(Condition condition) {
		for (StaticCondition sc : condition.staticConditions()) {
			if (sc instanceof HasOtherContributorCondition) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Verifies that wood materials are loaded.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "data_loading", required = true)
	public void testWoodMaterialsLoaded(TestContext context) {
		var oakOpt = componentRegistry.get(new OpenIdentifier("forgero", "oak"));
		assertTrue(oakOpt.isPresent(), "Oak material should be loaded");

		var birchOpt = componentRegistry.get(new OpenIdentifier("forgero", "birch"));
		assertTrue(birchOpt.isPresent(), "Birch material should be loaded");

		context.complete();
	}

	/**
	 * Verifies that stone materials are loaded.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "data_loading", required = true)
	public void testStoneMaterialsLoaded(TestContext context) {
		var stoneOpt = componentRegistry.get(new OpenIdentifier("forgero", "stone"));
		assertTrue(stoneOpt.isPresent(), "Stone material should be loaded");

		var cobblestoneOpt = componentRegistry.get(new OpenIdentifier("forgero", "cobblestone"));
		assertTrue(cobblestoneOpt.isPresent(), "Cobblestone material should be loaded");

		context.complete();
	}

	/**
	 * Verifies that metal materials are loaded.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "data_loading", required = true)
	public void testMetalMaterialsLoaded(TestContext context) {
		var ironOpt = componentRegistry.get(new OpenIdentifier("forgero", "iron"));
		assertTrue(ironOpt.isPresent(), "Iron material should be loaded");

		var goldOpt = componentRegistry.get(new OpenIdentifier("forgero", "gold"));
		assertTrue(goldOpt.isPresent(), "Gold material should be loaded");

		var diamondOpt = componentRegistry.get(new OpenIdentifier("forgero", "diamond"));
		assertTrue(diamondOpt.isPresent(), "Diamond material should be loaded");

		context.complete();
	}

	/**
	 * Verifies that materials are correctly tagged.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "data_loading", required = true)
	public void testMaterialsHaveTags(TestContext context) {
		assertNotNull(taggedComponents, "TaggedRegistry should be available");

		// Get all components with the material tag
		var materialTag = new OpenIdentifier("forgero", "materials/material");
		var materials = taggedComponents.findByTag(materialTag);

		assertFalse(materials.isEmpty(), "Should have materials tagged with 'materials/material'");

		context.complete();
	}

	/**
	 * Verifies that metal materials are tagged correctly.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "data_loading", required = true)
	public void testMetalTagsExist(TestContext context) {
		var metalTag = new OpenIdentifier("forgero", "materials/metal");
		var metals = taggedComponents.findByTag(metalTag);

		assertFalse(metals.isEmpty(), "Should have materials tagged with 'materials/metal'");

		// Verify iron is in the metal tag
		boolean hasIron = metals.stream()
				.anyMatch(c -> c.id().path().equals("iron"));
		assertTrue(hasIron, "Iron should be tagged as metal");

		context.complete();
	}

	/**
	 * Verifies that component count is reasonable (sanity check).
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "data_loading", required = true)
	public void testComponentCountReasonable(TestContext context) {
		var allComponents = componentRegistry.all();
		int count = allComponents.size();

		// We expect at least the vanilla materials (around 40+)
		assertTrue(count > 10, "Should have more than 10 components loaded, but found: " + count);

		context.complete();
	}
}
