package com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.selector;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.utility.codec.DispatchCodecUtils;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

import java.util.Set;

public interface BlockSelector {
	Set<BlockPos> select(BlockPos target, Entity source);

	String type();

	static Codec<? extends BlockSelector> getCodec(String type) {
		return switch (type) {
			case ColumnSelector.TYPE -> ColumnSelector.CODEC;
			case PatternSelector.TYPE -> PatternSelector.CODEC;
			case RadiusVeinSelector.TYPE -> RadiusVeinSelector.CODEC;
			case SingleSelector.TYPE -> SingleSelector.CODEC;
			default -> throw new IllegalArgumentException("Unknown BlockSelector type: " + type);
		};
	}

	Codec<BlockSelector> CODEC = DispatchCodecUtils.create(
			BlockSelector::getCodec,
			BlockSelector::type
	);
}
