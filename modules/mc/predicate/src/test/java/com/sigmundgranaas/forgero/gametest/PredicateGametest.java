package com.sigmundgranaas.forgero.gametest;

import com.sigmundgranaas.forgero.predicate.minecraft.entity.EntityFlagPredicate;
import com.sigmundgranaas.forgero.predicate.minecraft.item.EquipmentPredicate;
import com.sigmundgranaas.forgero.predicate.minecraft.item.ItemPredicate;
import com.sigmundgranaas.forgero.predicate.minecraft.util.DimensionPredicate;
import com.sigmundgranaas.forgero.predicate.minecraft.util.LightPredicate;
import com.sigmundgranaas.forgero.predicate.minecraft.util.LocationPredicate;
import com.sigmundgranaas.forgero.predicate.minecraft.util.NumericPredicate;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

public class PredicateGametest {
	private static final String EMPTY_STRUCTURE = FabricGameTest.EMPTY_STRUCTURE;

	// region EntityFlag Predicate Tests
	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testIsSneakingTrue(TestContext context) {
		EntityFlagPredicate predicate = new EntityFlagPredicate(Optional.of(true), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
		LivingEntity entity = context.spawnEntity(EntityType.ARMOR_STAND, BlockPos.ORIGIN);

		entity.setSneaking(true);
		context.assertTrue(predicate.test(entity), "Predicate should be true when entity is sneaking");

		entity.setSneaking(false);
		context.assertFalse(predicate.test(entity), "Predicate should be false when entity is not sneaking");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testIsOnFireFalse(TestContext context) {
		EntityFlagPredicate predicate = new EntityFlagPredicate(Optional.empty(), Optional.of(false), Optional.empty(), Optional.empty(), Optional.empty());
		LivingEntity entity = context.spawnEntity(EntityType.ARMOR_STAND, BlockPos.ORIGIN);

		entity.setFireTicks(0);
		context.assertTrue(predicate.test(entity), "Predicate should be true when entity is not on fire");

		entity.setFireTicks(100);
		context.assertFalse(predicate.test(entity), "Predicate should be false when entity is on fire");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testMultipleEntityFlagsMatch(TestContext context) {
		EntityFlagPredicate predicate = new EntityFlagPredicate(Optional.of(true), Optional.empty(), Optional.of(true), Optional.of(true), Optional.empty());
		LivingEntity entity = context.spawnEntity(EntityType.ARMOR_STAND, new BlockPos(0, 1, 0));

		entity.setSneaking(true);
		entity.setSprinting(true);

		context.waitAndRun(5, () -> {
			context.assertTrue(predicate.test(entity), "Predicate should match with multiple correct flags");
			context.complete();
		});
	}
	// endregion

	// region Equipment Predicate Tests
	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testMainhandMatch(TestContext context) {
		ItemPredicate ironSword = new ItemPredicate(Optional.of(List.of(Items.IRON_SWORD)), Optional.empty(), Optional.empty());
		EquipmentPredicate predicate = new EquipmentPredicate(Optional.of(ironSword), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
		LivingEntity entity = context.spawnEntity(EntityType.ZOMBIE, BlockPos.ORIGIN);

		entity.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
		context.assertTrue(predicate.test(entity), "Predicate should match when holding iron sword");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testMainhandMismatch(TestContext context) {
		ItemPredicate ironSword = new ItemPredicate(Optional.of(List.of(Items.IRON_SWORD)), Optional.empty(), Optional.empty());
		EquipmentPredicate predicate = new EquipmentPredicate(Optional.of(ironSword), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
		LivingEntity entity = context.spawnEntity(EntityType.ZOMBIE, BlockPos.ORIGIN);

		entity.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
		context.assertFalse(predicate.test(entity), "Predicate should not match when holding diamond sword");

		entity.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
		context.assertFalse(predicate.test(entity), "Predicate should not match when holding nothing");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testHeadAndChestMatch(TestContext context) {
		ItemPredicate helmet = new ItemPredicate(Optional.of(List.of(Items.IRON_HELMET)), Optional.empty(), Optional.empty());
		ItemPredicate chestplate = new ItemPredicate(Optional.of(List.of(Items.IRON_CHESTPLATE)), Optional.empty(), Optional.empty());
		EquipmentPredicate predicate = new EquipmentPredicate(Optional.empty(), Optional.empty(), Optional.of(helmet), Optional.of(chestplate), Optional.empty(), Optional.empty());
		LivingEntity entity = context.spawnEntity(EntityType.ZOMBIE, BlockPos.ORIGIN);

		entity.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
		entity.equipStack(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
		context.assertTrue(predicate.test(entity), "Predicate should match with correct armor");

		context.complete();
	}
	// endregion

	// region Location Predicate Tests
	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testDimensionMatch(TestContext context) {
		DimensionPredicate dimension = new DimensionPredicate(List.of(World.OVERWORLD.getValue()));
		LocationPredicate predicate = new LocationPredicate(Optional.empty(), Optional.of(dimension), Optional.empty(), Optional.empty(), Optional.empty());

		context.assertTrue(predicate.test(context.getWorld(), BlockPos.ORIGIN), "Predicate should match overworld dimension");
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testDimensionMismatch(TestContext context) {
		DimensionPredicate dimension = new DimensionPredicate(List.of(World.NETHER.getValue()));
		LocationPredicate predicate = new LocationPredicate(Optional.empty(), Optional.of(dimension), Optional.empty(), Optional.empty(), Optional.empty());

		context.assertFalse(predicate.test(context.getWorld(), BlockPos.ORIGIN), "Predicate should not match nether dimension");
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testLightLevelMatch(TestContext context) {
		NumericPredicate lightLevel = new NumericPredicate(Optional.of(10.0), Optional.empty(), Optional.empty());
		LightPredicate light = new LightPredicate(Optional.of(lightLevel), Optional.empty());
		LocationPredicate predicate = new LocationPredicate(Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(light), Optional.empty());
		BlockPos testPos = BlockPos.ORIGIN;

		context.setBlockState(testPos.up(3), Blocks.GLOWSTONE);

		context.waitAndRun(10, () -> {
			context.assertTrue(predicate.test(context.getWorld(), context.getAbsolutePos(testPos).up()), "Glowstone should provide enough block light");
			context.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testCanSeeSky(TestContext context) {
		LocationPredicate canSeeSkyPredicate = new LocationPredicate(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(true));
		LocationPredicate cannotSeeSkyPredicate = new LocationPredicate(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(false));
		BlockPos testPos = BlockPos.ORIGIN;

		context.assertTrue(canSeeSkyPredicate.test(context.getWorld(), testPos), "Position should be able to see sky in empty structure");

		context.setBlockState(testPos.up(2), Blocks.STONE);

		context.waitAndRun(5, () -> {
			context.assertFalse(canSeeSkyPredicate.test(context.getWorld(), context.getAbsolutePos(testPos)), "Stone block should obstruct sky view");
			context.assertTrue(cannotSeeSkyPredicate.test(context.getWorld(), context.getAbsolutePos(testPos)), "Should now be considered unable to see sky");
			context.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testCombinedLocationPredicateMatch(TestContext context) {
		DimensionPredicate dimension = new DimensionPredicate(List.of(World.OVERWORLD.getValue()));
		LightPredicate light = new LightPredicate(Optional.empty(), Optional.of(new NumericPredicate(Optional.empty(), Optional.of(5.0), Optional.empty())));
		LocationPredicate predicate = new LocationPredicate(Optional.empty(), Optional.of(dimension), Optional.empty(), Optional.of(light), Optional.of(false));
		// Move test position to the center to avoid edge effects
		BlockPos testPos = new BlockPos(1, 1, 1);

		// Build a 3x3 roof to properly block sky light
		for (int x = 0; x <= 2; x++) {
			for (int z = 0; z <= 2; z++) {
				context.setBlockState(new BlockPos(x, 3, z), Blocks.BLACK_WOOL);
			}
		}

		context.waitAndRun(20, () -> {
			context.assertTrue(predicate.test(context.getWorld(), context.getAbsolutePos(testPos)), "Predicate should match in dark, covered, overworld location");
			context.complete();
		});
	}
	// endregion
}
