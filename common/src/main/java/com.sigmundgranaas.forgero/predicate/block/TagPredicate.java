package com.sigmundgranaas.forgero.predicate.block;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public record TagPredicate(List<TagKey<Block>> tags) implements Predicate<BlockState> {
	@Override
	public boolean test(BlockState blockState) {
		return !tags.isEmpty() && tags.stream().anyMatch(blockState::isIn);
	}

	public static final Codec<TagPredicate> CODEC = Codec.either(
			Identifier.CODEC,
			Codec.list(Identifier.CODEC)
	).xmap(
			TagPredicate::fromEither,
			TagPredicate::toEither
	);

	private static TagPredicate fromEither(Either<Identifier, List<Identifier>> either) {
		return new TagPredicate(either.map(
				id -> Collections.singletonList(identifierToTag(id)),
				TagPredicate::identifiersToTags
		));
	}

	private static Either<Identifier, List<Identifier>> toEither(TagPredicate predicate) {
		List<Identifier> ids = tagsToIdentifiers(predicate.tags);
		return ids.size() == 1 ? Either.left(ids.get(0)) : Either.right(ids);
	}

	private static TagKey<Block> identifierToTag(Identifier id) {
		return TagKey.of(Registries.BLOCK.getKey(), id);
	}

	private static Identifier tagToIdentifier(TagKey<Block> tag) {
		return tag.id();
	}

	private static List<TagKey<Block>> identifiersToTags(List<Identifier> identifiers) {
		return identifiers.stream().map(TagPredicate::identifierToTag).toList();
	}

	private static List<Identifier> tagsToIdentifiers(List<TagKey<Block>> blocks) {
		return blocks.stream().map(TagPredicate::tagToIdentifier).toList();
	}
}
