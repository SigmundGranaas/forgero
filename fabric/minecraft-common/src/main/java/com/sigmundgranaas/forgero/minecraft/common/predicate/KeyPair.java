package com.sigmundgranaas.forgero.minecraft.common.predicate;

import com.sigmundgranaas.forgero.minecraft.common.predicate.flag.PredicatePair;

public interface KeyPair<T> {
	String key();

	T value();

	static <T> KeyPair<Predicate<T>> predicate(String key, Predicate<T> specification) {
		return new PredicatePair<>(key, specification);
	}

	static <T> KeyPair<T> pair(String key, T specification) {
		return new KeyPair<T>() {
			@Override
			public String key() {
				return key;
			}

			@Override
			public T value() {
				return specification;
			}
		};
	}
}
