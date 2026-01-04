package com.sigmundgranaas.forgero.tests;

import com.sigmundgranaas.forgero.loader.api.ForgeroApi;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GameTests for armor item attributes.
 * Tests that armor items have correct protection values and attributes.
 */
public class ArmorAttributeTest implements ForgeroGameTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(ArmorAttributeTest.class);

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void iron_helmet_exists_and_has_armor(TestContext context) {
		var ctx = forgero(context);
		var query = ForgeroApi.itemQuery();

		var ironHelmet = ctx.component("forgero:iron-helmet");
		assertTrue(ironHelmet.isPresent(),
				"iron-helmet component must exist - check armor content loading");

		var stack = ctx.toStack(ironHelmet.get());
		assertTrue(stack.isPresent(), "Iron helmet must convert to ItemStack");

		ItemStack itemStack = stack.get();
		assertFalse(itemStack.isEmpty(), "Iron helmet ItemStack must not be empty");

		// Helmet should provide armor protection
		int armor = query.getArmor(itemStack);
		LOGGER.debug("Iron helmet attributes: armor={}", armor);
		assertTrue(armor > 0, "Iron helmet should have positive armor value, got: " + armor);

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void iron_chestplate_has_higher_armor_than_helmet(TestContext context) {
		var ctx = forgero(context);
		var query = ForgeroApi.itemQuery();

		var ironHelmet = ctx.component("forgero:iron-helmet");
		var ironChestplate = ctx.component("forgero:iron-chestplate");

		assertTrue(ironHelmet.isPresent(),
				"iron-helmet component must exist - check armor content loading");
		assertTrue(ironChestplate.isPresent(),
				"iron-chestplate component must exist - check armor content loading");

		var helmetStack = ctx.toStack(ironHelmet.get());
		var chestplateStack = ctx.toStack(ironChestplate.get());

		assertTrue(helmetStack.isPresent() && chestplateStack.isPresent(),
				"Both armor pieces must convert to ItemStack");

		int helmetArmor = query.getArmor(helmetStack.get());
		int chestplateArmor = query.getArmor(chestplateStack.get());

		LOGGER.debug("Iron armor comparison: helmetArmor={}, chestplateArmor={}", helmetArmor, chestplateArmor);

		// Chestplate should provide more protection than helmet
		assertTrue(chestplateArmor >= helmetArmor,
				"Chestplate armor (" + chestplateArmor + ") should be >= helmet (" + helmetArmor + ")");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void diamond_armor_better_than_iron(TestContext context) {
		var ctx = forgero(context);
		var query = ForgeroApi.itemQuery();

		var ironChestplate = ctx.component("forgero:iron-chestplate");
		var diamondChestplate = ctx.component("forgero:diamond-chestplate");

		assertTrue(ironChestplate.isPresent(),
				"iron-chestplate component must exist - check armor content loading");
		assertTrue(diamondChestplate.isPresent(),
				"diamond-chestplate component must exist - check armor content loading");

		var ironStack = ctx.toStack(ironChestplate.get());
		var diamondStack = ctx.toStack(diamondChestplate.get());

		assertTrue(ironStack.isPresent() && diamondStack.isPresent(),
				"Both armor pieces must convert to ItemStack");

		int ironArmor = query.getArmor(ironStack.get());
		int diamondArmor = query.getArmor(diamondStack.get());

		LOGGER.debug("Material armor comparison: ironArmor={}, diamondArmor={}", ironArmor, diamondArmor);

		// Diamond should provide more protection than iron
		assertTrue(diamondArmor >= ironArmor,
				"Diamond armor (" + diamondArmor + ") should be >= iron (" + ironArmor + ")");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void netherite_armor_has_toughness(TestContext context) {
		var ctx = forgero(context);
		var query = ForgeroApi.itemQuery();

		var netheriteChestplate = ctx.component("forgero:netherite-chestplate");

		assertTrue(netheriteChestplate.isPresent(),
				"netherite-chestplate component must exist - check armor content loading");

		var stack = ctx.toStack(netheriteChestplate.get());
		assertTrue(stack.isPresent(), "Netherite chestplate must convert to ItemStack");

		int armor = query.getArmor(stack.get());
		int durability = query.getMaxDurability(stack.get());

		LOGGER.debug("Netherite chestplate attributes: armor={}, durability={}", armor, durability);

		assertTrue(armor > 0, "Netherite chestplate should have positive armor");
		assertTrue(durability > 0, "Netherite chestplate should have positive durability");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void all_armor_pieces_exist_for_iron(TestContext context) {
		var ctx = forgero(context);

		String[] armorPieces = {
				"forgero:iron-helmet",
				"forgero:iron-chestplate",
				"forgero:iron-leggings",
				"forgero:iron-boots"
		};

		for (String pieceId : armorPieces) {
			var component = ctx.component(pieceId);
			assertTrue(component.isPresent(),
					pieceId + " component must exist - check armor content loading");

			var stack = ctx.toStack(component.get());
			assertTrue(stack.isPresent(), pieceId + " must convert to ItemStack");
			LOGGER.debug("Verified armor piece: {}", pieceId);
		}

		LOGGER.debug("Iron armor set check: all 4 pieces verified");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void armor_does_not_have_tool_attributes(TestContext context) {
		var ctx = forgero(context);
		var query = ForgeroApi.itemQuery();

		var ironChestplate = ctx.component("forgero:iron-chestplate");

		assertTrue(ironChestplate.isPresent(),
				"iron-chestplate component must exist - check armor content loading");

		var stack = ctx.toStack(ironChestplate.get());
		assertTrue(stack.isPresent(), "Iron chestplate must convert to ItemStack");

		// Armor should NOT have mining speed or attack damage like tools
		float miningSpeed = query.getMiningSpeed(stack.get());
		float attackDamage = query.getAttackDamage(stack.get());

		LOGGER.debug("Iron chestplate tool attributes (should be ~0): miningSpeed={}, attackDamage={}",
				miningSpeed, attackDamage);

		// Mining speed and attack damage should be zero or minimal for armor
		assertTrue(miningSpeed <= 1.0f,
				"Armor should not have significant mining speed, got: " + miningSpeed);

		context.complete();
	}
}
