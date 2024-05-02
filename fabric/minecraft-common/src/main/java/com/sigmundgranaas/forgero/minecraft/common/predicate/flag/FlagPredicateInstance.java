package com.sigmundgranaas.forgero.minecraft.common.predicate.flag;

import java.util.function.Predicate;

import com.sigmundgranaas.forgero.minecraft.common.predicate.KeyPair;

public record FlagPredicateInstance<T>(String key, Boolean flag,
                                       Predicate<T> test) implements KeyPair<Predicate<T>>, Predicate<T> {
	@Override
	public Predicate<T> value() {
		return test;
	}

	@Override
	public boolean test(T entity) {
		return flag == test.test(entity);
	}

	public static <T> FlagPredicateInstance<T> of(KeyPair<Predicate<T>> entry, boolean flag) {
		return new FlagPredicateInstance<T>(entry.key(), flag, entry.value());
	}
}
