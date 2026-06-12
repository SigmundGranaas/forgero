package com.sigmundgranaas.forgero.tools;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.common.api.ForgeroApi;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive integration tests for tool behaviors.
 *
 * These tests validate real in-game behavior:
 * - Durability and tool breaking
 * - Attack damage application to entities
 * - Mining level requirements (can/cannot harvest blocks)
 * - Tool vs vanilla comparisons
 *
 * Inspired by legacy fabric/forgero-fabric-core tests.
 */
public class ToolBehaviorIntegrationGametest implements ForgeroGameTest {

	// ========== Durability Tests ==========

	/**
	 * Tests that a normal Forgero tool breaks when durability is exhausted.
	 * Validates:
	 * - Tool is removed from hand when max damage is applied
	 * - Off-hand item is unaffected
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void normalToolBreaksWhenDurabilityExhausted(TestContext context) {
		ItemStack tool = getForgeroTool("pickaxe");
		if (tool == null) {
			context.complete();
			return;
		}

		ItemStack offHandTool = tool.copy();

		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		// Switch to survival for durability to matter
		player.changeGameMode(GameMode.SURVIVAL);
		player.setStackInHand(Hand.MAIN_HAND, tool);
		player.setStackInHand(Hand.OFF_HAND, offHandTool);

		// Damage tool to max
		int durability = tool.getMaxDamage();
		tool.damage(durability, player, p -> p.sendToolBreakStatus(Hand.MAIN_HAND));
		player.playerTick();

		// Main hand tool should be broken
		assertTrue(player.getStackInHand(Hand.MAIN_HAND).isEmpty(),
				"Tool should be destroyed when taking max damage");

		// Off hand should be unaffected
		assertFalse(player.getStackInHand(Hand.OFF_HAND).isDamaged(),
				"Off-hand tool should not be damaged");
		assertFalse(player.getStackInHand(Hand.OFF_HAND).isEmpty(),
				"Off-hand tool should not be empty");

		context.complete();
	}

	/**
	 * Tests that breaking off-hand tool doesn't affect main hand.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void offHandToolBreakDoesNotAffectMainHand(TestContext context) {
		ItemStack mainHandTool = getForgeroTool("sword");
		ItemStack offHandTool = getForgeroTool("sword");
		if (mainHandTool == null || offHandTool == null) {
			context.complete();
			return;
		}

		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.changeGameMode(GameMode.SURVIVAL);
		player.setStackInHand(Hand.MAIN_HAND, mainHandTool);
		player.setStackInHand(Hand.OFF_HAND, offHandTool);

		// Break off-hand tool
		int durability = offHandTool.getMaxDamage();
		offHandTool.damage(durability, player, p -> p.sendToolBreakStatus(Hand.OFF_HAND));
		player.playerTick();

		// Off hand should be broken
		assertTrue(player.getStackInHand(Hand.OFF_HAND).isEmpty(),
				"Off-hand tool should be destroyed");

		// Main hand should be unaffected
		assertFalse(player.getStackInHand(Hand.MAIN_HAND).isDamaged(),
				"Main-hand tool should not be damaged");
		assertFalse(player.getStackInHand(Hand.MAIN_HAND).isEmpty(),
				"Main-hand tool should not be empty");

		context.complete();
	}

	/**
	 * Tests that tools have reasonable durability values.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void toolsHaveReasonableDurability(TestContext context) {
		String[] toolTypes = {"pickaxe", "axe", "sword", "shovel", "hoe"};

		for (String type : toolTypes) {
			ItemStack tool = getForgeroTool(type);
			if (tool != null) {
				int durability = tool.getMaxDamage();
				assertTrue(durability > 0, type + " should have positive durability");
				assertTrue(durability < 100000, type + " durability should be reasonable");
			}
		}

		context.complete();
	}

	// ========== Attack Damage Tests ==========

	/**
	 * Tests that Forgero sword deals similar damage to vanilla diamond sword.
	 * The expected damage for a diamond sword is 7.0.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void forgeroSwordDealsDamageToEntity(TestContext context) {
		ItemStack forgeroSword = getForgeroToolByMaterial("diamond", "sword");
		if (forgeroSword == null) {
			context.complete();
			return;
		}

		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setStackInHand(Hand.MAIN_HAND, forgeroSword);

		// Tick player to update attributes and let the attack cooldown fully recharge
		// (a sword's cooldown period is ~12.5 ticks; attacking sooner deals reduced,
		// cooldown-scaled damage that drops out of the expected range).
		for (int i = 0; i < 20; i++) {
			player.playerTick();
		}

		LivingEntity target = context.spawnEntity(EntityType.PIG, new BlockPos(2, 1, 2));
		float initialHealth = target.getHealth();

		player.attack(target);

		context.waitAndRun(3, () -> {
			float damage = initialHealth - target.getHealth();
			assertTrue(damage > 0, "Forgero sword should deal damage, dealt " + damage);
			// Diamond sword deals ~7 damage
			assertTrue(damage >= 5 && damage <= 10,
					"Forgero diamond sword damage should be in expected range (5-10), was " + damage);
			context.complete();
		});
	}

	/**
	 * Tests that vanilla diamond sword deals expected damage.
	 * Baseline comparison for Forgero tools.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void vanillaDiamondSwordDealsDamage(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));

		// Tick player to update attributes and let the attack cooldown fully recharge
		// (a sword's cooldown period is ~12.5 ticks; attacking sooner deals reduced,
		// cooldown-scaled damage and the strict 7.0 ± 0.5 assertion fails).
		for (int i = 0; i < 20; i++) {
			player.playerTick();
		}

		LivingEntity target = context.spawnEntity(EntityType.PIG, new BlockPos(2, 1, 2));
		float initialHealth = target.getHealth();

		player.attack(target);

		context.waitAndRun(3, () -> {
			float damage = initialHealth - target.getHealth();
			assertTrue(damage > 0, "Vanilla sword should deal damage");
			// Expected: 7.0 damage
			assertEquals(7.0f, damage, 0.5f, "Vanilla diamond sword should deal ~7 damage");
			context.complete();
		});
	}

	/**
	 * Tests that player attack damage attribute is correctly updated when holding tool.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void playerAttackDamageUpdatesWithTool(TestContext context) {
		ItemStack tool = getForgeroToolByMaterial("diamond", "sword");
		if (tool == null) {
			context.complete();
			return;
		}

		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		// Check base damage
		double baseDamage = player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);

		// Equip tool
		player.setStackInHand(Hand.MAIN_HAND, tool);
		for (int i = 0; i < 10; i++) {
			player.playerTick();
		}

		double toolDamage = player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);

		assertTrue(toolDamage > baseDamage,
				"Attack damage should increase with tool: base=" + baseDamage + ", with tool=" + toolDamage);

		context.complete();
	}

	// ========== Mining Level Tests ==========

	/**
	 * Tests that iron pickaxe can harvest diamond ore.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void ironPickaxeCanHarvestDiamondOre(TestContext context) {
		BlockPos pos = new BlockPos(2, 1, 2);
		context.setBlockState(pos, Blocks.DIAMOND_ORE.getDefaultState());

		ItemStack ironPickaxe = getForgeroToolByMaterial("iron", "pickaxe");
		if (ironPickaxe == null) {
			// Fallback to vanilla
			ironPickaxe = new ItemStack(Items.IRON_PICKAXE);
		}

		boolean canHarvest = ironPickaxe.isSuitableFor(Blocks.DIAMOND_ORE.getDefaultState());

		context.waitAndRun(5, () -> {
			assertTrue(canHarvest, "Iron pickaxe should be able to harvest diamond ore");
			context.complete();
		});
	}

	/**
	 * Tests that wooden pickaxe cannot harvest diamond ore.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void woodenPickaxeCannotHarvestDiamondOre(TestContext context) {
		BlockPos pos = new BlockPos(2, 1, 2);
		context.setBlockState(pos, Blocks.DIAMOND_ORE.getDefaultState());

		ItemStack woodenPickaxe = getForgeroToolByMaterial("oak", "pickaxe");
		if (woodenPickaxe == null) {
			// Fallback to vanilla
			woodenPickaxe = new ItemStack(Items.WOODEN_PICKAXE);
		}

		boolean canHarvest = woodenPickaxe.isSuitableFor(Blocks.DIAMOND_ORE.getDefaultState());

		context.waitAndRun(5, () -> {
			assertFalse(canHarvest, "Wooden pickaxe should NOT be able to harvest diamond ore");
			context.complete();
		});
	}

	/**
	 * Tests that stone pickaxe can harvest iron ore but not diamond ore.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void stonePickaxeMiningLevelCorrect(TestContext context) {
		ItemStack stonePickaxe = getForgeroToolByMaterial("stone", "pickaxe");
		if (stonePickaxe == null) {
			stonePickaxe = new ItemStack(Items.STONE_PICKAXE);
		}

		boolean canHarvestIron = stonePickaxe.isSuitableFor(Blocks.IRON_ORE.getDefaultState());
		boolean canHarvestDiamond = stonePickaxe.isSuitableFor(Blocks.DIAMOND_ORE.getDefaultState());

		assertTrue(canHarvestIron, "Stone pickaxe should harvest iron ore");
		assertFalse(canHarvestDiamond, "Stone pickaxe should NOT harvest diamond ore");

		context.complete();
	}

	/**
	 * Tests that netherite pickaxe can harvest all ores.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void netheritePickaxeCanHarvestAllOres(TestContext context) {
		ItemStack netheritePickaxe = getForgeroToolByMaterial("netherite", "pickaxe");
		if (netheritePickaxe == null) {
			netheritePickaxe = new ItemStack(Items.NETHERITE_PICKAXE);
		}

		boolean canHarvestDiamond = netheritePickaxe.isSuitableFor(Blocks.DIAMOND_ORE.getDefaultState());
		boolean canHarvestObsidian = netheritePickaxe.isSuitableFor(Blocks.OBSIDIAN.getDefaultState());
		boolean canHarvestAncientDebris = netheritePickaxe.isSuitableFor(Blocks.ANCIENT_DEBRIS.getDefaultState());

		assertTrue(canHarvestDiamond, "Netherite pickaxe should harvest diamond ore");
		assertTrue(canHarvestObsidian, "Netherite pickaxe should harvest obsidian");
		assertTrue(canHarvestAncientDebris, "Netherite pickaxe should harvest ancient debris");

		context.complete();
	}

	// ========== Tool Suitability Tests ==========

	/**
	 * Tests that axe is suitable for wood blocks.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void axeIsSuitableForWood(TestContext context) {
		ItemStack axe = getForgeroToolByMaterial("iron", "axe");
		if (axe == null) {
			axe = new ItemStack(Items.IRON_AXE);
		}

		boolean suitable = axe.isSuitableFor(Blocks.OAK_LOG.getDefaultState());
		assertTrue(suitable, "Axe should be suitable for oak log");

		context.complete();
	}

	/**
	 * Tests that shovel is suitable for dirt/sand.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void shovelIsSuitableForDirt(TestContext context) {
		ItemStack shovel = getForgeroToolByMaterial("iron", "shovel");
		if (shovel == null) {
			shovel = new ItemStack(Items.IRON_SHOVEL);
		}

		boolean suitableDirt = shovel.isSuitableFor(Blocks.DIRT.getDefaultState());
		boolean suitableSand = shovel.isSuitableFor(Blocks.SAND.getDefaultState());

		assertTrue(suitableDirt, "Shovel should be suitable for dirt");
		assertTrue(suitableSand, "Shovel should be suitable for sand");

		context.complete();
	}

	/**
	 * Tests that hoe is suitable for hay/leaves.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void hoeIsSuitableForCrops(TestContext context) {
		ItemStack hoe = getForgeroToolByMaterial("iron", "hoe");
		if (hoe == null) {
			hoe = new ItemStack(Items.IRON_HOE);
		}

		boolean suitableLeaves = hoe.isSuitableFor(Blocks.OAK_LEAVES.getDefaultState());

		assertTrue(suitableLeaves, "Hoe should be suitable for leaves");

		context.complete();
	}

	// ========== Forgero vs Vanilla Comparison Tests ==========

	/**
	 * Tests that Forgero pickaxe mines stone at reasonable speed.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void forgeroPickaxeMinesStoneEfficiently(TestContext context) {
		ItemStack forgeroPick = getForgeroToolByMaterial("iron", "pickaxe");
		ItemStack vanillaPick = new ItemStack(Items.IRON_PICKAXE);

		if (forgeroPick == null) {
			context.complete();
			return;
		}

		float forgeroSpeed = forgeroPick.getMiningSpeedMultiplier(Blocks.STONE.getDefaultState());
		float vanillaSpeed = vanillaPick.getMiningSpeedMultiplier(Blocks.STONE.getDefaultState());

		assertTrue(forgeroSpeed > 1.0f, "Forgero pickaxe should mine stone faster than by hand");
		// Allow some variance but should be in same ballpark as vanilla
		assertTrue(Math.abs(forgeroSpeed - vanillaSpeed) < vanillaSpeed * 0.5f,
				"Forgero pickaxe speed should be similar to vanilla: forgero=" + forgeroSpeed + ", vanilla=" + vanillaSpeed);

		context.complete();
	}

	/**
	 * Tests that Forgero axe mines wood at reasonable speed.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void forgeroAxeMinesWoodEfficiently(TestContext context) {
		ItemStack forgeroAxe = getForgeroToolByMaterial("iron", "axe");
		ItemStack vanillaAxe = new ItemStack(Items.IRON_AXE);

		if (forgeroAxe == null) {
			context.complete();
			return;
		}

		float forgeroSpeed = forgeroAxe.getMiningSpeedMultiplier(Blocks.OAK_LOG.getDefaultState());
		float vanillaSpeed = vanillaAxe.getMiningSpeedMultiplier(Blocks.OAK_LOG.getDefaultState());

		assertTrue(forgeroSpeed > 1.0f, "Forgero axe should mine wood faster than by hand");
		assertTrue(Math.abs(forgeroSpeed - vanillaSpeed) < vanillaSpeed * 0.5f,
				"Forgero axe speed should be similar to vanilla");

		context.complete();
	}

	// ========== Tool Registration Tests ==========

	/**
	 * Tests that common Forgero tools are registered.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void commonForgeroToolsRegistered(TestContext context) {
		String[] expectedTools = {
				"forgero:iron-pickaxe",
				"forgero:iron-sword",
				"forgero:diamond-pickaxe",
				"forgero:diamond-sword"
		};

		for (String toolId : expectedTools) {
			var item = Registries.ITEM.get(new Identifier(toolId));
			assertNotEquals(Items.AIR, item,
					"Tool " + toolId + " should be registered");
		}

		context.complete();
	}

	// ========== Helper Methods ==========

	private ItemStack getForgeroTool(String toolType) {
		var registry = ForgeroApi.componentRegistry();
		var converter = ForgeroApi.converter();

		var toolOpt = registry.all().stream()
				.filter(c -> hasTag(c, toolType) && hasTag(c, "tool"))
				.findFirst();

		return toolOpt.flatMap(converter::toStack).orElse(null);
	}

	private ItemStack getForgeroToolByMaterial(String material, String toolType) {
		var registry = ForgeroApi.componentRegistry();
		var converter = ForgeroApi.converter();

		// Try exact match first
		String identifier = "forgero:" + material + "-" + toolType;
		var exactMatch = registry.get(OpenIdentifier.parse(identifier));
		if (exactMatch.isPresent()) {
			return converter.toStack(exactMatch.get()).orElse(null);
		}

		// Fallback to tag-based search
		var toolOpt = registry.all().stream()
				.filter(c -> hasTag(c, toolType) && hasTag(c, material))
				.findFirst();

		return toolOpt.flatMap(converter::toStack).orElse(null);
	}

	private boolean hasTag(Component c, String tagPart) {
		return c.getTags().stream()
				.anyMatch(tag -> tag.toString().toLowerCase().contains(tagPart.toLowerCase()));
	}
}
