package com.sigmundgranaas.forgero.minecraft.common.predicate;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

public class KeyCodec<T> implements Codec<KeyPair<T>> {
	private final String key;
	private final Codec<KeyPair<T>> codec;

	public KeyCodec(String key, Codec<KeyPair<T>> codec) {
		this.key = key;
		this.codec = codec;
	}

	public Codec<KeyPair<T>> codec() {
		return codec;
	}

	public String key() {
		return key;
	}

	public static <T> Codec<KeyPair<T>> codec(String key, Codec<KeyPair<T>> codec) {
		return new KeyCodec<>(key, codec);
	}

	@Override
	public <T1> DataResult<Pair<KeyPair<T>, T1>> decode(DynamicOps<T1> ops, T1 input) {
		return codec.decode(ops, input);
	}

	@Override
	public <T1> DataResult<T1> encode(KeyPair<T> input, DynamicOps<T1> ops, T1 prefix) {
		return codec.encode(input, ops, prefix);
	}
}
