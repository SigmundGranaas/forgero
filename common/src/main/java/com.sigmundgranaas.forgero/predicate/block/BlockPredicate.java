package com.sigmundgranaas.forgero.predicate.block;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public record BlockPredicate(List<Block> blocks) implements Predicate<Block> {
	@Override
	public boolean test(Block block) {
		return !blocks.isEmpty() && blocks.contains(block);
	}

	private static BlockPredicate fromEither(Either<Identifier, List<Identifier>> either) {
		return new BlockPredicate(either.map(
				id -> Collections.singletonList(identifierToBlock(id)),
				BlockPredicate::identifiersToBlocks
		));
	}

	private static Either<Identifier, List<Identifier>> toEither(BlockPredicate predicate) {
		List<Identifier> ids = blocksToIdentifiers(predicate.blocks);
		return ids.size() == 1 ? Either.left(ids.get(0)) : Either.right(ids);
	}

	public static final Codec<BlockPredicate> CODEC = Codec.either(
			Identifier.CODEC,
			Codec.list(Identifier.CODEC)
	).xmap(
			BlockPredicate::fromEither,
			BlockPredicate::toEither
	);

	private static Block identifierToBlock(Identifier id) {
		return Registries.BLOCK.get(id);
	}

	private static Identifier blockToIdentifier(Block block) {
		return Registries.BLOCK.getId(block);
	}

	private static List<Block> identifiersToBlocks(List<Identifier> identifiers) {
		return identifiers.stream().map(BlockPredicate::identifierToBlock).toList();
	}

	private static List<Identifier> blocksToIdentifiers(List<Block> blocks) {
		return blocks.stream().map(BlockPredicate::blockToIdentifier).toList();
	}
}
