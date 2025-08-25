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

	public static final ContextKey<Integer> OVERHEATED_STAGE_HITS = ContextKey.of("overheated_stage_hits", Integer.class);
	public static final ContextKey<Integer> WELDING_STAGE_HITS = ContextKey.of("welding_stage_hits", Integer.class);
	public static final ContextKey<Integer> FORGING_STAGE_HITS = ContextKey.of("forging_stage_hits", Integer.class);
	public static final ContextKey<Integer> SHAPING_STAGE_HITS = ContextKey.of("shaping_stage_hits", Integer.class);
	public static final ContextKey<Integer> CRITICAL_STAGE_HITS = ContextKey.of("critical_stage_hits", Integer.class);
	public static final ContextKey<Integer> TEMPERING_STAGE_HITS = ContextKey.of("tempering_stage_hits", Integer.class);
	public static final ContextKey<Integer> COLD_STAGE_HITS = ContextKey.of("cold_stage_hits", Integer.class);
	public static final ContextKey<Integer> PERFECT_STAGE_HITS = ContextKey.of("perfect_stage_hits", Integer.class);

	public static final ContextKey<Integer> TOTAL_HITS = ContextKey.of("total_hits", Integer.class);
	public static final ContextKey<Integer> MISS_HITS = ContextKey.of("miss_hits", Integer.class);
	public static final ContextKey<Integer> FAST_MARKER_HITS = ContextKey.of("fast_marker_hits", Integer.class);
}
