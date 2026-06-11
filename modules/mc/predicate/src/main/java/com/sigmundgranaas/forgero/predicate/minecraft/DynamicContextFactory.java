package com.sigmundgranaas.forgero.predicate.minecraft;

import com.sigmundgranaas.forgero.common.runtime.DynamicContext;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

/**
 * Factory for creating DynamicContext from Minecraft game state.
 * Use where Entity/World is available to enable conditional attributes.
 */
public final class DynamicContextFactory {

	private DynamicContextFactory() {}

	/**
	 * Creates context from a source entity.
	 * Enables predicates to check entity state (sneaking, health, etc.)
	 *
	 * @param entity The source entity (e.g., player holding item)
	 * @return Context with entity and world, or empty if null
	 */
	public static DynamicContext fromEntity(@Nullable Entity entity) {
		if (entity == null) {
			return DynamicContext.empty();
		}

		var builder = new DynamicContext.Builder();
		builder.put(MinecraftContextKeys.SOURCE_ENTITY, entity);

		World world = entity.getWorld();
		if (world != null) {
			builder.put(MinecraftContextKeys.WORLD, world);
		}

		return builder.build();
	}

	/**
	 * Creates context from source and target entities.
	 * Enables predicates to check both attacker and target state.
	 *
	 * @param source The source entity (e.g., attacker)
	 * @param target The target entity (e.g., entity being hit)
	 * @return Context with both entities
	 */
	public static DynamicContext fromEntities(@Nullable Entity source, @Nullable Entity target) {
		if (source == null && target == null) {
			return DynamicContext.empty();
		}

		var builder = new DynamicContext.Builder();

		if (source != null) {
			builder.put(MinecraftContextKeys.SOURCE_ENTITY, source);
			World world = source.getWorld();
			if (world != null) {
				builder.put(MinecraftContextKeys.WORLD, world);
			}
		}

		if (target != null) {
			builder.put(MinecraftContextKeys.TARGET_ENTITY, target);
		}

		return builder.build();
	}
}
