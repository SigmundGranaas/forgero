package com.sigmundgranaas.forgero.handler.blockbreak.filter;

import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public class SameBlockFilter implements BlockFilter {
	public static final SameBlockFilter DEFAULT = new SameBlockFilter();

	public static final String TYPE = "forgero:same_block";

	public final static ClassKey<SameBlockFilter> KEY = new ClassKey<>(TYPE, SameBlockFilter.class);

	public final static JsonBuilder<SameBlockFilter> BUILDER = HandlerBuilder.fromStringOrType(KEY.clazz(), TYPE, DEFAULT);

	@Override
	public boolean filter(Entity entity, BlockPos currentPos, BlockPos root) {
		return entity.getWorld().getBlockState(currentPos).getBlock().equals(entity.getWorld().getBlockState(root).getBlock());
	}
}
