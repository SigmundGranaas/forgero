package com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.filter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public record FilterWrapper(List<BlockFilter> filters) implements BlockFilter {
	public static final String TYPE = "forgero:filter_wrapper";

	public static final Codec<FilterWrapper> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.list(BlockFilter.CODEC).fieldOf("filters").forGetter(FilterWrapper::filters)
	).apply(instance, FilterWrapper::new));

	@Override
	public boolean filter(Entity entity, BlockPos currentPos, BlockPos root) {
		return filters.stream().allMatch(filter -> filter.filter(entity, currentPos, root));
	}

	@Override
	public String type() {
		return TYPE;
	}
}
