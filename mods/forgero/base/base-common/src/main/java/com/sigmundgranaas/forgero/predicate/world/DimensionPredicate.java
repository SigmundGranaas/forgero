package com.sigmundgranaas.forgero.predicate.world;

import java.util.function.Predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.predicate.codecs.KeyPair;
import com.sigmundgranaas.forgero.predicate.codecs.KeyPairCodec;

import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public record DimensionPredicate(Identifier dimension, boolean isIn) implements Predicate<World> {
	public static final String KEY = "dimension";

	public static final Codec<DimensionPredicate> CODEC = RecordCodecBuilder
			.create(instance -> instance.group(
							Identifier.CODEC.fieldOf("dimension").forGetter(DimensionPredicate::dimension),
							Codec.BOOL.fieldOf("is_in").orElse(true).forGetter(DimensionPredicate::isIn)
					)
					.apply(instance, DimensionPredicate::new));

	public static Codec<KeyPair<Predicate<World>>> KEY_PAIR_CODEC =
			KeyPairCodec.of(
					KEY,
					CODEC.xmap(
							dimensionPredicate -> dimensionPredicate,
							predicate -> (DimensionPredicate) predicate
					)
			);


	@Override
	public boolean test(World world) {
		boolean isInDimension = world.getRegistryKey().getValue().equals(dimension);

		if (isInDimension && isIn()) {
			return true;
		} else if (!isInDimension && !isIn()) {
			return true;
		} else {
			return false;
		}
	}
}
