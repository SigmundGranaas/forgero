package com.sigmundgranaas.forgero.properties.gametest;

import java.util.List;

import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.GameMode;

/**
 * Reusable helper for driving real block-breaking in gametests, encapsulating the two distinct
 * mechanics so tests don't trip on them:
 * <ul>
 *   <li>{@link #aoeMine} — CREATIVE + teleport-to-block instant break, which lets a block-breaking
 *       property's AOE apply. Use for "blocks became AIR" assertions. Creative yields NO drops.</li>
 *   <li>{@link #breakForDrops} — SURVIVAL + teleport + interaction-manager {@code tryBreakBlock},
 *       which fully breaks the block AND produces drops (running the loot mixins). Use for drop /
 *       loot-transform assertions. A survival mock player never accrues break progress via the
 *       packet path, so this is the way to get real drops.</li>
 * </ul>
 */
public final class BlockBreakTestHelper {

	private BlockBreakTestHelper() {
	}

	/** Instant-breaks the block at {@code relRoot} in creative so any AOE block-breaking property applies. */
	public static void aoeMine(TestContext context, ItemStack tool, BlockPos relRoot) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		BlockPos abs = context.getAbsolutePos(relRoot);
		player.teleport(abs.getX(), abs.getY(), abs.getZ());
		player.setStackInHand(Hand.MAIN_HAND, tool);
		player.setYaw(0);
		player.setPitch(45);
		new PlayerActionTestHelper(context, player).mineBlock(abs);
	}

	/** Fully breaks the block at {@code relPos} in survival (real drops + loot mixins fire). */
	public static void breakForDrops(TestContext context, ItemStack tool, BlockPos relPos) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.changeGameMode(GameMode.SURVIVAL);
		BlockPos abs = context.getAbsolutePos(relPos);
		player.teleport(abs.getX(), abs.getY(), abs.getZ());
		player.setStackInHand(Hand.MAIN_HAND, tool);
		clearDrops(context, relPos);
		player.interactionManager.tryBreakBlock(abs);
	}

	/** The item stacks currently dropped within ~3 blocks of {@code relPos}. */
	public static List<ItemStack> dropsNear(TestContext context, BlockPos relPos) {
		Box area = new Box(context.getAbsolutePos(relPos)).expand(3.0);
		return context.getWorld().getEntitiesByClass(ItemEntity.class, area, e -> true)
				.stream().map(ItemEntity::getStack).toList();
	}

	private static void clearDrops(TestContext context, BlockPos relPos) {
		Box area = new Box(context.getAbsolutePos(relPos)).expand(3.0);
		context.getWorld().getEntitiesByClass(ItemEntity.class, area, e -> true).forEach(ItemEntity::discard);
	}
}
