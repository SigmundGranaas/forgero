package com.sigmundgranaas.forgero.predicate.codecs;

import java.util.function.Predicate;

import com.mojang.serialization.Codec;

public class CodecUtils {
	public static <T, P extends Predicate<T>> Codec<Predicate<T>> generalPredicate(Codec<P> specificCodec, Class<P> clazz) {
		return specificCodec.xmap(it -> it, clazz::cast);
	}
}
