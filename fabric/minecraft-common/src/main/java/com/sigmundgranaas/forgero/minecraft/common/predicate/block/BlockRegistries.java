package com.sigmundgranaas.forgero.minecraft.common.predicate.block;

import java.util.function.Predicate;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.minecraft.common.predicate.KeyPair;
import com.sigmundgranaas.forgero.minecraft.common.predicate.SpecificationRegistry;

public class BlockRegistries {
	public static final SpecificationRegistry<Codec<KeyPair<Predicate<WorldBlockPair>>>> BLOCK_CODEC_REGISTRY = new SpecificationRegistry<>();
}
