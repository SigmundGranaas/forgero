package com.sigmundgranaas.vanillaupgrades.tests;

import com.sigmundgranaas.forgero.common.api.item.ItemMutationApi;
import com.sigmundgranaas.forgero.common.api.item.ItemQueryApi;
import com.sigmundgranaas.forgero.loader.api.ForgeroApi;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

/**
 * Tests for the upgrade slot system on vanilla items.
 *
 * These tests verify that:
 * 1. Vanilla items are recognized as Forgero components
 * 2. Upgrade slots are present and accessible
 * 3. The upgrade query/mutation APIs work correctly
 *
 * If the upgrade system breaks, these tests WILL FAIL.
 */
public class VanillaUpgradeSlotTest implements FabricGameTest {

	// =============================================
	// ITEM RECOGNITION TESTS
	// =============================================

	/**
	 * Diamond sword should be recognized as a Forgero item.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void diamondSwordIsRecognizedAsForgeroItem(TestContext context) {
		ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
		ItemQueryApi query = ForgeroApi.itemQuery();

		boolean isForgero = query.isForgeroItem(sword);

		context.assertTrue(isForgero,
			"Diamond sword should be recognized as a Forgero item");
		context.complete();
	}

	/**
	 * Diamond pickaxe should be recognized as a Forgero item.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void diamondPickaxeIsRecognizedAsForgeroItem(TestContext context) {
		ItemStack pickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
		ItemQueryApi query = ForgeroApi.itemQuery();

		boolean isForgero = query.isForgeroItem(pickaxe);

		context.assertTrue(isForgero,
			"Diamond pickaxe should be recognized as a Forgero item");
		context.complete();
	}

	/**
	 * Diamond chestplate should be recognized as a Forgero item.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void diamondChestplateIsRecognizedAsForgeroItem(TestContext context) {
		ItemStack chestplate = new ItemStack(Items.DIAMOND_CHESTPLATE);
		ItemQueryApi query = ForgeroApi.itemQuery();

		boolean isForgero = query.isForgeroItem(chestplate);

		context.assertTrue(isForgero,
			"Diamond chestplate should be recognized as a Forgero item");
		context.complete();
	}

	/**
	 * Iron helmet should be recognized as a Forgero item.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void ironHelmetIsRecognizedAsForgeroItem(TestContext context) {
		ItemStack helmet = new ItemStack(Items.IRON_HELMET);
		ItemQueryApi query = ForgeroApi.itemQuery();

		boolean isForgero = query.isForgeroItem(helmet);

		context.assertTrue(isForgero,
			"Iron helmet should be recognized as a Forgero item");
		context.complete();
	}

	// =============================================
	// CUSTOMIZABLE TESTS
	// =============================================

	/**
	 * Diamond sword should be customizable (have upgrade slots).
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void diamondSwordIsCustomizable(TestContext context) {
		ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
		ItemQueryApi query = ForgeroApi.itemQuery();

		boolean isCustomizable = query.isCustomizable(sword);

		context.assertTrue(isCustomizable,
			"Diamond sword should be customizable (have upgrade slots)");
		context.complete();
	}

	/**
	 * Diamond pickaxe should be customizable.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void diamondPickaxeIsCustomizable(TestContext context) {
		ItemStack pickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
		ItemQueryApi query = ForgeroApi.itemQuery();

		boolean isCustomizable = query.isCustomizable(pickaxe);

		context.assertTrue(isCustomizable,
			"Diamond pickaxe should be customizable");
		context.complete();
	}

	/**
	 * Diamond chestplate should be customizable.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void diamondChestplateIsCustomizable(TestContext context) {
		ItemStack chestplate = new ItemStack(Items.DIAMOND_CHESTPLATE);
		ItemQueryApi query = ForgeroApi.itemQuery();

		boolean isCustomizable = query.isCustomizable(chestplate);

		context.assertTrue(isCustomizable,
			"Diamond chestplate should be customizable (have upgrade slots)");
		context.complete();
	}

	// =============================================
	// UPGRADE SLOT COUNT TESTS
	// =============================================

	/**
	 * Diamond sword should have upgrade slots available.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void diamondSwordHasUpgradeSlots(TestContext context) {
		ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
		ItemQueryApi query = ForgeroApi.itemQuery();

		int slotCount = query.getUpgradeSlotCount(sword);

		context.assertTrue(slotCount > 0,
			"Diamond sword should have at least 1 upgrade slot, but has " + slotCount);
		context.complete();
	}

	/**
	 * Diamond pickaxe should have upgrade slots available.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void diamondPickaxeHasUpgradeSlots(TestContext context) {
		ItemStack pickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
		ItemQueryApi query = ForgeroApi.itemQuery();

		int slotCount = query.getUpgradeSlotCount(pickaxe);

		context.assertTrue(slotCount > 0,
			"Diamond pickaxe should have at least 1 upgrade slot, but has " + slotCount);
		context.complete();
	}

	/**
	 * Diamond chestplate should have upgrade slots (lining + reinforcement at diamond tier).
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void diamondChestplateHasMultipleUpgradeSlots(TestContext context) {
		ItemStack chestplate = new ItemStack(Items.DIAMOND_CHESTPLATE);
		ItemQueryApi query = ForgeroApi.itemQuery();

		int slotCount = query.getUpgradeSlotCount(chestplate);

		// Diamond tier should have 2 slots: lining + reinforcement
		context.assertTrue(slotCount >= 2,
			"Diamond chestplate should have at least 2 upgrade slots, but has " + slotCount);
		context.complete();
	}

	/**
	 * Iron helmet should have at least 1 upgrade slot (lining).
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void ironHelmetHasUpgradeSlot(TestContext context) {
		ItemStack helmet = new ItemStack(Items.IRON_HELMET);
		ItemQueryApi query = ForgeroApi.itemQuery();

		int slotCount = query.getUpgradeSlotCount(helmet);

		context.assertTrue(slotCount >= 1,
			"Iron helmet should have at least 1 upgrade slot (lining), but has " + slotCount);
		context.complete();
	}

	// =============================================
	// EMPTY SLOT TESTS
	// =============================================

	/**
	 * Fresh diamond sword should have empty upgrade slots.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void freshDiamondSwordHasEmptySlots(TestContext context) {
		ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
		ItemQueryApi query = ForgeroApi.itemQuery();

		boolean hasEmpty = query.hasEmptySlots(sword);
		int emptyCount = query.getEmptySlotCount(sword);

		context.assertTrue(hasEmpty,
			"Fresh diamond sword should have empty upgrade slots");
		context.assertTrue(emptyCount > 0,
			"Fresh diamond sword should have at least 1 empty slot, but has " + emptyCount);
		context.complete();
	}

	/**
	 * Fresh diamond chestplate should have empty upgrade slots.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void freshDiamondChestplateHasEmptySlots(TestContext context) {
		ItemStack chestplate = new ItemStack(Items.DIAMOND_CHESTPLATE);
		ItemQueryApi query = ForgeroApi.itemQuery();

		boolean hasEmpty = query.hasEmptySlots(chestplate);
		int emptyCount = query.getEmptySlotCount(chestplate);

		context.assertTrue(hasEmpty,
			"Fresh diamond chestplate should have empty upgrade slots");
		context.assertTrue(emptyCount >= 2,
			"Fresh diamond chestplate should have at least 2 empty slots, but has " + emptyCount);
		context.complete();
	}

	// =============================================
	// ATTRIBUTE QUERY TESTS
	// =============================================

	/**
	 * ItemQueryApi should return correct attack damage for diamond sword.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void queryApiReturnsCorrectAttackDamage(TestContext context) {
		ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
		ItemQueryApi query = ForgeroApi.itemQuery();

		float damage = query.getAttackDamage(sword);

		context.assertTrue(damage == 7.0f,
			"ItemQueryApi should return 7.0 attack damage for diamond sword, but returned " + damage);
		context.complete();
	}

	/**
	 * ItemQueryApi should return correct mining speed for diamond pickaxe.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void queryApiReturnsCorrectMiningSpeed(TestContext context) {
		ItemStack pickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
		ItemQueryApi query = ForgeroApi.itemQuery();

		float speed = query.getMiningSpeed(pickaxe);

		context.assertTrue(speed == 8.0f,
			"ItemQueryApi should return 8.0 mining speed for diamond pickaxe, but returned " + speed);
		context.complete();
	}

	/**
	 * ItemQueryApi should return correct durability for iron sword.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void queryApiReturnsCorrectDurability(TestContext context) {
		ItemStack sword = new ItemStack(Items.IRON_SWORD);
		ItemQueryApi query = ForgeroApi.itemQuery();

		int durability = query.getMaxDurability(sword);

		context.assertTrue(durability == 250,
			"ItemQueryApi should return 250 durability for iron sword, but returned " + durability);
		context.complete();
	}

	/**
	 * ItemQueryApi should return correct armor value for diamond chestplate.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void queryApiReturnsCorrectArmor(TestContext context) {
		ItemStack chestplate = new ItemStack(Items.DIAMOND_CHESTPLATE);
		ItemQueryApi query = ForgeroApi.itemQuery();

		int armor = query.getArmor(chestplate);

		context.assertTrue(armor == 8,
			"ItemQueryApi should return 8 armor for diamond chestplate, but returned " + armor);
		context.complete();
	}

	// =============================================
	// HANDLE COMPONENT TESTS
	// =============================================

	/**
	 * Stick IS a Forgero item (it's a handle component).
	 * This test verifies the system correctly recognizes sticks.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void stickIsRecognizedAsHandle(TestContext context) {
		ItemStack stick = new ItemStack(Items.STICK);
		ItemQueryApi query = ForgeroApi.itemQuery();

		boolean isForgero = query.isForgeroItem(stick);

		context.assertTrue(isForgero,
			"Stick should be recognized as a Forgero item (wooden handle)");
		context.complete();
	}

	// =============================================
	// NON-FORGERO ITEM TESTS
	// =============================================

	/**
	 * Bucket is NOT a Forgero item.
	 * Note: Cobblestone IS a Forgero item (it's registered as a stone material).
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void bucketIsNotForgeroItem(TestContext context) {
		ItemStack bucket = new ItemStack(Items.BUCKET);
		ItemQueryApi query = ForgeroApi.itemQuery();

		boolean isForgero = query.isForgeroItem(bucket);

		context.assertTrue(!isForgero,
			"Bucket should NOT be recognized as a Forgero item");
		context.complete();
	}

	/**
	 * Non-Forgero item should return 0 for attributes.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void nonForgeroItemReturnsZeroAttributes(TestContext context) {
		ItemStack bucket = new ItemStack(Items.BUCKET);
		ItemQueryApi query = ForgeroApi.itemQuery();

		float damage = query.getAttackDamage(bucket);
		float speed = query.getMiningSpeed(bucket);
		int durability = query.getMaxDurability(bucket);

		context.assertTrue(damage == 0.0f,
			"Non-Forgero item should return 0 attack damage");
		context.assertTrue(speed == 0.0f,
			"Non-Forgero item should return 0 mining speed");
		context.assertTrue(durability == 0,
			"Non-Forgero item should return 0 durability");
		context.complete();
	}

	/**
	 * Non-Forgero item should NOT be customizable.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void nonForgeroItemNotCustomizable(TestContext context) {
		ItemStack bucket = new ItemStack(Items.BUCKET);
		ItemQueryApi query = ForgeroApi.itemQuery();

		boolean isCustomizable = query.isCustomizable(bucket);
		int slotCount = query.getUpgradeSlotCount(bucket);

		context.assertTrue(!isCustomizable,
			"Bucket should NOT be customizable");
		context.assertTrue(slotCount == 0,
			"Bucket should have 0 upgrade slots");
		context.complete();
	}
}
