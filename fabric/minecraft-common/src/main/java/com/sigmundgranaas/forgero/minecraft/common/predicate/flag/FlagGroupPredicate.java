package com.sigmundgranaas.forgero.minecraft.common.predicate.flag;

import java.util.List;
import java.util.function.Predicate;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.minecraft.common.predicate.KeyPair;
import com.sigmundgranaas.forgero.minecraft.common.predicate.entity.EntityRegistries;

import net.minecraft.entity.Entity;

public record FlagGroupPredicate<T>(List<FlagPredicateInstance<T>> entries) implements Predicate<T> {
	public static final String KEY = "flag";

	public static final FlagGroupCodec<Entity> ENTIY_FLAG_CODEC = new FlagGroupCodec<>(EntityRegistries.ENTITY_FLAG_PREDICATE_REGISTRY, KEY);

	public static final Codec<KeyPair<Predicate<Entity>>> CODEC_SPECIFICATION = ENTIY_FLAG_CODEC.asPredicateCodec();

	@Override
	public boolean test(T test) {
		return entries.stream().allMatch(entry -> entry.test(test));
	}
}
