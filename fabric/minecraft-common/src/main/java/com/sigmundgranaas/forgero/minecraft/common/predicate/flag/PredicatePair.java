package com.sigmundgranaas.forgero.minecraft.common.predicate.flag;

import java.util.function.Predicate;

import com.sigmundgranaas.forgero.minecraft.common.predicate.KeyPair;

public record PredicatePair<T>(String key,
                               Predicate<T> predicateEntry) implements KeyPair<Predicate<T>> {
	@Override
	public Predicate<T> value() {
		return predicateEntry();
	}
}
