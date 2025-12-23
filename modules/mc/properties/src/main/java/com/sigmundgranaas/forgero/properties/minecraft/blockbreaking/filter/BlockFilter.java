package com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.filter;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.utility.codec.DispatchCodecUtils;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public interface BlockFilter {
	boolean filter(Entity entity, BlockPos currentPos, BlockPos root);

	String type();

	static Codec<? extends BlockFilter> getCodec(String type) {
		return switch (type) {
			case CanMineFilter.TYPE -> CanMineFilter.CODEC;
			case FilterWrapper.TYPE -> FilterWrapper.CODEC;
			case IsBlockFilter.TYPE -> IsBlockFilter.CODEC;
			case SameBlockFilter.TYPE -> SameBlockFilter.CODEC;
			case SimilarBlockFilter.TYPE -> SimilarBlockFilter.CODEC;
			case BlockTagFilter.TYPE -> BlockTagFilter.CODEC;
			default -> throw new IllegalArgumentException("Unknown BlockFilter type: " + type);
		};
	}

	Codec<BlockFilter> CODEC = DispatchCodecUtils.create(
			BlockFilter::getCodec,
			BlockFilter::type
	);
}
