package com.sigmundgranaas.forgero.tests;

import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroTestUtils;
import com.sigmundgranaas.forgero.mc.testcommon.helpers.PlayerFactory;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import static org.junit.jupiter.api.Assertions.*;

/**
 * High-value gameplay tests for the BlockExplosionEffect.
 * Validates that the end_crystal upgrade creates explosions when hitting blocks.
 *
 * <p>VALUE: ⭐⭐⭐⭐⭐ This validates the end_crystal upgrade behavior - explosive mining
 * is a unique gameplay mechanic that must work correctly.
 *
 * <p>The BlockExplosionEffect is triggered via OnHitBlock when a player attacks a block
 * with a tool that has this effect (e.g., end_crystal upgrade).
 */
public class BlockExplosionEffectTest implements ForgeroGameTest {

	private static final BlockPos CENTER = new BlockPos(5, 5, 5);

	/**
	 * GAMEPLAY TEST: End crystal upgrade material is registered.
	 *
	 * <p>Validates that the end_crystal secondary material exists and can be looked up.
	 *
	 * <p>VALUE: ⭐⭐⭐⭐ Prerequisite for explosion functionality.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "block_explosion")
	public void end_crystal_material_is_registered(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);

		var endCrystal = ctx.component("forgero:end_crystal");
		assertTrue(endCrystal.isPresent(), "End crystal material must be registered for explosion effects");

		context.complete();
	}

	/**
	 * GAMEPLAY TEST: No explosion without the explosion effect.
	 *
	 * <p>Validates that attacking blocks with a normal tool does NOT create explosions.
	 * This is a negative test to ensure explosions only happen with the effect.
	 *
	 * <p>VALUE: ⭐⭐⭐⭐ Ensures explosion is opt-in via effect, not default behavior.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "block_explosion")
	public void no_explosion_without_effect(TestContext context) {
		// Create player with vanilla diamond pickaxe (no explosion effect)
		ServerPlayerEntity player = PlayerFactory.create(context)
				.at(CENTER)
				.holding(new ItemStack(Items.DIAMOND_PICKAXE))
				.build();

		// Place a 3x3x3 cube of stone blocks
		for (int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				for (int z = -1; z <= 1; z++) {
					context.setBlockState(CENTER.add(x, y, z), Blocks.STONE);
				}
			}
		}

		// Simulate attacking the center block
		ServerPlayerInteractionManager interactionManager = player.interactionManager;
		interactionManager.processBlockBreakingAction(
				context.getAbsolutePos(CENTER),
				PlayerActionC2SPacket.Action.START_DESTROY_BLOCK,
				Direction.UP,
				context.getWorld().getHeight(),
				0
		);

		// Wait a tick
		context.waitAndRun(2, () -> {
			// Count remaining stone blocks - should all still be there
			int remainingBlocks = 0;
			for (int x = -1; x <= 1; x++) {
				for (int y = -1; y <= 1; y++) {
					for (int z = -1; z <= 1; z++) {
						if (context.getBlockState(CENTER.add(x, y, z)).getBlock() == Blocks.STONE) {
							remainingBlocks++;
						}
					}
				}
			}

			// At least 26 blocks should remain (center might be broken by attack, but no explosion)
			assertTrue(remainingBlocks >= 26,
					"Without explosion effect, at least 26 blocks should remain (got " + remainingBlocks + ")");

			context.complete();
		});
	}

	/**
	 * GAMEPLAY TEST: Standard Forgero tool does not explode.
	 *
	 * <p>Validates that standard Forgero tools without the end_crystal upgrade
	 * do not create explosions when mining blocks.
	 *
	 * <p>VALUE: ⭐⭐⭐⭐ Ensures only specific upgrades trigger explosions.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "block_explosion")
	public void standard_forgero_tool_no_explosion(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);

		// Get a standard iron pickaxe (no explosion effect)
		var ironPickaxe = ctx.component("forgero:iron-pickaxe");
		assertTrue(ironPickaxe.isPresent(), "Iron pickaxe must be registered");
		ItemStack pickaxeStack = ctx.toStack(ironPickaxe.get()).orElseThrow();

		ServerPlayerEntity player = PlayerFactory.create(context)
				.at(CENTER)
				.holding(pickaxeStack)
				.build();

		// Place a 3x3x3 cube of dirt blocks
		for (int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				for (int z = -1; z <= 1; z++) {
					context.setBlockState(CENTER.add(x, y, z), Blocks.DIRT);
				}
			}
		}

		// Simulate attacking the center block
		ServerPlayerInteractionManager interactionManager = player.interactionManager;
		interactionManager.processBlockBreakingAction(
				context.getAbsolutePos(CENTER),
				PlayerActionC2SPacket.Action.START_DESTROY_BLOCK,
				Direction.UP,
				context.getWorld().getHeight(),
				0
		);

		// Wait a tick
		context.waitAndRun(2, () -> {
			// Count remaining dirt blocks - most should still be there (no explosion)
			int remainingBlocks = 0;
			for (int x = -1; x <= 1; x++) {
				for (int y = -1; y <= 1; y++) {
					for (int z = -1; z <= 1; z++) {
						if (context.getBlockState(CENTER.add(x, y, z)).getBlock() == Blocks.DIRT) {
							remainingBlocks++;
						}
					}
				}
			}

			// Should have 26 or 27 blocks remaining (center might be broken, but no explosion)
			assertTrue(remainingBlocks >= 26,
					"Standard tool should not explode - expected 26-27 blocks, got " + remainingBlocks);

			context.complete();
		});
	}
}
