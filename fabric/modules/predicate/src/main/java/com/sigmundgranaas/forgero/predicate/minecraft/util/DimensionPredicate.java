package com.sigmundgranaas.forgero.predicate.minecraft.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.List;

/**
 * A predicate for checking the dimension of a location.
 * Can check against a single dimension ID or a list of them.
 *
 * <p><h3>Example:</h3>
 * {@code "dimension": "minecraft:the_nether"}
 */
public record DimensionPredicate(List<Identifier> dimensions) {

	private static final Codec<List<Identifier>> DIMENSION_LIST_CODEC = Codec.either(Identifier.CODEC, Codec.list(Identifier.CODEC))
			.xmap(
					either -> either.map(List::of, list -> list),
					list -> list.size() == 1 ? Either.left(list.get(0)) : Either.right(list)
			);

	public static final Codec<DimensionPredicate> CODEC = DIMENSION_LIST_CODEC.xmap(DimensionPredicate::new, DimensionPredicate::dimensions);

	public boolean test(RegistryKey<World> worldKey) {
		return dimensions.contains(worldKey.getValue());
	}
}
