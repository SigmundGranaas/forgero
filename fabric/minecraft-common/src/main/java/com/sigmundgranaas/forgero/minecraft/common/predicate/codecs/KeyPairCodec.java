package com.sigmundgranaas.forgero.minecraft.common.predicate.codecs;

import java.util.function.Predicate;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public class KeyPairCodec<T> implements Codec<KeyPair<Predicate<T>>> {
	@Getter
	private final String key;
	private final Codec<Predicate<T>> originalCodec;


	public KeyPairCodec(String key, Codec<Predicate<T>> originalCodec) {
		this.key = key;
		this.originalCodec = originalCodec;
	}

	@Override
	public <T1> DataResult<Pair<KeyPair<Predicate<T>>, T1>> decode(DynamicOps<T1> ops, T1 input) {
		return originalCodec.decode(ops, input)
				.map(pair -> pair.mapFirst(it -> KeyPair.pair(key, it)));
	}

	public static <T> Codec<KeyPair<Predicate<T>>> of(String key, Codec<Predicate<T>> originalCodec) {
		return new KeyPairCodec<>(key, originalCodec);
	}

	@Override
	public <T1> DataResult<T1> encode(KeyPair<Predicate<T>> input, DynamicOps<T1> ops, T1 prefix) {
		if (input.value() instanceof KeyPair<?>) {
			return originalCodec.encode(input.value(), ops, prefix);
		}
		return DataResult.error(() -> "Failed to adapt " + input.getClass().getName());
	}
}
