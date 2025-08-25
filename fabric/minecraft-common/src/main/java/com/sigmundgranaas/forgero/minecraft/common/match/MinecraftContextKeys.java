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
	public static final ContextKey<Integer> STRAW_STAGE_HITS = ContextKey.of("straw_stage_hits", Integer.class);
	public static final ContextKey<Integer> BLUE_STAGE_HITS = ContextKey.of("blue_stage_hits", Integer.class);
	public static final ContextKey<Integer> BROWN_STAGE_HITS = ContextKey.of("brown_stage_hits", Integer.class);
	public static final ContextKey<Integer> GREY_STAGE_HITS = ContextKey.of("grey_stage_hits", Integer.class);
	public static final ContextKey<Integer> TOTAL_HITS = ContextKey.of("total_hits", Integer.class);

	// Accuracy and performance tracking
	public static final ContextKey<Integer> TOTAL_ATTEMPTS = ContextKey.of("total_attempts", Integer.class);
	public static final ContextKey<Integer> MISS_HITS = ContextKey.of("miss_hits", Integer.class);
	public static final ContextKey<Double> ACCURACY_RATE = ContextKey.of("accuracy_rate", Double.class);
}
