package com.sigmundgranaas.forgero.properties.minecraft.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

/**
 * A dynamic condition that checks the biome at a position.
 * Supports both direct biome ID matching and tag-based matching (prefix with #).
 *
 * Examples:
 * - "minecraft:plains" - matches the plains biome
 * - "#minecraft:is_forest" - matches any biome with the is_forest tag
 */
public record BiomeCondition(OpenIdentifier type, String biome) implements DynamicCondition {
	public static final Codec<BiomeCondition> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(BiomeCondition::type),
					Codec.STRING.fieldOf("biome").forGetter(BiomeCondition::biome)
			).apply(instance, BiomeCondition::new));

	@Override
	public boolean test(DynamicContext context) {
		return context.get(MinecraftContextKeys.WORLD)
				.flatMap(world -> context.get(MinecraftContextKeys.BLOCK_POS)
						.map(pos -> testBiome(world, pos)))
				.orElse(false);
	}

	private boolean testBiome(World world, BlockPos pos) {
		try {
			if (biome.contains(":") && !biome.startsWith("#")) {
				// Direct ID match: "minecraft:plains"
				return testId(world, pos, new Identifier(biome));
			} else if (biome.startsWith("#")) {
				// Tag match: "#minecraft:is_forest"
				String tagId = biome.substring(1);
				if (tagId.contains(":")) {
					return testTag(world, pos, TagKey.of(RegistryKeys.BIOME, new Identifier(tagId)));
				}
			}
		} catch (Exception e) {
			// Invalid identifier format - return false rather than crashing
			return false;
		}
		return false;
	}

	private boolean testTag(World world, BlockPos pos, TagKey<Biome> tag) {
		return world.getBiome(pos).isIn(tag);
	}

	private boolean testId(World world, BlockPos pos, Identifier biomeId) {
		return world.getBiome(pos).matchesId(biomeId);
	}
}
