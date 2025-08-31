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

	public static final ContextKey<Integer> COLD_STAGE_HITS = ContextKey.of("cold_stage_hits", Integer.class);
	public static final ContextKey<Integer> WARM_STAGE_HITS = ContextKey.of("warm_stage_hits", Integer.class);
	public static final ContextKey<Integer> HOT_STAGE_HITS = ContextKey.of("hot_stage_hits", Integer.class);
	public static final ContextKey<Integer> VERY_HOT_STAGE_HITS = ContextKey.of("very_hot_stage_hits", Integer.class);
	public static final ContextKey<Integer> NEAR_MELT_STAGE_HITS = ContextKey.of("near_melt_stage_hits", Integer.class);
	public static final ContextKey<Integer> MOLTEN_STAGE_HITS = ContextKey.of("molten_stage_hits", Integer.class);

	public static final ContextKey<Integer> TOTAL_HITS = ContextKey.of("total_hits", Integer.class);
	public static final ContextKey<Integer> MISS_HITS = ContextKey.of("miss_hits", Integer.class);
	public static final ContextKey<Integer> FAST_MARKER_HITS = ContextKey.of("fast_marker_hits", Integer.class);

	// Continuous time (tick) tracking across the minigame lifetime
	public static final ContextKey<Integer> TOTAL_STAGE_TICKS = ContextKey.of("total_stage_ticks", Integer.class);
	public static final ContextKey<Integer> COLD_STAGE_TICKS = ContextKey.of("cold_stage_ticks", Integer.class);
	public static final ContextKey<Integer> WARM_STAGE_TICKS = ContextKey.of("warm_stage_ticks", Integer.class);
	public static final ContextKey<Integer> HOT_STAGE_TICKS = ContextKey.of("hot_stage_ticks", Integer.class);
	public static final ContextKey<Integer> VERY_HOT_STAGE_TICKS = ContextKey.of("very_hot_stage_ticks", Integer.class);
	public static final ContextKey<Integer> NEAR_MELT_STAGE_TICKS = ContextKey.of("near_melt_stage_ticks", Integer.class);
	public static final ContextKey<Integer> MOLTEN_STAGE_TICKS = ContextKey.of("molten_stage_ticks", Integer.class);

	// Fractions (0.0 - 1.0) derived from the above tick counts
	public static final ContextKey<Double> COLD_STAGE_FRACTION = ContextKey.of("cold_stage_fraction", Double.class);
	public static final ContextKey<Double> WARM_STAGE_FRACTION = ContextKey.of("warm_stage_fraction", Double.class);
	public static final ContextKey<Double> HOT_STAGE_FRACTION = ContextKey.of("hot_stage_fraction", Double.class);
	public static final ContextKey<Double> VERY_HOT_STAGE_FRACTION = ContextKey.of("very_hot_stage_fraction", Double.class);
	public static final ContextKey<Double> NEAR_MELT_STAGE_FRACTION = ContextKey.of("near_melt_stage_fraction", Double.class);
	public static final ContextKey<Double> MOLTEN_STAGE_FRACTION = ContextKey.of("molten_stage_fraction", Double.class);

	// Stage transitions
	public static final ContextKey<int[]> STAGE_TRANSITION_MATRIX = ContextKey.of("stage_transition_matrix", int[].class); // 6x6 flattened
	public static final ContextKey<int[]> STAGE_CHANGE_SEQUENCE = ContextKey.of("stage_change_sequence", int[].class);
}
