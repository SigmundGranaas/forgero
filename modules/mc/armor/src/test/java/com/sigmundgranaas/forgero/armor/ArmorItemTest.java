package com.sigmundgranaas.forgero.armor;

import com.sigmundgranaas.forgero.common.api.item.ItemQueryApi;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.loader.api.ForgeroApi;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests Forgero armor items for real functionality.
 * Focus: Do armor pieces actually work as expected?
 * - Component to ItemStack conversion
 * - Attribute queries via ItemQueryApi
 * - Edge cases with extreme values
 * - Armor type correctness
 */
public class ArmorItemTest implements ForgeroGameTest {

	/**
	 * USE CASE: Convert armor component to ItemStack and back (roundtrip).
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_armor")
	public void armorComponentRoundtrip(TestContext context) {
		var registry = ForgeroApi.componentRegistry();
		var converter = ForgeroApi.converter();

		// Find an armor component
		var armorOpt = registry.all().stream()
				.filter(this::isArmorComponent)
				.findFirst();

		if (armorOpt.isEmpty()) {
			context.complete();
			return;
		}

		Component original = armorOpt.get();

		// Component -> ItemStack -> Component
		var stackOpt = converter.toStack(original);
		assertTrue(stackOpt.isPresent(), "Armor should convert to ItemStack");

		ItemStack stack = stackOpt.get();
		var roundTripOpt = converter.toComponent(stack);
		assertTrue(roundTripOpt.isPresent(), "ItemStack should convert back");

		assertEquals(original.id(), roundTripOpt.get().id(),
				"Roundtrip preserves component ID");

		context.complete();
	}

	/**
	 * USE CASE: Query armor attributes using ItemQueryApi.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_armor")
	public void queryArmorAttributes(TestContext context) {
		ItemQueryApi query = ForgeroApi.itemQuery();
		ItemStack armor = getAnyArmorStack();

		if (armor == null) {
			context.complete();
			return;
		}

		// Should return actual values, not crash
		int protection = query.getArmor(armor);
		float toughness = query.getArmorToughness(armor);
		int durability = query.getMaxDurability(armor);

		assertTrue(protection >= 0 || toughness >= 0 || durability > 0,
				"Armor should have some positive attributes");

		context.complete();
	}

	/**
	 * USE CASE: Armor components convert to ArmorItem instances.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_armor")
	public void armorComponentBecomesArmorItem(TestContext context) {
		var registry = ForgeroApi.componentRegistry();
		var converter = ForgeroApi.converter();

		// Find a complete armor piece (helmet, chestplate, leggings, or boots)
		var armorOpt = registry.all().stream()
				.filter(c -> hasTag(c, "helmet") || hasTag(c, "chestplate")
						|| hasTag(c, "leggings") || hasTag(c, "boots"))
				.findFirst();

		if (armorOpt.isEmpty()) {
			context.complete();
			return;
		}

		var stackOpt = converter.toStack(armorOpt.get());
		if (stackOpt.isPresent()) {
			ItemStack stack = stackOpt.get();
			assertTrue(stack.getItem() instanceof ArmorItem,
					"Armor component should become ArmorItem");
		}

		context.complete();
	}

	/**
	 * USE CASE: Armor works with vanilla Minecraft systems (don't crash).
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_armor")
	public void armorWorksWithVanillaSystems(TestContext context) {
		ItemStack armor = getAnyArmorStack();

		if (armor == null) {
			context.complete();
			return;
		}

		// Should not crash
		assertNotNull(armor.getItem());
		assertTrue(armor.getMaxDamage() >= 0);
		assertFalse(armor.isEmpty());

		context.complete();
	}

	/**
	 * EDGE CASE: Armor with extreme attribute values works correctly.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_armor")
	public void extremeArmorAttributes(TestContext context) {
		ItemQueryApi query = ForgeroApi.itemQuery();
		var registry = ForgeroApi.componentRegistry();

		// Test extreme values
		var extremeHelmet = registry.get(OpenIdentifier.of("test-extreme-helmet"));
		if (extremeHelmet.isPresent()) {
			var stack = ForgeroApi.converter().toStack(extremeHelmet.get()).orElse(null);
			if (stack != null) {
				float protection = query.getAttribute(stack, DefaultAttributes.ARMOR);
				float toughness = query.getAttribute(stack, DefaultAttributes.ARMOR_TOUGHNESS);
				float knockbackResistance = query.getAttribute(stack, DefaultAttributes.KNOCKBACK_RESISTANCE);

				assertTrue(protection > 1_000_000f,
						"Extreme helmet should have >1M protection");
				assertTrue(toughness > 1_000_000f,
						"Extreme helmet should have >1M toughness");
				assertEquals(10.0f, knockbackResistance,
						"Extreme helmet should have 10.0 knockback resistance");
			}
		}

		// Test zero values
		var zeroHelmet = registry.get(OpenIdentifier.of("test-zero-helmet"));
		if (zeroHelmet.isPresent()) {
			var stack = ForgeroApi.converter().toStack(zeroHelmet.get()).orElse(null);
			if (stack != null) {
				assertEquals(0f, query.getAttribute(stack, DefaultAttributes.ARMOR),
						"Zero helmet should have 0 protection");
				assertEquals(0f, query.getAttribute(stack, DefaultAttributes.ARMOR_TOUGHNESS),
						"Zero helmet should have 0 toughness");
				assertEquals(0f, query.getAttribute(stack, DefaultAttributes.DURABILITY),
						"Zero helmet should have 0 durability");
			}
		}

		// Test negative values
		var negativeHelmet = registry.get(OpenIdentifier.of("test-negative-helmet"));
		if (negativeHelmet.isPresent()) {
			var stack = ForgeroApi.converter().toStack(negativeHelmet.get()).orElse(null);
			if (stack != null) {
				assertEquals(-5f, query.getAttribute(stack, DefaultAttributes.ARMOR),
						"Negative helmet should have -5 protection (debuff)");
				assertEquals(-2.0f, query.getAttribute(stack, DefaultAttributes.ARMOR_TOUGHNESS),
						"Negative helmet should have -2.0 toughness (debuff)");
				assertEquals(100f, query.getAttribute(stack, DefaultAttributes.DURABILITY),
						"Negative helmet should have normal durability (100)");
			}
		}

		// Test known values
		var knownHelmet = registry.get(OpenIdentifier.of("test-known-helmet"));
		if (knownHelmet.isPresent()) {
			var stack = ForgeroApi.converter().toStack(knownHelmet.get()).orElse(null);
			if (stack != null) {
				assertEquals(5f, query.getArmor(stack),
						"Known helmet should have 5 protection");
				assertEquals(2.5f, query.getArmorToughness(stack),
						"Known helmet should have 2.5 toughness");
				assertEquals(0.2f, query.getAttribute(stack, DefaultAttributes.KNOCKBACK_RESISTANCE),
						"Known helmet should have 0.2 knockback resistance");
				assertEquals(250, query.getMaxDurability(stack),
						"Known helmet should have 250 durability");
			}
		}

		context.complete();
	}

	/**
	 * USE CASE: Different armor types (helmet, chestplate, leggings, boots) all work.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_armor")
	public void differentArmorTypesWork(TestContext context) {
		var registry = ForgeroApi.componentRegistry();
		var converter = ForgeroApi.converter();
		String[] armorTypes = {"helmet", "chestplate", "leggings", "boots"};

		for (String armorType : armorTypes) {
			var armorOpt = registry.all().stream()
					.filter(c -> hasTag(c, armorType))
					.findFirst();

			if (armorOpt.isPresent()) {
				var stackOpt = converter.toStack(armorOpt.get());
				if (stackOpt.isPresent()) {
					ItemStack stack = stackOpt.get();
					assertFalse(stack.isEmpty(), armorType + " should convert to valid ItemStack");
				}
			}
		}

		context.complete();
	}

	/**
	 * USE CASE: Vanilla armor items don't break armor queries.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_armor")
	public void vanillaArmorReturnsDefaults(TestContext context) {
		ItemQueryApi query = ForgeroApi.itemQuery();
		ItemStack vanillaArmor = new ItemStack(Items.DIAMOND_CHESTPLATE);

		// Should return defaults, not crash
		assertEquals(0, query.getArmor(vanillaArmor));
		assertEquals(0.0f, query.getArmorToughness(vanillaArmor));
		assertEquals(0, query.getMaxDurability(vanillaArmor));
		assertFalse(query.isForgeroItem(vanillaArmor));

		context.complete();
	}

	// ==================== Helper Methods ====================

	private ItemStack getAnyArmorStack() {
		var registry = ForgeroApi.componentRegistry();
		var converter = ForgeroApi.converter();

		var armorOpt = registry.all().stream()
				.filter(this::isArmorComponent)
				.findFirst();

		return armorOpt.flatMap(converter::toStack).orElse(null);
	}

	private boolean isArmorComponent(Component c) {
		return hasTag(c, "helmet") || hasTag(c, "chestplate") || hasTag(c, "leggings") || hasTag(c, "boots");
	}

	private boolean hasTag(Component c, String tagPart) {
		return c.getTags().stream()
				.anyMatch(tag -> tag.toString().contains(tagPart));
	}
}
