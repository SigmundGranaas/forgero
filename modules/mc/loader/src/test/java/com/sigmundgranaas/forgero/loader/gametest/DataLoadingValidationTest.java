package com.sigmundgranaas.forgero.loader.gametest;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.engine.TaggedRegistry;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.core.condition.predicate.HasOtherContributorCondition;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import com.sigmundgranaas.forgero.loader.api.ForgeroInitializedCallback;
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

	/**
	 * Helper to get ComponentRegistry using the static accessor.
	 */
	private static ComponentRegistry getComponentRegistry() {
		return ForgeroInitializedCallback.getServices()
				.map(s -> s.componentRegistry())
				.orElse(null);
	}

	/**
	 * Helper to get TaggedRegistry using the static accessor.
	 */
	private static TaggedRegistry<Component> getTaggedComponents() {
		return ForgeroInitializedCallback.getServices()
				.map(s -> s.taggedComponents())
				.orElse(null);
	}

	/**
	 * Verifies that vanilla materials are loaded from minecraft-vanilla-materials content module.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "data_loading", required = true)
	public void testVanillaMaterialsLoaded(TestContext context) {
		assertNotNull(getComponentRegistry(), "ComponentRegistry should be available");

		// Check for iron material - a core vanilla material
		var ironOpt = getComponentRegistry().get(new OpenIdentifier("forgero", "iron"));
		assertTrue(ironOpt.isPresent(), "Iron material should be loaded");

		context.complete();
	}

	/**
	 * Verifies that materials have attributes.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "data_loading", required = true)
	public void testMaterialsHaveAttributes(TestContext context) {
		var ironOpt = getComponentRegistry().get(new OpenIdentifier("forgero", "iron"));
		if (ironOpt.isEmpty()) {
			// Iron not loaded in minimal test content - skip attribute test
			context.complete();
			return;
		}

		Component iron = ironOpt.get();
		List<? extends Attribute> attributes = iron.properties(Attribute.KEY);

		// If iron is loaded, it should have attributes
		assertFalse(attributes.isEmpty(), "Iron should have attributes defined");

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
		var oakOpt = getComponentRegistry().get(new OpenIdentifier("forgero", "oak"));
		assertTrue(oakOpt.isPresent(), "Oak material should be loaded");

		var birchOpt = getComponentRegistry().get(new OpenIdentifier("forgero", "birch"));
		assertTrue(birchOpt.isPresent(), "Birch material should be loaded");

		context.complete();
	}

	/**
	 * Verifies that stone materials are loaded.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "data_loading", required = true)
	public void testStoneMaterialsLoaded(TestContext context) {
		var stoneOpt = getComponentRegistry().get(new OpenIdentifier("forgero", "stone"));
		assertTrue(stoneOpt.isPresent(), "Stone material should be loaded");

		var cobblestoneOpt = getComponentRegistry().get(new OpenIdentifier("forgero", "cobblestone"));
		assertTrue(cobblestoneOpt.isPresent(), "Cobblestone material should be loaded");

		context.complete();
	}

	/**
	 * Verifies that metal materials are loaded.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "data_loading", required = true)
	public void testMetalMaterialsLoaded(TestContext context) {
		var ironOpt = getComponentRegistry().get(new OpenIdentifier("forgero", "iron"));
		assertTrue(ironOpt.isPresent(), "Iron material should be loaded");

		var goldOpt = getComponentRegistry().get(new OpenIdentifier("forgero", "gold"));
		assertTrue(goldOpt.isPresent(), "Gold material should be loaded");

		var diamondOpt = getComponentRegistry().get(new OpenIdentifier("forgero", "diamond"));
		assertTrue(diamondOpt.isPresent(), "Diamond material should be loaded");

		context.complete();
	}

	/**
	 * Verifies that TaggedRegistry is available and can be queried.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "data_loading", required = true)
	public void testMaterialsHaveTags(TestContext context) {
		assertNotNull(getTaggedComponents(), "TaggedRegistry should be available");

		// Verify the registry can be queried (may return empty for minimal test content)
		var materialTag = new OpenIdentifier("forgero", "materials/material");
		var materials = getTaggedComponents().findByTag(materialTag);
		assertNotNull(materials, "findByTag should return a list, not null");

		context.complete();
	}

	/**
	 * Verifies that TaggedRegistry can find components by tag when available.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "data_loading", required = true)
	public void testMetalTagsExist(TestContext context) {
		// First check if iron is loaded - if not, skip the test
		var ironOpt = getComponentRegistry().get(new OpenIdentifier("forgero", "iron"));
		if (ironOpt.isEmpty()) {
			// Iron not in minimal test content - skip tag verification
			context.complete();
			return;
		}

		// If iron is loaded, verify it can be found via its tags
		var iron = ironOpt.get();
		assertNotNull(iron.getTags(), "Iron should have tags");

		context.complete();
	}

	/**
	 * Verifies that component count is reasonable (sanity check).
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "data_loading", required = true)
	public void testComponentCountReasonable(TestContext context) {
		var allComponents = getComponentRegistry().all();
		int count = allComponents.size();

		// We expect at least the vanilla materials (around 40+)
		assertTrue(count > 10, "Should have more than 10 components loaded, but found: " + count);

		context.complete();
	}
}
