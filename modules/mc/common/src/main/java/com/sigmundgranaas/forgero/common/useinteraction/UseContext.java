package com.sigmundgranaas.forgero.common.useinteraction;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Context object containing all data needed for use phase effects.
 * Immutable record that captures the state at the time of the use event.
 *
 * <p>This is the core context passed to all {@link UsePhaseEffect} implementations
 * during item use lifecycle events.</p>
 */
public record UseContext(
		World world,
		LivingEntity user,
		Hand hand,
		ItemStack stack,
		int chargeTime,
		int remainingTicks,
		float pullProgress,
		@Nullable Entity target
) {
	/**
	 * Creates a context for the start of a use action (right-click initiated).
	 */
	public static UseContext start(World world, LivingEntity user, Hand hand, ItemStack stack) {
		return new UseContext(world, user, hand, stack, 0, 0, 0f, null);
	}

	/**
	 * Creates a context for the start of a use action with an entity target.
	 */
	public static UseContext startWithTarget(World world, LivingEntity user, Hand hand, ItemStack stack, Entity target) {
		return new UseContext(world, user, hand, stack, 0, 0, 0f, target);
	}

	/**
	 * Creates a context for a tick during use.
	 */
	public static UseContext tick(World world, LivingEntity user, Hand hand, ItemStack stack,
	                              int chargeTime, int remainingTicks, float pullProgress) {
		return new UseContext(world, user, hand, stack, chargeTime, remainingTicks, pullProgress, null);
	}

	/**
	 * Creates a context for when use is stopped (release).
	 */
	public static UseContext release(World world, LivingEntity user, Hand hand, ItemStack stack,
	                                 int chargeTime, int remainingTicks, float pullProgress) {
		return new UseContext(world, user, hand, stack, chargeTime, remainingTicks, pullProgress, null);
	}

	/**
	 * Creates a context for when use is finished (max time reached).
	 */
	public static UseContext finish(World world, LivingEntity user, Hand hand, ItemStack stack, int totalUseTime) {
		return new UseContext(world, user, hand, stack, totalUseTime, 0, 1.0f, null);
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
	 * @return true if the item is fully charged (pullProgress >= 1.0)
	 */
	public boolean isFullyCharged() {
		return pullProgress >= 1.0f;
	}

	/**
	 * @return the user as a PlayerEntity if applicable
	 */
	public Optional<PlayerEntity> asPlayer() {
		return user instanceof PlayerEntity player ? Optional.of(player) : Optional.empty();
	}

	/**
	 * @return the target entity if present
	 */
	public Optional<Entity> getTarget() {
		return Optional.ofNullable(target);
	}

	/**
	 * Creates a new context with an updated stack.
	 */
	public UseContext withStack(ItemStack newStack) {
		return new UseContext(world, user, hand, newStack, chargeTime, remainingTicks, pullProgress, target);
	}

	/**
	 * Creates a new context with an updated target.
	 */
	public UseContext withTarget(Entity newTarget) {
		return new UseContext(world, user, hand, stack, chargeTime, remainingTicks, pullProgress, newTarget);
	}
}
