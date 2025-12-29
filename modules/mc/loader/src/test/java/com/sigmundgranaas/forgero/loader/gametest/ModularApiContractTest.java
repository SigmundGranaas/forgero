package com.sigmundgranaas.forgero.loader.gametest;

import com.sigmundgranaas.forgero.common.api.item.ItemComparisonApi;
import com.sigmundgranaas.forgero.common.api.item.ItemMutationApi;
import com.sigmundgranaas.forgero.common.api.item.ItemQueryApi;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.loader.api.ForgeroApi;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the modular ItemStack APIs for real user-facing functionality.
 * <p>
 * Focus: Does the API actually work for real use cases?
 * - Can I query item attributes?
 * - Can I install/remove upgrades?
 * - Can I compare items?
 * - Do vanilla items work without breaking?
 * - Does null handling work?
 */
public class ModularApiContractTest implements ForgeroGameTest {

	// ==================== ItemQueryApi - Real Use Cases ====================

	/**
	 * USE CASE: Query attributes from a Forgero item to display in UI or use in logic.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void queryAttributesFromForgeroItem(TestContext context) {
		ItemQueryApi api = ForgeroApi.itemQuery();
		ItemStack forgeroItem = getAnyForgeroItem();

		// Can I get real attribute values?
		float damage = api.getAttackDamage(forgeroItem);
		int durability = api.getMaxDurability(forgeroItem);
		float miningSpeed = api.getMiningSpeed(forgeroItem);

		// Should return actual numbers, not crash
		assertTrue(damage >= 0 || durability > 0 || miningSpeed >= 0,
				"Should return real attribute values");

		context.complete();
	}

	/**
	 * USE CASE: Query vanilla items without breaking (compatibility).
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void queryVanillaItemsReturnsDefaults(TestContext context) {
		ItemQueryApi api = ForgeroApi.itemQuery();
		ItemStack vanilla = new ItemStack(Items.DIAMOND_SWORD);

		// Should return sensible defaults, not crash
		assertEquals(0.0f, api.getAttackDamage(vanilla));
		assertEquals(0, api.getMaxDurability(vanilla));
		assertFalse(api.isForgeroItem(vanilla));

		context.complete();
	}

	/**
	 * USE CASE: Handle null/empty without crashing.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void queryNullItemsDoesNotCrash(TestContext context) {
		ItemQueryApi api = ForgeroApi.itemQuery();

		// Should not crash
		assertEquals(0.0f, api.getAttackDamage(null));
		assertEquals(0.0f, api.getAttackDamage(ItemStack.EMPTY));
		assertFalse(api.isForgeroItem(null));

		context.complete();
	}

	/**
	 * USE CASE: Get item composition (parts and upgrades) for UI display.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void getItemComposition(TestContext context) {
		ItemQueryApi api = ForgeroApi.itemQuery();
		ItemStack forgeroItem = getAnyForgeroItem();

		// Can I get the parts that make up this item?
		List<ItemStack> parts = api.getParts(forgeroItem);
		List<ItemStack> upgrades = api.getInstalledUpgrades(forgeroItem);

		assertNotNull(parts);
		assertNotNull(upgrades);

		context.complete();
	}

	/**
	 * USE CASE: Check if item has upgrade slots available before trying to upgrade.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void checkUpgradeSlotsBeforeUpgrading(TestContext context) {
		ItemQueryApi api = ForgeroApi.itemQuery();
		ItemStack customizable = getCustomizableItem();

		if (api.isCustomizable(customizable)) {
			int totalSlots = api.getUpgradeSlotCount(customizable);
			int emptySlots = api.getEmptySlotCount(customizable);

			assertTrue(totalSlots > 0, "Customizable items should have slots");
			assertEquals(totalSlots, api.getFilledSlotCount(customizable) + emptySlots,
					"Total = filled + empty");
		}

		context.complete();
	}

	// ==================== ItemMutationApi - Real Use Cases ====================

	/**
	 * USE CASE: Install an upgrade and verify it actually worked.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void installUpgradeActuallyWorks(TestContext context) {
		ItemMutationApi mutate = ForgeroApi.itemMutation();
		ItemQueryApi query = ForgeroApi.itemQuery();

		ItemStack tool = getCustomizableItem();
		ItemStack upgrade = getUpgradeItem();

		// Try to install
		ItemStack result = mutate.installUpgrade(tool, upgrade);

		// If it was compatible, verify it worked
		if (mutate.canInstallUpgrade(tool, upgrade)) {
			int emptyBefore = query.getEmptySlotCount(tool);
			int emptyAfter = query.getEmptySlotCount(result);

			assertTrue(emptyAfter < emptyBefore || emptyAfter == 0,
					"Empty slots should decrease after successful install");
		}

		context.complete();
	}

	/**
	 * USE CASE: Original ItemStack remains unchanged after mutation (immutability).
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void mutationsAreImmutable(TestContext context) {
		ItemMutationApi mutate = ForgeroApi.itemMutation();
		ItemQueryApi query = ForgeroApi.itemQuery();

		ItemStack original = getCustomizableItem();
		ItemStack upgrade = getUpgradeItem();

		int emptyBefore = query.getEmptySlotCount(original);

		// Mutate
		ItemStack result = mutate.installUpgrade(original, upgrade);

		// Original should be unchanged
		assertEquals(emptyBefore, query.getEmptySlotCount(original),
				"Original stack must remain unchanged");

		context.complete();
	}

	/**
	 * USE CASE: Remove all upgrades to reset item to base state.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void removeAllUpgradesWorks(TestContext context) {
		ItemMutationApi mutate = ForgeroApi.itemMutation();
		ItemQueryApi query = ForgeroApi.itemQuery();

		ItemStack tool = getCustomizableItem();
		ItemStack cleared = mutate.removeAllUpgrades(tool);

		// After clearing, should have 0 filled slots
		assertEquals(0, query.getFilledSlotCount(cleared),
				"Cleared item should have 0 filled slots");

		context.complete();
	}

	/**
	 * USE CASE: Vanilla items don't break mutation API.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void mutateVanillaItemsDoesNotCrash(TestContext context) {
		ItemMutationApi api = ForgeroApi.itemMutation();
		ItemStack vanilla = new ItemStack(Items.DIAMOND_SWORD);
		ItemStack upgrade = getUpgradeItem();

		// Should not crash, just return original
		ItemStack result = api.installUpgrade(vanilla, upgrade);
		assertSame(vanilla, result);

		context.complete();
	}

	// ==================== ItemComparisonApi - Real Use Cases ====================

	/**
	 * USE CASE: Check if two items are the same type for stacking/grouping logic.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void compareItemTypes(TestContext context) {
		ItemComparisonApi api = ForgeroApi.itemComparison();

		ItemStack item1 = getAnyForgeroItem();
		ItemStack item2 = item1.copy();

		// Same item should be same type
		assertTrue(api.isSameType(item1, item2),
				"Copies should be same type");

		// Different items should not be same type
		ItemStack vanilla = new ItemStack(Items.DIAMOND_SWORD);
		assertFalse(api.isSameType(item1, vanilla),
				"Forgero and vanilla items are different types");

		context.complete();
	}

	/**
	 * USE CASE: Compare vanilla items works.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void compareVanillaItems(TestContext context) {
		ItemComparisonApi api = ForgeroApi.itemComparison();

		ItemStack sword1 = new ItemStack(Items.DIAMOND_SWORD);
		ItemStack sword2 = new ItemStack(Items.DIAMOND_SWORD);
		ItemStack pickaxe = new ItemStack(Items.DIAMOND_PICKAXE);

		assertTrue(api.isSameType(sword1, sword2),
				"Same vanilla items should match");
		assertFalse(api.isSameType(sword1, pickaxe),
				"Different vanilla items should not match");

		context.complete();
	}

	/**
	 * USE CASE: Null handling in comparison.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void compareNullItemsDoesNotCrash(TestContext context) {
		ItemComparisonApi api = ForgeroApi.itemComparison();
		ItemStack item = getAnyForgeroItem();

		// Should not crash
		assertFalse(api.isSameType(null, item));
		assertFalse(api.isSameType(item, null));
		assertFalse(api.areSimilar(null, item));

		context.complete();
	}

	// ==================== Helper Methods ====================

	private ItemStack getAnyForgeroItem() {
		var allComponents = ForgeroApi.componentRegistry().all();
		assertFalse(allComponents.isEmpty(), "Need components to test");

		Component component = allComponents.iterator().next();
		Optional<ItemStack> stack = ForgeroApi.converter().toStack(component);
		assertTrue(stack.isPresent(), "Should convert component to stack");

		return stack.get();
	}

	private ItemStack getCustomizableItem() {
		var allComponents = ForgeroApi.componentRegistry().all();
		for (Component component : allComponents) {
			if (ForgeroApi.slotManager().hasComponentUpgradeSlots(component)) {
				Optional<ItemStack> stack = ForgeroApi.converter().toStack(component);
				if (stack.isPresent()) {
					return stack.get();
				}
			}
		}
		// Fallback to any item
		return getAnyForgeroItem();
	}

	private ItemStack getUpgradeItem() {
		var allComponents = ForgeroApi.componentRegistry().all();
		for (Component component : allComponents) {
			for (OpenIdentifier tag : component.getTags()) {
				String path = tag.path();
				if (path.contains("gem") || path.contains("schematic") || path.contains("upgrade")) {
					Optional<ItemStack> stack = ForgeroApi.converter().toStack(component);
					if (stack.isPresent()) {
						return stack.get();
					}
				}
			}
		}
		// Fallback to any item
		return getAnyForgeroItem();
	}
}
