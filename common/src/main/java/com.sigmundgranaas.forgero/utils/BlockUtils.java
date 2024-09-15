package com.sigmundgranaas.forgero.utils;

import java.util.function.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class BlockUtils {
	public static TagKey<Block> of(String id) {
		return TagKey.of(Registries.BLOCK.getKey(), new Identifier(id));
	}

	public static Predicate<BlockState> isIn(String id) {
		return state -> state.isIn(BlockUtils.of(id));
	}
}
