package com.sigmundgranaas.forgero.minecraft.common.predicate.error;

import java.util.function.Predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.sigmundgranaas.forgero.minecraft.common.predicate.codecs.KeyPair;
import com.sigmundgranaas.forgero.minecraft.common.predicate.codecs.SpecificationRegistry;


public interface PredicateErrorHandler {
	<T> boolean canHandle(String key, Object value, SpecificationRegistry<Codec<KeyPair<Predicate<T>>>> codecs);

	<R, T> String createWarningMessage(DynamicOps<R> ops, MapLike<R> input, String key, R value, SpecificationRegistry<Codec<KeyPair<Predicate<T>>>> codecs);
}
