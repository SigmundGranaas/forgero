package com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.filter;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public class FilterWrapper implements BlockFilter {
	private final List<BlockFilter> filterList;

	public FilterWrapper(List<BlockFilter> filterList) {
		this.filterList = filterList;
	}

	@Override
	public boolean filter(Entity entity, BlockPos currentPos, BlockPos root) {
		return filterList.stream().allMatch(filter -> filter.filter(entity, currentPos, root));
	}
}
