package com.sigmundgranaas.forgero.minecraft.common.predicate;

import java.util.function.Function;
import java.util.function.Predicate;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public class AdapterCodec<T, R> implements Codec<KeyPair<Predicate<T>>> {
	@Getter
	private final String key;
	private final Codec<Predicate<R>> originalCodec;
	private final Function<Predicate<R>, PredicateAdapter<T, R>> transformer;

	public AdapterCodec(String key, Codec<Predicate<R>> originalCodec, Function<Predicate<R>, PredicateAdapter<T, R>> transformer) {
		this.key = key;
		this.originalCodec = originalCodec;
		this.transformer = transformer;
	}

	@Override
	public <T1> DataResult<Pair<KeyPair<Predicate<T>>, T1>> decode(DynamicOps<T1> ops, T1 input) {
		return originalCodec.decode(ops, input)
				.map(pair -> pair.mapFirst(transformer).mapFirst(it -> KeyPair.pair(key, it)));
	}

	public static <T, R> Codec<KeyPair<Predicate<T>>> of(String key, Codec<Predicate<R>> originalCodec, Function<Predicate<R>, PredicateAdapter<T, R>> transformer) {
		return new AdapterCodec<>(key, originalCodec, transformer);
	}

	@Override
	public <T1> DataResult<T1> encode(KeyPair<Predicate<T>> input, DynamicOps<T1> ops, T1 prefix) {
		if (input.value() instanceof PredicateAdapter<?, ?>) {
			return originalCodec.encode(((PredicateAdapter<T, R>) input.value()).predicate(), ops, prefix);
		}
		return DataResult.error(() -> "Failed to adapt " + input.getClass().getName());
	}
}
