package com.sigmundgranaas.vanillaupgrades.tests;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

/**
 * Real gameplay validation tests for Vanilla Upgrades.
 *
 * These tests verify ACTUAL GAMEPLAY BEHAVIOR - not just that things convert to components.
 * If core values change (attack damage, armor values, mining speed, etc.), these tests WILL FAIL.
 *
 * Test categories:
 * 1. Weapon damage - hitting entities reduces their health by expected amounts
 * 2. Armor protection - armor attribute values match vanilla
 * 3. Tool durability - durability values match vanilla exactly
 * 4. Mining speed - tools mine blocks at expected speeds
 * 5. Mining level - tools can/cannot harvest appropriate blocks
 */
public class VanillaItemGameplayTest implements FabricGameTest {

	// Attack cooldown: Swords = 12.5 ticks (1.6 speed), Axes = 20 ticks (1.0 speed)
	// Need at least 25 ticks to ensure ALL weapons have full cooldown
	private static final int ATTACK_COOLDOWN_TICKS = 25;

	// =============================================
	// SWORD DAMAGE TESTS
	// =============================================

	/**
	 * Diamond sword should deal exactly 7 damage to entities.
	 * Vanilla base damage = 7 (4 base + 3 from material)
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void diamondSwordDealsSevenDamage(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));

		// Wait for full attack cooldown (sword: 1.6 speed = 12.5 ticks)
		for (int i = 0; i < ATTACK_COOLDOWN_TICKS; i++) {
			player.playerTick();
		}

		LivingEntity target = context.spawnEntity(EntityType.COW, new BlockPos(2, 1, 2));
		float initialHealth = target.getHealth();

		player.attack(target);

		context.waitAndRun(1, () -> {
			float damage = initialHealth - target.getHealth();
			context.assertTrue(damage >= 6.5f && damage <= 7.5f,
				"Diamond sword should deal ~7 damage, but dealt " + damage);
			context.complete();
		});
	}

	/**
	 * Iron sword should deal exactly 6 damage to entities.
	 * Vanilla base damage = 6 (4 base + 2 from material)
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void ironSwordDealsSixDamage(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));

		for (int i = 0; i < ATTACK_COOLDOWN_TICKS; i++) {
			player.playerTick();
		}

		LivingEntity target = context.spawnEntity(EntityType.COW, new BlockPos(2, 1, 2));
		float initialHealth = target.getHealth();

		player.attack(target);

		context.waitAndRun(1, () -> {
			float damage = initialHealth - target.getHealth();
			context.assertTrue(damage >= 5.5f && damage <= 6.5f,
				"Iron sword should deal ~6 damage, but dealt " + damage);
			context.complete();
		});
	}

	/**
	 * Netherite sword should deal exactly 8 damage to entities.
	 * Vanilla base damage = 8 (4 base + 4 from material)
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void netheriteSwordDealsEightDamage(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.NETHERITE_SWORD));

		for (int i = 0; i < ATTACK_COOLDOWN_TICKS; i++) {
			player.playerTick();
		}

		LivingEntity target = context.spawnEntity(EntityType.COW, new BlockPos(2, 1, 2));
		float initialHealth = target.getHealth();

		player.attack(target);

		context.waitAndRun(1, () -> {
			float damage = initialHealth - target.getHealth();
			context.assertTrue(damage >= 7.5f && damage <= 8.5f,
				"Netherite sword should deal ~8 damage, but dealt " + damage);
			context.complete();
		});
	}

	/**
	 * Wooden sword should deal exactly 4 damage to entities.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void woodenSwordDealsFourDamage(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.WOODEN_SWORD));

		for (int i = 0; i < ATTACK_COOLDOWN_TICKS; i++) {
			player.playerTick();
		}

		LivingEntity target = context.spawnEntity(EntityType.COW, new BlockPos(2, 1, 2));
		float initialHealth = target.getHealth();

		player.attack(target);

		context.waitAndRun(1, () -> {
			float damage = initialHealth - target.getHealth();
			context.assertTrue(damage >= 3.5f && damage <= 4.5f,
				"Wooden sword should deal ~4 damage, but dealt " + damage);
			context.complete();
		});
	}

	// =============================================
	// AXE DAMAGE TESTS
	// =============================================

	/**
	 * Diamond axe should deal exactly 9 damage to entities.
	 * Vanilla base damage = 9 (1 base + 8 from diamond axe)
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void diamondAxeDealsNineDamage(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.DIAMOND_AXE));

		// Axes have slower attack speed (1.0), need full 20+ ticks
		for (int i = 0; i < ATTACK_COOLDOWN_TICKS; i++) {
			player.playerTick();
		}

		LivingEntity target = context.spawnEntity(EntityType.COW, new BlockPos(2, 1, 2));
		float initialHealth = target.getHealth();

		player.attack(target);

		context.waitAndRun(1, () -> {
			float damage = initialHealth - target.getHealth();
			context.assertTrue(damage >= 8.5f && damage <= 9.5f,
				"Diamond axe should deal ~9 damage, but dealt " + damage);
			context.complete();
		});
	}

	/**
	 * Netherite axe should deal exactly 10 damage to entities.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void netheriteAxeDealsTenDamage(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.NETHERITE_AXE));

		for (int i = 0; i < ATTACK_COOLDOWN_TICKS; i++) {
			player.playerTick();
		}

		LivingEntity target = context.spawnEntity(EntityType.COW, new BlockPos(2, 1, 2));
		float initialHealth = target.getHealth();

		player.attack(target);

		context.waitAndRun(1, () -> {
			float damage = initialHealth - target.getHealth();
			context.assertTrue(damage >= 9.5f && damage <= 10.5f,
				"Netherite axe should deal ~10 damage, but dealt " + damage);
			context.complete();
		});
	}

	/**
	 * Iron axe should deal exactly 9 damage to entities.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void ironAxeDealsNineDamage(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.IRON_AXE));

		for (int i = 0; i < ATTACK_COOLDOWN_TICKS; i++) {
			player.playerTick();
		}

		LivingEntity target = context.spawnEntity(EntityType.COW, new BlockPos(2, 1, 2));
		float initialHealth = target.getHealth();

		player.attack(target);

		context.waitAndRun(1, () -> {
			float damage = initialHealth - target.getHealth();
			context.assertTrue(damage >= 8.5f && damage <= 9.5f,
				"Iron axe should deal ~9 damage, but dealt " + damage);
			context.complete();
		});
	}

	// =============================================
	// TOOL DURABILITY TESTS
	// =============================================

	/**
	 * Diamond pickaxe should have exactly 1561 durability.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void diamondPickaxeHasCorrectDurability(TestContext context) {
		ItemStack pickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
		int durability = pickaxe.getMaxDamage();

		context.assertTrue(durability == 1561,
			"Diamond pickaxe should have 1561 durability, but has " + durability);
		context.complete();
	}

	/**
	 * Netherite pickaxe should have exactly 2031 durability.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void netheritePickaxeHasCorrectDurability(TestContext context) {
		ItemStack pickaxe = new ItemStack(Items.NETHERITE_PICKAXE);
		int durability = pickaxe.getMaxDamage();

		context.assertTrue(durability == 2031,
			"Netherite pickaxe should have 2031 durability, but has " + durability);
		context.complete();
	}

	/**
	 * Iron sword should have exactly 250 durability.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void ironSwordHasCorrectDurability(TestContext context) {
		ItemStack sword = new ItemStack(Items.IRON_SWORD);
		int durability = sword.getMaxDamage();

		context.assertTrue(durability == 250,
			"Iron sword should have 250 durability, but has " + durability);
		context.complete();
	}

	/**
	 * Tool should break when durability is exhausted.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void toolBreaksWhenDurabilityExhausted(TestContext context) {
		ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE);
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.changeGameMode(GameMode.SURVIVAL);
		player.setStackInHand(Hand.MAIN_HAND, pickaxe);

		// Damage to max
		int maxDamage = pickaxe.getMaxDamage();
		pickaxe.damage(maxDamage, player, p -> p.sendToolBreakStatus(Hand.MAIN_HAND));
		player.playerTick();

		context.assertTrue(player.getStackInHand(Hand.MAIN_HAND).isEmpty(),
			"Tool should break when durability is exhausted");
		context.complete();
	}

	// =============================================
	// ARMOR DURABILITY TESTS
	// =============================================

	/**
	 * Diamond chestplate should have exactly 528 durability.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void diamondChestplateHasCorrectDurability(TestContext context) {
		ItemStack chestplate = new ItemStack(Items.DIAMOND_CHESTPLATE);
		int durability = chestplate.getMaxDamage();

		context.assertTrue(durability == 528,
			"Diamond chestplate should have 528 durability, but has " + durability);
		context.complete();
	}

	/**
	 * Iron helmet should have exactly 165 durability.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void ironHelmetHasCorrectDurability(TestContext context) {
		ItemStack helmet = new ItemStack(Items.IRON_HELMET);
		int durability = helmet.getMaxDamage();

		context.assertTrue(durability == 165,
			"Iron helmet should have 165 durability, but has " + durability);
		context.complete();
	}

	/**
	 * Netherite leggings should have exactly 555 durability.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void netheriteLeggingsHasCorrectDurability(TestContext context) {
		ItemStack leggings = new ItemStack(Items.NETHERITE_LEGGINGS);
		int durability = leggings.getMaxDamage();

		context.assertTrue(durability == 555,
			"Netherite leggings should have 555 durability, but has " + durability);
		context.complete();
	}

	/**
	 * Golden boots should have exactly 91 durability.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void goldenBootsHasCorrectDurability(TestContext context) {
		ItemStack boots = new ItemStack(Items.GOLDEN_BOOTS);
		int durability = boots.getMaxDamage();

		context.assertTrue(durability == 91,
			"Golden boots should have 91 durability, but has " + durability);
		context.complete();
	}

	// =============================================
	// ARMOR PROTECTION VALUE TESTS
	// =============================================

	/**
	 * Diamond chestplate should provide 8 armor points.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void diamondChestplateHasCorrectArmor(TestContext context) {
		ItemStack chestplate = new ItemStack(Items.DIAMOND_CHESTPLATE);
		ArmorItem armorItem = (ArmorItem) chestplate.getItem();
		int protection = armorItem.getProtection();

		context.assertTrue(protection == 8,
			"Diamond chestplate should provide 8 armor, but provides " + protection);
		context.complete();
	}

	/**
	 * Netherite chestplate should provide 8 armor points.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void netheriteChestplateHasCorrectArmor(TestContext context) {
		ItemStack chestplate = new ItemStack(Items.NETHERITE_CHESTPLATE);
		ArmorItem armorItem = (ArmorItem) chestplate.getItem();
		int protection = armorItem.getProtection();

		context.assertTrue(protection == 8,
			"Netherite chestplate should provide 8 armor, but provides " + protection);
		context.complete();
	}

	/**
	 * Iron helmet should provide 2 armor points.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void ironHelmetHasCorrectArmor(TestContext context) {
		ItemStack helmet = new ItemStack(Items.IRON_HELMET);
		ArmorItem armorItem = (ArmorItem) helmet.getItem();
		int protection = armorItem.getProtection();

		context.assertTrue(protection == 2,
			"Iron helmet should provide 2 armor, but provides " + protection);
		context.complete();
	}

	/**
	 * Diamond boots should provide 3 armor points.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void diamondBootsHasCorrectArmor(TestContext context) {
		ItemStack boots = new ItemStack(Items.DIAMOND_BOOTS);
		ArmorItem armorItem = (ArmorItem) boots.getItem();
		int protection = armorItem.getProtection();

		context.assertTrue(protection == 3,
			"Diamond boots should provide 3 armor, but provides " + protection);
		context.complete();
	}

	/**
	 * Full diamond armor should provide 20 total armor points.
	 * Helmet=3, Chest=8, Legs=6, Boots=3 = 20
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void fullDiamondArmorProvidesTotalProtection(TestContext context) {
		int helmet = ((ArmorItem) Items.DIAMOND_HELMET).getProtection();
		int chest = ((ArmorItem) Items.DIAMOND_CHESTPLATE).getProtection();
		int legs = ((ArmorItem) Items.DIAMOND_LEGGINGS).getProtection();
		int boots = ((ArmorItem) Items.DIAMOND_BOOTS).getProtection();

		int total = helmet + chest + legs + boots;

		context.assertTrue(total == 20,
			"Full diamond armor should provide 20 total armor (3+8+6+3), but provides " + total +
			" (helmet=" + helmet + ", chest=" + chest + ", legs=" + legs + ", boots=" + boots + ")");
		context.complete();
	}

	/**
	 * Full netherite armor should provide 20 total armor points with toughness.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void fullNetheriteArmorProvidesTotalProtection(TestContext context) {
		int helmet = ((ArmorItem) Items.NETHERITE_HELMET).getProtection();
		int chest = ((ArmorItem) Items.NETHERITE_CHESTPLATE).getProtection();
		int legs = ((ArmorItem) Items.NETHERITE_LEGGINGS).getProtection();
		int boots = ((ArmorItem) Items.NETHERITE_BOOTS).getProtection();

		int total = helmet + chest + legs + boots;

		context.assertTrue(total == 20,
			"Full netherite armor should provide 20 total armor, but provides " + total);
		context.complete();
	}

	// =============================================
	// ARMOR TOUGHNESS TESTS
	// =============================================

	/**
	 * Diamond armor should have 2 toughness per piece.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void diamondArmorHasCorrectToughness(TestContext context) {
		float chestToughness = ((ArmorItem) Items.DIAMOND_CHESTPLATE).getToughness();

		context.assertTrue(chestToughness == 2.0f,
			"Diamond chestplate should have 2.0 toughness, but has " + chestToughness);
		context.complete();
	}

	/**
	 * Netherite armor should have 3 toughness per piece.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void netheriteArmorHasCorrectToughness(TestContext context) {
		float chestToughness = ((ArmorItem) Items.NETHERITE_CHESTPLATE).getToughness();

		context.assertTrue(chestToughness == 3.0f,
			"Netherite chestplate should have 3.0 toughness, but has " + chestToughness);
		context.complete();
	}

	/**
	 * Iron armor should have 0 toughness.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void ironArmorHasZeroToughness(TestContext context) {
		float chestToughness = ((ArmorItem) Items.IRON_CHESTPLATE).getToughness();

		context.assertTrue(chestToughness == 0.0f,
			"Iron chestplate should have 0.0 toughness, but has " + chestToughness);
		context.complete();
	}

	// =============================================
	// MINING SPEED TESTS
	// =============================================

	/**
	 * Diamond pickaxe should mine stone at speed 8.0.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void diamondPickaxeMinesStoneAtCorrectSpeed(TestContext context) {
		ItemStack pickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
		BlockState stone = Blocks.STONE.getDefaultState();

		float speed = pickaxe.getMiningSpeedMultiplier(stone);

		context.assertTrue(speed == 8.0f,
			"Diamond pickaxe should mine stone at speed 8.0, but mines at " + speed);
		context.complete();
	}

	/**
	 * Netherite pickaxe should mine stone at speed 9.0.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void netheritePickaxeMinesStoneAtCorrectSpeed(TestContext context) {
		ItemStack pickaxe = new ItemStack(Items.NETHERITE_PICKAXE);
		BlockState stone = Blocks.STONE.getDefaultState();

		float speed = pickaxe.getMiningSpeedMultiplier(stone);

		context.assertTrue(speed == 9.0f,
			"Netherite pickaxe should mine stone at speed 9.0, but mines at " + speed);
		context.complete();
	}

	/**
	 * Iron pickaxe should mine stone at speed 6.0.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void ironPickaxeMinesStoneAtCorrectSpeed(TestContext context) {
		ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE);
		BlockState stone = Blocks.STONE.getDefaultState();

		float speed = pickaxe.getMiningSpeedMultiplier(stone);

		context.assertTrue(speed == 6.0f,
			"Iron pickaxe should mine stone at speed 6.0, but mines at " + speed);
		context.complete();
	}

	/**
	 * Wooden pickaxe should mine stone at speed 2.0.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void woodenPickaxeMinesStoneAtCorrectSpeed(TestContext context) {
		ItemStack pickaxe = new ItemStack(Items.WOODEN_PICKAXE);
		BlockState stone = Blocks.STONE.getDefaultState();

		float speed = pickaxe.getMiningSpeedMultiplier(stone);

		context.assertTrue(speed == 2.0f,
			"Wooden pickaxe should mine stone at speed 2.0, but mines at " + speed);
		context.complete();
	}

	/**
	 * Diamond axe should mine oak log at speed 8.0.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void diamondAxeMinesWoodAtCorrectSpeed(TestContext context) {
		ItemStack axe = new ItemStack(Items.DIAMOND_AXE);
		BlockState oakLog = Blocks.OAK_LOG.getDefaultState();

		float speed = axe.getMiningSpeedMultiplier(oakLog);

		context.assertTrue(speed == 8.0f,
			"Diamond axe should mine oak log at speed 8.0, but mines at " + speed);
		context.complete();
	}

	/**
	 * Iron shovel should mine dirt at speed 6.0.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void ironShovelMinesDirtAtCorrectSpeed(TestContext context) {
		ItemStack shovel = new ItemStack(Items.IRON_SHOVEL);
		BlockState dirt = Blocks.DIRT.getDefaultState();

		float speed = shovel.getMiningSpeedMultiplier(dirt);

		context.assertTrue(speed == 6.0f,
			"Iron shovel should mine dirt at speed 6.0, but mines at " + speed);
		context.complete();
	}

	// =============================================
	// MINING LEVEL TESTS (CAN HARVEST)
	// =============================================

	/**
	 * Iron pickaxe can harvest diamond ore.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void ironPickaxeCanHarvestDiamondOre(TestContext context) {
		ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE);
		BlockState diamondOre = Blocks.DIAMOND_ORE.getDefaultState();

		boolean canHarvest = pickaxe.isSuitableFor(diamondOre);

		context.assertTrue(canHarvest,
			"Iron pickaxe should be able to harvest diamond ore");
		context.complete();
	}

	/**
	 * Stone pickaxe cannot harvest diamond ore.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void stonePickaxeCannotHarvestDiamondOre(TestContext context) {
		ItemStack pickaxe = new ItemStack(Items.STONE_PICKAXE);
		BlockState diamondOre = Blocks.DIAMOND_ORE.getDefaultState();

		boolean canHarvest = pickaxe.isSuitableFor(diamondOre);

		context.assertTrue(!canHarvest,
			"Stone pickaxe should NOT be able to harvest diamond ore");
		context.complete();
	}

	/**
	 * Wooden pickaxe can harvest stone but not iron ore.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void woodenPickaxeMiningLevelCorrect(TestContext context) {
		ItemStack pickaxe = new ItemStack(Items.WOODEN_PICKAXE);

		boolean canHarvestStone = pickaxe.isSuitableFor(Blocks.STONE.getDefaultState());
		boolean canHarvestIron = pickaxe.isSuitableFor(Blocks.IRON_ORE.getDefaultState());

		context.assertTrue(canHarvestStone,
			"Wooden pickaxe should harvest stone");
		context.assertTrue(!canHarvestIron,
			"Wooden pickaxe should NOT harvest iron ore");
		context.complete();
	}

	/**
	 * Netherite pickaxe can harvest obsidian and ancient debris.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void netheritePickaxeCanHarvestObsidianAndDebris(TestContext context) {
		ItemStack pickaxe = new ItemStack(Items.NETHERITE_PICKAXE);

		boolean canHarvestObsidian = pickaxe.isSuitableFor(Blocks.OBSIDIAN.getDefaultState());
		boolean canHarvestDebris = pickaxe.isSuitableFor(Blocks.ANCIENT_DEBRIS.getDefaultState());

		context.assertTrue(canHarvestObsidian,
			"Netherite pickaxe should harvest obsidian");
		context.assertTrue(canHarvestDebris,
			"Netherite pickaxe should harvest ancient debris");
		context.complete();
	}

	/**
	 * Diamond pickaxe can harvest obsidian.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void diamondPickaxeCanHarvestObsidian(TestContext context) {
		ItemStack pickaxe = new ItemStack(Items.DIAMOND_PICKAXE);

		boolean canHarvest = pickaxe.isSuitableFor(Blocks.OBSIDIAN.getDefaultState());

		context.assertTrue(canHarvest,
			"Diamond pickaxe should harvest obsidian");
		context.complete();
	}

	// =============================================
	// TOOL SUITABILITY TESTS
	// =============================================

	/**
	 * Axe is suitable for wood blocks but not stone.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void axeSuitabilityCorrect(TestContext context) {
		ItemStack axe = new ItemStack(Items.IRON_AXE);

		boolean suitableWood = axe.isSuitableFor(Blocks.OAK_LOG.getDefaultState());
		boolean suitableStone = axe.isSuitableFor(Blocks.STONE.getDefaultState());

		context.assertTrue(suitableWood,
			"Iron axe should be suitable for oak log");
		context.assertTrue(!suitableStone,
			"Iron axe should NOT be suitable for stone");
		context.complete();
	}

	/**
	 * Shovel is suitable for dirt/sand but not stone.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void shovelSuitabilityCorrect(TestContext context) {
		ItemStack shovel = new ItemStack(Items.IRON_SHOVEL);

		boolean suitableDirt = shovel.isSuitableFor(Blocks.DIRT.getDefaultState());
		boolean suitableSand = shovel.isSuitableFor(Blocks.SAND.getDefaultState());
		boolean suitableStone = shovel.isSuitableFor(Blocks.STONE.getDefaultState());

		context.assertTrue(suitableDirt,
			"Iron shovel should be suitable for dirt");
		context.assertTrue(suitableSand,
			"Iron shovel should be suitable for sand");
		context.assertTrue(!suitableStone,
			"Iron shovel should NOT be suitable for stone");
		context.complete();
	}

	/**
	 * Hoe is suitable for leaves.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void hoeSuitabilityCorrect(TestContext context) {
		ItemStack hoe = new ItemStack(Items.IRON_HOE);

		boolean suitableLeaves = hoe.isSuitableFor(Blocks.OAK_LEAVES.getDefaultState());

		context.assertTrue(suitableLeaves,
			"Iron hoe should be suitable for leaves");
		context.complete();
	}

	/**
	 * Pickaxe is suitable for stone but not wood.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void pickaxeSuitabilityCorrect(TestContext context) {
		ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE);

		boolean suitableStone = pickaxe.isSuitableFor(Blocks.STONE.getDefaultState());
		boolean suitableWood = pickaxe.isSuitableFor(Blocks.OAK_LOG.getDefaultState());

		context.assertTrue(suitableStone,
			"Iron pickaxe should be suitable for stone");
		context.assertTrue(!suitableWood,
			"Iron pickaxe should NOT be suitable for oak log");
		context.complete();
	}

	// =============================================
	// ATTACK SPEED ATTRIBUTE TESTS
	// =============================================

	/**
	 * Sword should have attack speed of 1.6.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void swordHasCorrectAttackSpeed(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));
		player.playerTick();

		double attackSpeed = player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED);

		// Base attack speed is 4.0, sword modifier is -2.4, so total is 1.6
		context.assertTrue(Math.abs(attackSpeed - 1.6) < 0.01,
			"Diamond sword attack speed should be 1.6, but is " + attackSpeed);
		context.complete();
	}

	/**
	 * Axe should have attack speed of 1.0 (diamond) or 0.9 (iron/stone).
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void diamondAxeHasCorrectAttackSpeed(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.DIAMOND_AXE));
		player.playerTick();

		double attackSpeed = player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED);

		// Diamond axe modifier is -3.0, so total is 1.0
		context.assertTrue(Math.abs(attackSpeed - 1.0) < 0.01,
			"Diamond axe attack speed should be 1.0, but is " + attackSpeed);
		context.complete();
	}

	// =============================================
	// PLAYER ATTRIBUTE INTEGRATION TESTS
	// =============================================

	/**
	 * Player attack damage should increase when holding a weapon.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void playerAttackDamageIncreasesWithWeapon(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		// Check bare-handed damage
		double baseDamage = player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);

		// Equip diamond sword
		player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));
		player.playerTick();

		double weaponDamage = player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);

		context.assertTrue(weaponDamage > baseDamage,
			"Attack damage should increase with weapon: bare=" + baseDamage + ", sword=" + weaponDamage);
		context.assertTrue(weaponDamage == 7.0,
			"Diamond sword should give 7.0 attack damage, but gives " + weaponDamage);
		context.complete();
	}

	/**
	 * Player armor should increase when wearing armor.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void playerArmorIncreasesWithEquipment(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		// Check bare armor
		double baseArmor = player.getAttributeValue(EntityAttributes.GENERIC_ARMOR);

		// Equip diamond chestplate
		player.equipStack(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE));
		player.playerTick();

		double armoredValue = player.getAttributeValue(EntityAttributes.GENERIC_ARMOR);

		context.assertTrue(armoredValue > baseArmor,
			"Armor should increase with equipment: bare=" + baseArmor + ", equipped=" + armoredValue);
		context.assertTrue(armoredValue == 8.0,
			"Diamond chestplate should give 8.0 armor, but gives " + armoredValue);
		context.complete();
	}

	/**
	 * Full armor set should give correct total armor value.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void fullArmorSetGivesCorrectTotalArmor(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		// Equip full diamond armor
		player.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
		player.equipStack(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE));
		player.equipStack(EquipmentSlot.LEGS, new ItemStack(Items.DIAMOND_LEGGINGS));
		player.equipStack(EquipmentSlot.FEET, new ItemStack(Items.DIAMOND_BOOTS));
		player.playerTick();

		double totalArmor = player.getAttributeValue(EntityAttributes.GENERIC_ARMOR);

		// Full diamond armor: 3+8+6+3 = 20
		context.assertTrue(totalArmor == 20.0,
			"Full diamond armor should give 20.0 total armor, but gives " + totalArmor);
		context.complete();
	}
}
