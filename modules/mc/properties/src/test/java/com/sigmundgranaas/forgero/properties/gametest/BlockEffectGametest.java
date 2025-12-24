package com.sigmundgranaas.forgero.properties.gametest;

import com.sigmundgranaas.forgero.effects.block.IgniteBlockEffect;
import com.sigmundgranaas.forgero.effects.block.PlaceBlockEffect;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/**
 * Gametests for OnHitBlockEffect implementations.
 * Tests block placement and modification effects triggered on block hit.
 */
public class BlockEffectGametest {

	// ========== PLACE BLOCK EFFECT TESTS ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testPlaceBlockEffectAtPosition(TestContext context) {
		World world = context.getWorld();
		BlockPos hitPos = context.getAbsolutePos(new BlockPos(1, 1, 1));
		LivingEntity source = context.createMockCreativeServerPlayerInWorld();

		// Create effect that places a torch at hit position
		PlaceBlockEffect effect = new PlaceBlockEffect(
				new Identifier("minecraft", "torch"),
				PlaceBlockEffect.OffsetDirection.NONE,
				true
		);

		// Ensure position is air before placing
		world.setBlockState(hitPos, Blocks.AIR.getDefaultState());
		context.assertTrue(world.getBlockState(hitPos).isAir(), "Position should be air before placing");

		effect.apply(world, source, hitPos);

