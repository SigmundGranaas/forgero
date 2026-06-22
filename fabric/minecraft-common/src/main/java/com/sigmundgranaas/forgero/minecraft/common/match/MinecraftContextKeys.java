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
	public static final ContextKey<Integer> WORKABLE_STAGE_HITS = ContextKey.of("workable_stage_hits", Integer.class);
	public static final ContextKey<Integer> OVERHEATED_STAGE_HITS = ContextKey.of("overheated_stage_hits", Integer.class);

	public static final ContextKey<Integer> TOTAL_HITS = ContextKey.of("total_hits", Integer.class);
	public static final ContextKey<Integer> MISS_HITS = ContextKey.of("miss_hits", Integer.class);
	public static final ContextKey<Integer> COOLING_MARKER_HITS = ContextKey.of("cooling_marker_hits", Integer.class);
	public static final ContextKey<Integer> QUENCH_COUNT = ContextKey.of("quench_count", Integer.class);
	public static final ContextKey<Integer> REHEAT_COUNT = ContextKey.of("reheat_count", Integer.class);

	public static final ContextKey<Integer> POOR_STRIKES = ContextKey.of("poor_strikes", Integer.class);
	public static final ContextKey<Integer> GOOD_STRIKES = ContextKey.of("good_strikes", Integer.class);
	public static final ContextKey<Integer> PERFECT_STRIKES = ContextKey.of("perfect_strikes", Integer.class);
	public static final ContextKey<Integer> POOR_COOLING_STRIKES = ContextKey.of("poor_cooling_strikes", Integer.class);
	public static final ContextKey<Integer> GOOD_COOLING_STRIKES = ContextKey.of("good_cooling_strikes", Integer.class);
	public static final ContextKey<Integer> PERFECT_COOLING_STRIKES = ContextKey.of("perfect_cooling_strikes", Integer.class);
	public static final ContextKey<Integer> PERFECT_STRIKE_STREAK = ContextKey.of("perfect_strike_streak", Integer.class);
	public static final ContextKey<Integer> SKILLED_STRIKE_STREAK = ContextKey.of("skilled_strike_streak", Integer.class);

	public static final ContextKey<Integer> COOLING_COLD_STAGE_HITS = ContextKey.of("cooling_cold_stage_hits", Integer.class);
	public static final ContextKey<Integer> COOLING_WARM_STAGE_HITS = ContextKey.of("cooling_warm_stage_hits", Integer.class);
	public static final ContextKey<Integer> COOLING_HOT_STAGE_HITS = ContextKey.of("cooling_hot_stage_hits", Integer.class);
	public static final ContextKey<Integer> COOLING_WORKABLE_STAGE_HITS = ContextKey.of("cooling_workable_stage_hits", Integer.class);
	public static final ContextKey<Integer> COOLING_OVERHEATED_STAGE_HITS = ContextKey.of("cooling_overheated_stage_hits", Integer.class);

	public static final ContextKey<Integer> TOTAL_QUENCH_SESSIONS = ContextKey.of("total_quench_sessions", Integer.class);
	public static final ContextKey<Integer> IN_PROGRESS_QUENCH_SESSIONS = ContextKey.of("in_progress_quench_sessions", Integer.class);
	public static final ContextKey<Integer> FINAL_QUENCH_SESSIONS = ContextKey.of("final_quench_sessions", Integer.class);
	public static final ContextKey<Integer> FINAL_QUENCH_START_TEMPERATURE = ContextKey.of("final_quench_start_temperature", Integer.class);
	public static final ContextKey<Integer> FINAL_QUENCH_START_STAGE = ContextKey.of("final_quench_start_stage", Integer.class);
	public static final ContextKey<Integer> LAST_QUENCH_START_TEMPERATURE = ContextKey.of("last_quench_start_temperature", Integer.class);
	public static final ContextKey<Integer> LAST_QUENCH_START_STAGE = ContextKey.of("last_quench_start_stage", Integer.class);
	public static final ContextKey<Boolean> FINAL_QUENCH_COMPLETED_IN_ONE_GO = ContextKey.of("final_quench_completed_in_one_go", Boolean.class);
	public static final ContextKey<Boolean> QUENCHED_DURING_SMITHING = ContextKey.of("quenched_during_smithing", Boolean.class);

	// Fractions (0.0 - 1.0) derived from temperature-classified hammer strikes.
	public static final ContextKey<Double> COLD_STAGE_FRACTION = ContextKey.of("cold_stage_fraction", Double.class);
	public static final ContextKey<Double> WARM_STAGE_FRACTION = ContextKey.of("warm_stage_fraction", Double.class);
	public static final ContextKey<Double> HOT_STAGE_FRACTION = ContextKey.of("hot_stage_fraction", Double.class);
	public static final ContextKey<Double> WORKABLE_STAGE_FRACTION = ContextKey.of("workable_stage_fraction", Double.class);
	public static final ContextKey<Double> OVERHEATED_STAGE_FRACTION = ContextKey.of("overheated_stage_fraction", Double.class);
	public static final ContextKey<Double> POOR_STRIKE_FRACTION = ContextKey.of("poor_strike_fraction", Double.class);
	public static final ContextKey<Double> GOOD_STRIKE_FRACTION = ContextKey.of("good_strike_fraction", Double.class);
	public static final ContextKey<Double> PERFECT_STRIKE_FRACTION = ContextKey.of("perfect_strike_fraction", Double.class);

	// Stage transitions
	public static final ContextKey<int[]> STAGE_TRANSITION_MATRIX = ContextKey.of("stage_transition_matrix", int[].class); // 5x5 flattened
	public static final ContextKey<int[]> STAGE_CHANGE_SEQUENCE = ContextKey.of("stage_change_sequence", int[].class);
	public static final ContextKey<int[]> COOLING_STAGE_HIT_COUNTS = ContextKey.of("cooling_stage_hit_counts", int[].class);
	public static final ContextKey<int[]> QUENCH_START_TEMPERATURES = ContextKey.of("quench_start_temperatures", int[].class);
	public static final ContextKey<int[]> QUENCH_START_STAGES = ContextKey.of("quench_start_stages", int[].class);
	public static final ContextKey<int[]> QUENCH_CONTEXT_SEQUENCE = ContextKey.of("quench_context_sequence", int[].class);
}
