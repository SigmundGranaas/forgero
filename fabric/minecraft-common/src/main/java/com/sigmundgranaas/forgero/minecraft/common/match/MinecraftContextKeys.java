package com.sigmundgranaas.forgero.minecraft.common.match;


import com.sigmundgranaas.forgero.core.util.match.ContextKey;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MinecraftContextKeys {
	public static final ContextKey<Entity> ENTITY = ContextKey.of("entity", Entity.class);
	public static final ContextKey<BlockPos> BLOCK_TARGET = ContextKey.of("block_target", BlockPos.class);
	public static final ContextKey<Entity> ENTITY_TARGET = ContextKey.of("entity_target", Entity.class);
	public static final ContextKey<World> WORLD = ContextKey.of("world", World.class);
	public static final ContextKey<ItemStack> STACK = ContextKey.of("stack", ItemStack.class);

	// Temperature-related context keys
	public static final ContextKey<Integer> RED_STAGE_HITS = ContextKey.of("red_stage_hits", Integer.class);
	public static final ContextKey<Integer> ORANGE_STAGE_HITS = ContextKey.of("orange_stage_hits", Integer.class);
	public static final ContextKey<Integer> YELLOW_STAGE_HITS = ContextKey.of("yellow_stage_hits", Integer.class);
	public static final ContextKey<Integer> PURPLE_STAGE_HITS = ContextKey.of("purple_stage_hits", Integer.class);
	public static final ContextKey<Integer> STARTING_STAGE_HITS = ContextKey.of("starting_stage_hits", Integer.class);
	public static final ContextKey<Integer> TOTAL_HITS = ContextKey.of("total_hits", Integer.class);
}