		context.assertTrue(world.getBlockState(hitPos).getBlock() == Blocks.TORCH, "Torch should be placed at hit position");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testPlaceBlockEffectWithOffset(TestContext context) {
		World world = context.getWorld();
		BlockPos hitPos = context.getAbsolutePos(new BlockPos(1, 1, 1));
		LivingEntity source = context.createMockCreativeServerPlayerInWorld();

		// Place a stone block to place torch on top of
		world.setBlockState(hitPos, Blocks.STONE.getDefaultState());

		// Create effect that places a torch above the hit position
		PlaceBlockEffect effect = new PlaceBlockEffect(
				new Identifier("minecraft", "torch"),
				PlaceBlockEffect.OffsetDirection.UP,
				true
		);

		BlockPos abovePos = hitPos.up();
		context.assertTrue(world.getBlockState(abovePos).isAir(), "Position above should be air before placing");

		effect.apply(world, source, hitPos);

		context.assertTrue(world.getBlockState(abovePos).getBlock() == Blocks.TORCH, "Torch should be placed above hit position");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testPlaceBlockEffectReplaceAirOnly(TestContext context) {
		World world = context.getWorld();
		BlockPos hitPos = context.getAbsolutePos(new BlockPos(1, 1, 1));
		LivingEntity source = context.createMockCreativeServerPlayerInWorld();

		// Place a stone block at hit position
		world.setBlockState(hitPos, Blocks.STONE.getDefaultState());

		// Create effect that only replaces air
		PlaceBlockEffect effect = new PlaceBlockEffect(
				new Identifier("minecraft", "dirt"),
				PlaceBlockEffect.OffsetDirection.NONE,
				true  // replace_air_only = true
		);

		effect.apply(world, source, hitPos);

		// Stone should remain because replace_air_only is true
		context.assertTrue(world.getBlockState(hitPos).getBlock() == Blocks.STONE, "Stone should not be replaced when replace_air_only is true");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testPlaceBlockEffectSourceFacing(TestContext context) {
		World world = context.getWorld();
		BlockPos hitPos = context.getAbsolutePos(new BlockPos(2, 1, 2));
		LivingEntity source = context.createMockCreativeServerPlayerInWorld();

		// Position source entity facing east
		source.setYaw(270f);  // East facing
		Direction expectedFacing = source.getHorizontalFacing();
		Direction placementDirection = expectedFacing.getOpposite();

		// Create effect that places in the direction the source is facing
		PlaceBlockEffect effect = new PlaceBlockEffect(
				new Identifier("minecraft", "cobblestone"),
				PlaceBlockEffect.OffsetDirection.SOURCE_FACING,
				true
		);

		effect.apply(world, source, hitPos);

		BlockPos expectedPos = hitPos.offset(placementDirection);
		context.assertTrue(world.getBlockState(expectedPos).getBlock() == Blocks.COBBLESTONE,
				"Block should be placed in the direction source is facing (opposite of facing direction)");
		context.complete();
	}

	// ========== IGNITE BLOCK EFFECT TESTS ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testIgniteBlockEffectSingleBlock(TestContext context) {
		World world = context.getWorld();
		BlockPos hitPos = context.getAbsolutePos(new BlockPos(1, 1, 1));
		LivingEntity source = context.createMockCreativeServerPlayerInWorld();

		// Place a burnable block (planks)
		world.setBlockState(hitPos, Blocks.OAK_PLANKS.getDefaultState());

		// Create effect with radius 0 (only ignite hit position)
		IgniteBlockEffect effect = new IgniteBlockEffect(0, true);

		effect.apply(world, source, hitPos);

		// Fire should be placed above the burnable block
		BlockPos abovePos = hitPos.up();
		BlockState aboveState = world.getBlockState(abovePos);
		context.assertTrue(aboveState.getBlock() == Blocks.FIRE || aboveState.getBlock() == Blocks.SOUL_FIRE,
				"Fire should be placed above burnable block");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testIgniteBlockEffectWithRadius(TestContext context) {
		World world = context.getWorld();
		BlockPos centerPos = context.getAbsolutePos(new BlockPos(2, 1, 2));
		LivingEntity source = context.createMockCreativeServerPlayerInWorld();

		// Place burnable blocks in a small area
		world.setBlockState(centerPos, Blocks.OAK_PLANKS.getDefaultState());
		world.setBlockState(centerPos.north(), Blocks.OAK_PLANKS.getDefaultState());
		world.setBlockState(centerPos.south(), Blocks.OAK_PLANKS.getDefaultState());

		// Create effect with radius 1
		IgniteBlockEffect effect = new IgniteBlockEffect(1, true);

		effect.apply(world, source, centerPos);

		// Check that fire was placed in the area
		boolean foundFire = false;
		for (BlockPos pos : BlockPos.iterateOutwards(centerPos, 1, 1, 1)) {
			BlockState state = world.getBlockState(pos.up());
			if (state.getBlock() == Blocks.FIRE || state.getBlock() == Blocks.SOUL_FIRE) {
				foundFire = true;
				break;
			}
		}

		context.assertTrue(foundFire, "At least one fire block should be placed within radius");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testIgniteBlockEffectOnlyIgniteBurnable(TestContext context) {
		World world = context.getWorld();
		BlockPos hitPos = context.getAbsolutePos(new BlockPos(1, 1, 1));
		LivingEntity source = context.createMockCreativeServerPlayerInWorld();

		// Place a non-burnable block (stone)
		world.setBlockState(hitPos, Blocks.STONE.getDefaultState());

		// Create effect with only_ignite_burnable = true
		IgniteBlockEffect effect = new IgniteBlockEffect(0, true);

		effect.apply(world, source, hitPos);

		// No fire should be placed above stone
		BlockPos abovePos = hitPos.up();
		BlockState aboveState = world.getBlockState(abovePos);
		context.assertFalse(aboveState.getBlock() == Blocks.FIRE || aboveState.getBlock() == Blocks.SOUL_FIRE,
				"Fire should not be placed above non-burnable block when only_ignite_burnable is true");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testIgniteBlockEffectIgnoreNonBurnableFlag(TestContext context) {
		World world = context.getWorld();
		BlockPos hitPos = context.getAbsolutePos(new BlockPos(1, 1, 1));
		LivingEntity source = context.createMockCreativeServerPlayerInWorld();

		// Clear the area to ensure we're working with air
		world.setBlockState(hitPos, Blocks.AIR.getDefaultState());

		// Create effect with only_ignite_burnable = false (should place fire anywhere valid)
		IgniteBlockEffect effect = new IgniteBlockEffect(0, false);

		effect.apply(world, source, hitPos);

		// Fire should be placed at or near the position even without burnable blocks
		boolean foundFire = false;

		// Check the hit position itself
		if (world.getBlockState(hitPos).getBlock() == Blocks.FIRE ||
				world.getBlockState(hitPos).getBlock() == Blocks.SOUL_FIRE) {
			foundFire = true;
		}

		// Check adjacent positions
		if (!foundFire) {
			for (Direction dir : Direction.values()) {
				BlockPos adjacentPos = hitPos.offset(dir);
				BlockState state = world.getBlockState(adjacentPos);
				if (state.getBlock() == Blocks.FIRE || state.getBlock() == Blocks.SOUL_FIRE) {
					foundFire = true;
					break;
				}
			}
		}

		context.assertTrue(foundFire, "Fire should be placed when only_ignite_burnable is false");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testIgniteBlockEffectAdjacentPlacement(TestContext context) {
		World world = context.getWorld();
		BlockPos hitPos = context.getAbsolutePos(new BlockPos(2, 1, 2));
		LivingEntity source = context.createMockCreativeServerPlayerInWorld();

		// Place burnable blocks around the hit position
		world.setBlockState(hitPos, Blocks.OAK_PLANKS.getDefaultState());
		world.setBlockState(hitPos.north(), Blocks.OAK_PLANKS.getDefaultState());
		world.setBlockState(hitPos.south(), Blocks.OAK_PLANKS.getDefaultState());

		// Create effect that ignites burnable blocks
		IgniteBlockEffect effect = new IgniteBlockEffect(0, true);

		effect.apply(world, source, hitPos);

		// Fire should be placed somewhere in the area due to burnable blocks
		boolean foundFire = false;
		for (Direction dir : Direction.values()) {
			BlockPos checkPos = hitPos.offset(dir);
			BlockState state = world.getBlockState(checkPos);
			if (state.getBlock() == Blocks.FIRE || state.getBlock() == Blocks.SOUL_FIRE) {
				foundFire = true;
				break;
			}
		}

		// Also check above the hit position
		if (!foundFire) {
			BlockState aboveState = world.getBlockState(hitPos.up());
			if (aboveState.getBlock() == Blocks.FIRE || aboveState.getBlock() == Blocks.SOUL_FIRE) {
				foundFire = true;
			}
		}

		context.assertTrue(foundFire, "Fire should be placed near or above burnable blocks");
		context.complete();
	}
}
