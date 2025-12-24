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
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.List;

/**
 * A dynamic condition that checks if a block has one of the specified tags.
 * Checks the block at BLOCK_POS in the world context.
 *
 * Example JSON:
 * <pre>
 * {
 *   "type": "forgero:block_tag",
 *   "tags": "minecraft:logs"
 * }
 * </pre>
 * or with multiple tags:
 * <pre>
 * {
 *   "type": "forgero:block_tag",
 *   "tags": ["minecraft:logs", "minecraft:planks"]
 * }
 * </pre>
 */
public record BlockTagCondition(OpenIdentifier type, List<TagKey<Block>> tags) implements DynamicCondition {
	public static final Codec<BlockTagCondition> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(BlockTagCondition::type),
					Codec.either(Identifier.CODEC, Codec.list(Identifier.CODEC))
							.xmap(BlockTagCondition::fromEither, BlockTagCondition::toEither)
							.fieldOf("tags").forGetter(BlockTagCondition::tags)
			).apply(instance, BlockTagCondition::new));

	public BlockTagCondition {
		// Validate that the list is not empty
		if (tags.isEmpty()) {
			throw new IllegalArgumentException("Tag list cannot be empty for BlockTagCondition");
		}
	}

	@Override
	public boolean test(DynamicContext context) {
		if (tags.isEmpty()) {
			return false;
		}

		return context.get(MinecraftContextKeys.WORLD)
				.flatMap(world -> context.get(MinecraftContextKeys.BLOCK_POS)
						.map(pos -> {
							BlockState state = world.getBlockState(pos);
							return tags.stream().anyMatch(state::isIn);
						}))
				.orElse(false);
	}

	private static List<TagKey<Block>> fromEither(Either<Identifier, List<Identifier>> either) {
		return either.map(
				id -> Collections.singletonList(identifierToTag(id)),
				BlockTagCondition::identifiersToTags
		);
	}

	private static Either<Identifier, List<Identifier>> toEither(List<TagKey<Block>> tags) {
		List<Identifier> ids = tagsToIdentifiers(tags);
		return ids.size() == 1 ? Either.left(ids.get(0)) : Either.right(ids);
	}

	private static TagKey<Block> identifierToTag(Identifier id) {
		return TagKey.of(Registries.BLOCK.getKey(), id);
	}

	private static Identifier tagToIdentifier(TagKey<Block> tag) {
		return tag.id();
	}

	private static List<TagKey<Block>> identifiersToTags(List<Identifier> identifiers) {
		return identifiers.stream().map(BlockTagCondition::identifierToTag).toList();
	}

	private static List<Identifier> tagsToIdentifiers(List<TagKey<Block>> tags) {
		return tags.stream().map(BlockTagCondition::tagToIdentifier).toList();
	}
}
