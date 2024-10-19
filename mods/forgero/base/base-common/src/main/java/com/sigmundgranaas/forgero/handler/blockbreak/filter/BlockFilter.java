package com.sigmundgranaas.forgero.handler.blockbreak.filter;

import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

@FunctionalInterface
public interface BlockFilter {
	ClassKey<BlockFilter> KEY = new ClassKey<>("forgero:filter", BlockFilter.class);
	BlockFilter DEFAULT = (source, current, root) -> true;


	boolean filter(Entity entity, BlockPos currentPos, BlockPos root);
}
