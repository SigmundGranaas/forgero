package com.sigmundgranaas.forgero.minecraft.common.predicate.block;

import static com.sigmundgranaas.forgero.minecraft.common.predicate.CodecUtils.generalPredicate;

import java.util.function.Function;
import java.util.function.Predicate;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.minecraft.common.predicate.AdapterCodec;
import com.sigmundgranaas.forgero.minecraft.common.predicate.KeyPair;
import com.sigmundgranaas.forgero.minecraft.common.predicate.PredicateAdapter;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

public class Adapters {
	static Function<WorldBlockPair, BlockState> state = WorldBlockPair::state;
	static Function<WorldBlockPair, Block> block = WorldBlockPair::block;

	public static String TAGS_KEY = "tags";
	public static String TAG_KEY = "tag";
	public static String BLOCKS_KEY = "blocks";
	public static String BLOCK_KEY = "block";

	public static Codec<KeyPair<Predicate<WorldBlockPair>>> blockTagsAdapter() {
		return AdapterCodec.of(TAGS_KEY, generalPredicate(TagPredicate.CODEC, TagPredicate.class), PredicateAdapter.create(Adapters.state));
	}

	public static Codec<KeyPair<Predicate<WorldBlockPair>>> blockTagAdapter() {
		return AdapterCodec.of(TAG_KEY, generalPredicate(TagPredicate.CODEC, TagPredicate.class), PredicateAdapter.create(Adapters.state));
	}

	public static Codec<KeyPair<Predicate<WorldBlockPair>>> blocksAdapter() {
		return AdapterCodec.of(BLOCKS_KEY, generalPredicate(BlockPredicate.CODEC, BlockPredicate.class), PredicateAdapter.create(Adapters.block));
	}

	public static Codec<KeyPair<Predicate<WorldBlockPair>>> blockAdapter() {
		return AdapterCodec.of(BLOCK_KEY, generalPredicate(BlockPredicate.CODEC, BlockPredicate.class), PredicateAdapter.create(Adapters.block));
	}
}
