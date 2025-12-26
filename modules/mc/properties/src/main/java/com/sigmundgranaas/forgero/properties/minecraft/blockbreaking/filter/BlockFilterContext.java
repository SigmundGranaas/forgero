package com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.filter;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

/**
 * Context for block filtering operations.
 * <p>
 * Contains the entity performing the action and the root block position
 * (e.g., the originally targeted block in a vein mining operation).
 *
 * @param entity The entity performing the block operation
 * @param root   The root/origin block position for the operation
 */
public record BlockFilterContext(Entity entity, BlockPos root) {

	/**
	 * Creates a context with only an entity (no root position).
	 *
	 * @param entity The entity performing the operation
	 * @return A new context with null root
	 */
	public static BlockFilterContext of(Entity entity) {
		return new BlockFilterContext(entity, null);
	}

	/**
	 * Creates a context with entity and root position.
	 *
	 * @param entity The entity performing the operation
	 * @param root   The root block position
	 * @return A new context
	 */
	public static BlockFilterContext of(Entity entity, BlockPos root) {
		return new BlockFilterContext(entity, root);
	}
}
