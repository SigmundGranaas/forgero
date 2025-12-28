package com.sigmundgranaas.forgero.drp.gametest;

import com.sigmundgranaas.forgero.drp.testutil.DRPTestInitializer;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;

/**
 * GameTests for verifying DRP tag generation and resolution.
 */
public class TagGametest {

	private static final String TEST_NAMESPACE = DRPTestInitializer.TEST_NAMESPACE;

	/**
	 * Verifies that a simple item tag is generated and resolved correctly.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "drp_tags")
	public void testSimpleItemTag(TestContext context) {
		TagKey<Item> testTag = TagKey.of(RegistryKeys.ITEM, new Identifier(TEST_NAMESPACE, "test_items"));

		// Verify diamond is in the tag
		boolean hasDiamond = Items.DIAMOND.getRegistryEntry().isIn(testTag);
		context.assertTrue(hasDiamond, "Diamond should be in drp_test:test_items tag");

		// Verify emerald is in the tag
		boolean hasEmerald = Items.EMERALD.getRegistryEntry().isIn(testTag);
		context.assertTrue(hasEmerald, "Emerald should be in drp_test:test_items tag");

		// Verify gold ingot is in the tag
		boolean hasGold = Items.GOLD_INGOT.getRegistryEntry().isIn(testTag);
		context.assertTrue(hasGold, "Gold ingot should be in drp_test:test_items tag");

		// Verify iron ingot is NOT in the tag
		boolean hasIron = Items.IRON_INGOT.getRegistryEntry().isIn(testTag);
		context.assertFalse(hasIron, "Iron ingot should NOT be in drp_test:test_items tag");

		context.complete();
	}

	/**
	 * Verifies that a tag with tag references works correctly.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "drp_tags")
	public void testTagWithIncludes(TestContext context) {
		TagKey<Item> testGemsTag = TagKey.of(RegistryKeys.ITEM, new Identifier(TEST_NAMESPACE, "test_gems"));

		// Diamond should be in the tag (directly added)
		boolean hasDiamond = Items.DIAMOND.getRegistryEntry().isIn(testGemsTag);
		context.assertTrue(hasDiamond, "Diamond should be in drp_test:test_gems tag");

		// Emerald should be in the tag (directly added)
		boolean hasEmerald = Items.EMERALD.getRegistryEntry().isIn(testGemsTag);
		context.assertTrue(hasEmerald, "Emerald should be in drp_test:test_gems tag");

		// Iron ingot should be in via beacon_payment_items inclusion
		// (beacon_payment_items contains: diamond, emerald, gold_ingot, iron_ingot, netherite_ingot)
		boolean hasIronIngot = Items.IRON_INGOT.getRegistryEntry().isIn(testGemsTag);
		context.assertTrue(hasIronIngot, "Iron ingot should be in drp_test:test_gems via beacon_payment_items");

		context.complete();
	}

	/**
	 * Verifies that optional entries don't break tag loading.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "drp_tags")
	public void testOptionalEntries(TestContext context) {
		TagKey<Item> optionalTag = TagKey.of(RegistryKeys.ITEM, new Identifier(TEST_NAMESPACE, "optional_items"));

		// Stick should be in the tag (non-optional)
		boolean hasStick = Items.STICK.getRegistryEntry().isIn(optionalTag);
		context.assertTrue(hasStick, "Stick should be in drp_test:optional_items tag");

		// The tag should load without errors despite the fake optional item
		context.complete();
	}

	/**
	 * Verifies that block tags work correctly.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "drp_tags")
	public void testBlockTag(TestContext context) {
		var testBlocksTag = TagKey.of(RegistryKeys.BLOCK, new Identifier(TEST_NAMESPACE, "test_blocks"));

		// Stone should be in the tag
		boolean hasStone = Registries.BLOCK.get(new Identifier("minecraft", "stone"))
				.getRegistryEntry().isIn(testBlocksTag);
		context.assertTrue(hasStone, "Stone should be in drp_test:test_blocks tag");

		// Granite should be in the tag
		boolean hasGranite = Registries.BLOCK.get(new Identifier("minecraft", "granite"))
				.getRegistryEntry().isIn(testBlocksTag);
		context.assertTrue(hasGranite, "Granite should be in drp_test:test_blocks tag");

		context.complete();
	}

	/**
	 * Verifies that multiple tags can coexist without conflicts.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "drp_tags")
	public void testMultipleTags(TestContext context) {
		TagKey<Item> tag1 = TagKey.of(RegistryKeys.ITEM, new Identifier(TEST_NAMESPACE, "test_items"));
		TagKey<Item> tag2 = TagKey.of(RegistryKeys.ITEM, new Identifier(TEST_NAMESPACE, "test_gems"));
		TagKey<Item> tag3 = TagKey.of(RegistryKeys.ITEM, new Identifier(TEST_NAMESPACE, "optional_items"));

		// All tags should be queryable
		boolean diamond1 = Items.DIAMOND.getRegistryEntry().isIn(tag1);
		boolean diamond2 = Items.DIAMOND.getRegistryEntry().isIn(tag2);

		context.assertTrue(diamond1, "Diamond should be in test_items");
		context.assertTrue(diamond2, "Diamond should be in test_gems");

		// Stick should only be in optional_items
		boolean stickInOptional = Items.STICK.getRegistryEntry().isIn(tag3);
		boolean stickInItems = Items.STICK.getRegistryEntry().isIn(tag1);

		context.assertTrue(stickInOptional, "Stick should be in optional_items");
		context.assertFalse(stickInItems, "Stick should NOT be in test_items");

		context.complete();
	}
}
