package com.sigmundgranaas.forgero.predicate.minecraft.block;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;

/**
 * Predicate for the type of a block, checking against specific blocks or tags.
 */
public record BlockTypePredicate(
		Optional<List<Block>> block,
		Optional<List<TagKey<Block>>> tag
) {
	private static final Codec<List<Block>> BLOCK_LIST_CODEC = Codec.either(Identifier.CODEC, Codec.list(Identifier.CODEC))
			.xmap(
					either -> either.map(List::of, list -> list).stream().map(Registries.BLOCK::get).toList(),
					blocks -> blocks.size() == 1 ? Either.left(Registries.BLOCK.getId(blocks.get(0))) : Either.right(blocks.stream().map(Registries.BLOCK::getId).toList())
			);

	private static final Codec<List<TagKey<Block>>> TAG_LIST_CODEC = Codec.either(Identifier.CODEC, Codec.list(Identifier.CODEC))
			.xmap(
					either -> either.map(List::of, list -> list).stream().map(id -> TagKey.of(Registries.BLOCK.getKey(), id)).toList(),
					tags -> tags.size() == 1 ? Either.left(tags.get(0).id()) : Either.right(tags.stream().map(TagKey::id).toList())
			);

	public static final Codec<BlockTypePredicate> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					BLOCK_LIST_CODEC.optionalFieldOf("block").forGetter(BlockTypePredicate::block),
					TAG_LIST_CODEC.optionalFieldOf("tag").forGetter(BlockTypePredicate::tag)
			).apply(instance, BlockTypePredicate::new)
	);

	public boolean test(BlockState state) {
		boolean blockMatch = block.map(blocks -> blocks.contains(state.getBlock())).orElse(true);
		boolean tagMatch = tag.map(tags -> tags.stream().anyMatch(state::isIn)).orElse(true);
		return blockMatch && tagMatch;
	}
}
