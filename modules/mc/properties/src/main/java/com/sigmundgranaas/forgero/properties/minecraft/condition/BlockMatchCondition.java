package com.sigmundgranaas.forgero.properties.minecraft.condition;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.List;

/**
 * A dynamic condition that checks if a block matches one of the specified blocks.
 * Checks the block at BLOCK_POS in the world context.
 *
 * Example JSON:
 * <pre>
 * {
 *   "type": "forgero:block_match",
 *   "blocks": "minecraft:stone"
 * }
 * </pre>
 * or with multiple blocks:
 * <pre>
 * {
 *   "type": "forgero:block_match",
 *   "blocks": ["minecraft:stone", "minecraft:granite", "minecraft:diorite"]
 * }
 * </pre>
 */
public record BlockMatchCondition(OpenIdentifier type, List<Block> blocks) implements DynamicCondition {
	public static final Codec<BlockMatchCondition> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(BlockMatchCondition::type),
					Codec.either(Identifier.CODEC, Codec.list(Identifier.CODEC))
							.xmap(BlockMatchCondition::fromEither, BlockMatchCondition::toEither)
							.fieldOf("blocks").forGetter(BlockMatchCondition::blocks)
			).apply(instance, BlockMatchCondition::new));

	public BlockMatchCondition {
		// Validate that the list is not empty
		if (blocks.isEmpty()) {
			throw new IllegalArgumentException("Block list cannot be empty for BlockMatchCondition");
		}
	}

	@Override
	public boolean test(DynamicContext context) {
		if (blocks.isEmpty()) {
			return false;
		}

		return context.get(MinecraftContextKeys.WORLD)
				.flatMap(world -> context.get(MinecraftContextKeys.BLOCK_POS)
						.map(pos -> {
							BlockState state = world.getBlockState(pos);
							return blocks.contains(state.getBlock());
						}))
				.orElse(false);
	}

	private static List<Block> fromEither(Either<Identifier, List<Identifier>> either) {
		return either.map(
				id -> Collections.singletonList(identifierToBlock(id)),
				BlockMatchCondition::identifiersToBlocks
		);
	}

	private static Either<Identifier, List<Identifier>> toEither(List<Block> blocks) {
		List<Identifier> ids = blocksToIdentifiers(blocks);
		return ids.size() == 1 ? Either.left(ids.get(0)) : Either.right(ids);
	}

	private static Block identifierToBlock(Identifier id) {
		return Registries.BLOCK.get(id);
	}

	private static Identifier blockToIdentifier(Block block) {
		return Registries.BLOCK.getId(block);
	}

	private static List<Block> identifiersToBlocks(List<Identifier> identifiers) {
		return identifiers.stream().map(BlockMatchCondition::identifierToBlock).toList();
	}

	private static List<Identifier> blocksToIdentifiers(List<Block> blocks) {
		return blocks.stream().map(BlockMatchCondition::blockToIdentifier).toList();
	}
}
