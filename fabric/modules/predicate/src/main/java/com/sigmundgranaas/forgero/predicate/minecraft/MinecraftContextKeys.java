package com.sigmundgranaas.forgero.predicate.minecraft;

import com.sigmundgranaas.forgero.core.property.context.Key;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MinecraftContextKeys {
	// The entity performing an action or holding the tool
	public static final Key<Entity> ENTITY = new Key<>(new OpenIdentifier("forgero-mc", "entity"));

	// The entity being targeted (e.g., in an attack)
	public static final Key<Entity> TARGET_ENTITY = new Key<>(new OpenIdentifier("forgero-mc", "target_entity"));

	// The block being targeted (e.g., being broken)
	public static final Key<BlockPos> TARGET_BLOCK_POS = new Key<>(new OpenIdentifier("forgero-mc", "target_block_pos"));

	// The world where the event is happening
	public static final Key<World> WORLD = new Key<>(new OpenIdentifier("forgero-mc", "world"));
}
