package com.sigmundgranaas.forgero.common.runtime;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.runtime.Key;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A central registry for Minecraft-specific type-safe keys used in DynamicContext.
 * These keys allow predicates and properties to access gameplay data in a type-safe manner.
 * <p>
 * This is the single source of truth for all Minecraft context keys.
 */
public final class MinecraftContextKeys {
	/**
	 * The ItemStack being used/held.
	 */
	public static final Key<ItemStack> STACK = new Key<>(new OpenIdentifier("minecraft", "stack"));

	/**
	 * The world where the event occurs.
	 */
	public static final Key<World> WORLD = new Key<>(new OpenIdentifier("minecraft", "world"));

	/**
	 * The source/user entity (e.g., the attacker, the item user).
	 */
	public static final Key<Entity> SOURCE_ENTITY = new Key<>(new OpenIdentifier("minecraft", "source_entity"));

	/**
	 * The target entity (e.g., the entity being hit).
	 */
	public static final Key<Entity> TARGET_ENTITY = new Key<>(new OpenIdentifier("minecraft", "target_entity"));

	/**
	 * The block position being interacted with.
	 */
	public static final Key<BlockPos> BLOCK_POS = new Key<>(new OpenIdentifier("minecraft", "block_pos"));

	// Deprecated aliases for backward compatibility within predicate module
	/**
	 * @deprecated Use {@link #SOURCE_ENTITY} instead.
	 */
	@Deprecated
	public static final Key<Entity> ENTITY = SOURCE_ENTITY;

	/**
	 * @deprecated Use {@link #BLOCK_POS} instead.
	 */
	@Deprecated
	public static final Key<BlockPos> TARGET_BLOCK_POS = BLOCK_POS;

	private MinecraftContextKeys() {
		// Static utility class
	}
}
