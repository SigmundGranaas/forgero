package com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.filter;

import com.google.gson.JsonElement;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

@FunctionalInterface
public interface BlockFilter {
	static BlockFilter fromJson(JsonElement json) {
		return ((entity, currentPos, root) -> true);
	}

	boolean filter(Entity entity, BlockPos currentPos, BlockPos root);
}
