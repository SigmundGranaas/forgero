package com.sigmundgranaas.forgero.common.useinteraction;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Optional;

/**
 * Context object containing all data needed for block use interactions.
 * Immutable record that captures the state when an item is used on a block.
 *
 * <p>This is similar to {@link net.minecraft.item.ItemUsageContext} but designed
 * for the Forgero property system.</p>
 */
public record BlockUseContext(
		World world,
		LivingEntity user,
		Hand hand,
		ItemStack stack,
		BlockPos pos,
		Direction side,
		BlockHitResult hitResult
) {
	/**
	 * Creates a block use context from the given parameters.
	 */
	public static BlockUseContext create(World world, LivingEntity user, Hand hand,
	                                     ItemStack stack, BlockHitResult hitResult) {
		return new BlockUseContext(
				world,
				user,
				hand,
				stack,
				hitResult.getBlockPos(),
				hitResult.getSide(),
				hitResult
		);
	}

	/**
	 * @return true if this is running on the server side
	 */
	public boolean isServer() {
		return !world.isClient();
	}

	/**
	 * @return true if this is running on the client side
	 */
	public boolean isClient() {
		return world.isClient();
	}

	/**
	 * @return the user as a PlayerEntity if applicable
	 */
	public Optional<PlayerEntity> asPlayer() {
		return user instanceof PlayerEntity player ? Optional.of(player) : Optional.empty();
	}

	/**
	 * Creates a new context with an updated stack.
	 */
	public BlockUseContext withStack(ItemStack newStack) {
		return new BlockUseContext(world, user, hand, newStack, pos, side, hitResult);
	}
}
