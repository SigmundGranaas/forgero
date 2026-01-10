package com.sigmundgranaas.forgero.bows.gametest;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.loader.api.ForgeroApi;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;

/**
 * Tests that verify schematic items are properly registered.
 * <p>
 * These tests ensure that recipe generators have valid schematic items to reference,
 * preventing recipe warnings like "Missing items referenced by recipes: [forgero:bow_limb-schematic]".
 * <p>
 * If these tests fail, check:
 * 1. The schematic JSON files exist in content/forgero-bows/src/main/resources/data/forgero/schematics/
 * 2. The "host.create.id" field matches the expected item ID
 * 3. The schematic has proper type "forgero:schematic"
 */
public class SchematicItemRegistrationTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(SchematicItemRegistrationTest.class);

	/**
	 * Required schematics for bow/arrow recipes.
	 * These must exist as items for the recipe generators to work.
	 */
	private static final List<String> REQUIRED_BOW_SCHEMATICS = List.of(
			"forgero:bow_limb-schematic",
			"forgero:arrow_head-schematic"
	);

	/**
	 * Variant schematics that should exist as items but don't have recipe generators.
	 * These are used for crafting bonuses, not for generating new part types.
	 */
	private static final List<String> VARIANT_BOW_SCHEMATICS = List.of(
			"forgero:longbow_limb-schematic",
			"forgero:shortbow_limb-schematic",
			"forgero:refined_bow_limb-schematic",
			"forgero:mastercrafted_bow_limb-schematic",
			"forgero:refined_arrow_head-schematic",
			"forgero:mastercrafted_arrow_head-schematic"
	);

	/**
	 * Verifies that basic bow_limb-schematic item is registered.
	 * This schematic is required by the bow_limb recipe generator.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void bowLimbSchematicItemExists(TestContext context) {
		assertSchematicItemExists(context, "forgero:bow_limb-schematic",
				"Required for bow_limb recipe generator");
		context.complete();
	}

	/**
	 * Verifies that basic arrow_head-schematic item is registered.
	 * This schematic is required by the arrow_head recipe generator.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void arrowHeadSchematicItemExists(TestContext context) {
		assertSchematicItemExists(context, "forgero:arrow_head-schematic",
				"Required for arrow_head recipe generator");
		context.complete();
	}

	/**
	 * Verifies that longbow_limb-schematic variant item is registered.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void longbowLimbSchematicItemExists(TestContext context) {
		assertSchematicItemExists(context, "forgero:longbow_limb-schematic",
				"Longbow limb variant schematic");
		context.complete();
	}

	/**
	 * Verifies that shortbow_limb-schematic variant item is registered.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void shortbowLimbSchematicItemExists(TestContext context) {
		assertSchematicItemExists(context, "forgero:shortbow_limb-schematic",
				"Shortbow limb variant schematic");
		context.complete();
	}

	/**
	 * Verifies that refined_bow_limb-schematic variant item is registered.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void refinedBowLimbSchematicItemExists(TestContext context) {
		assertSchematicItemExists(context, "forgero:refined_bow_limb-schematic",
				"Refined bow limb variant schematic");
		context.complete();
	}

	/**
	 * Verifies that mastercrafted_bow_limb-schematic variant item is registered.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void mastercraftedBowLimbSchematicItemExists(TestContext context) {
		assertSchematicItemExists(context, "forgero:mastercrafted_bow_limb-schematic",
				"Mastercrafted bow limb variant schematic");
		context.complete();
	}

	/**
	 * Verifies that refined_arrow_head-schematic variant item is registered.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void refinedArrowHeadSchematicItemExists(TestContext context) {
		assertSchematicItemExists(context, "forgero:refined_arrow_head-schematic",
				"Refined arrow head variant schematic");
		context.complete();
	}

	/**
	 * Verifies that mastercrafted_arrow_head-schematic variant item is registered.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void mastercraftedArrowHeadSchematicItemExists(TestContext context) {
		assertSchematicItemExists(context, "forgero:mastercrafted_arrow_head-schematic",
				"Mastercrafted arrow head variant schematic");
		context.complete();
	}

	/**
	 * Comprehensive test that all required schematics are registered.
	 * This test provides a single point of failure if any schematic is missing.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void allRequiredSchematicsExist(TestContext context) {
		StringBuilder missingItems = new StringBuilder();

		for (String schematicId : REQUIRED_BOW_SCHEMATICS) {
			if (!itemExistsInRegistry(schematicId)) {
				missingItems.append("\n  - ").append(schematicId);
			}
		}

		context.assertTrue(missingItems.isEmpty(),
				"Required schematic items are missing from registry:" + missingItems +
						"\n\nFix: Ensure schematic JSON files exist with correct 'host.create.id' fields");

		context.complete();
	}

	/**
	 * Comprehensive test that all variant schematics are registered.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void allVariantSchematicsExist(TestContext context) {
		StringBuilder missingItems = new StringBuilder();

		for (String schematicId : VARIANT_BOW_SCHEMATICS) {
			if (!itemExistsInRegistry(schematicId)) {
				missingItems.append("\n  - ").append(schematicId);
			}
		}

		context.assertTrue(missingItems.isEmpty(),
				"Variant schematic items are missing from registry:" + missingItems +
						"\n\nFix: Ensure schematic JSON files exist with correct 'host.create.id' fields");

		context.complete();
	}

	/**
	 * Verifies that schematic components can be found in the component registry.
	 * This is a deeper check than just item registration.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void schematicsExistAsComponents(TestContext context) {
		// Check bow_limb schematic component
		OpenIdentifier bowLimbSchematicId = OpenIdentifier.of("forgero", "bow_limb_schematic");
		Optional<Component> bowLimbSchematic = ForgeroApi.componentRegistry().get(bowLimbSchematicId);

		context.assertTrue(bowLimbSchematic.isPresent(),
				"bow_limb_schematic should exist as a component in registry");

		// Check arrow_head schematic component
		OpenIdentifier arrowHeadSchematicId = OpenIdentifier.of("forgero", "arrow_head_schematic");
		Optional<Component> arrowHeadSchematic = ForgeroApi.componentRegistry().get(arrowHeadSchematicId);

		context.assertTrue(arrowHeadSchematic.isPresent(),
				"arrow_head_schematic should exist as a component in registry");

		context.complete();
	}

	// ==================== Helper Methods ====================

	private void assertSchematicItemExists(TestContext context, String itemId, String description) {
		boolean exists = itemExistsInRegistry(itemId);
		context.assertTrue(exists,
				String.format("Schematic item '%s' should be registered. %s", itemId, description));

		if (exists) {
			LOGGER.debug("Verified schematic item exists: {}", itemId);
		} else {
			LOGGER.error("Missing schematic item: {} - {}", itemId, description);
		}
	}

	private boolean itemExistsInRegistry(String itemId) {
		Identifier id = new Identifier(itemId);
		Item item = Registries.ITEM.get(id);
		return item != Items.AIR;
	}
}
