package com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.selector;

import java.util.Set;

import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;

import net.minecraft.util.math.BlockPos;

@FunctionalInterface
public interface BlockSelector {

	ClassKey<BlockSelector> KEY = new ClassKey<>("forgero:block_selector", BlockSelector.class);

	BlockSelector DEFAULT = Set::of;

	Set<BlockPos> select(BlockPos target);
}
