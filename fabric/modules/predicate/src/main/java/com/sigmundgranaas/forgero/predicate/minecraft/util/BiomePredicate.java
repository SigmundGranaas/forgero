package com.sigmundgranaas.forgero.predicate.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

import java.util.Optional;

/**
 * A predicate for checking the biome of a location.
 *
 * <p><h3>Examples:</h3>
 * {@code "biome": { "is": "minecraft:plains" }}
 * <br>
 * {@code "biome": { "in_tag": "minecraft:is_jungle" }}
 */
public record BiomePredicate(
		Optional<RegistryKey<Biome>> is,
		Optional<TagKey<Biome>> in_tag
) {
	private static final Codec<RegistryKey<Biome>> BIOME_KEY_CODEC = Identifier.CODEC.xmap(
			id -> RegistryKey.of(RegistryKeys.BIOME, id),
			RegistryKey::getValue
	);

	private static final Codec<TagKey<Biome>> BIOME_TAG_CODEC = Identifier.CODEC.xmap(
			id -> TagKey.of(RegistryKeys.BIOME, id),
			TagKey::id
	);

	public static final Codec<BiomePredicate> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					BIOME_KEY_CODEC.optionalFieldOf("is").forGetter(BiomePredicate::is),
					BIOME_TAG_CODEC.optionalFieldOf("in_tag").forGetter(BiomePredicate::in_tag)
			).apply(instance, BiomePredicate::new)
	);

	public boolean test(RegistryEntry<Biome> biomeEntry) {
		boolean biomeMatch = is.map(biomeEntry::matchesKey).orElse(true);
		boolean tagMatch = in_tag.map(biomeEntry::isIn).orElse(true);
		return biomeMatch && tagMatch;
	}
}
