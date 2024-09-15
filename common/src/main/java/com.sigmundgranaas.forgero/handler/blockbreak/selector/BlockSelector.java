package com.sigmundgranaas.forgero.handler.blockbreak.selector;

import java.util.Set;

import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

@FunctionalInterface
public interface BlockSelector {

	ClassKey<BlockSelector> KEY = new ClassKey<>("forgero:block_selector", BlockSelector.class);

	BlockSelector DEFAULT = SingleSelector.DEFAULT;

	Set<BlockPos> select(BlockPos target, Entity source);
}
