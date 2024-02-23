package com.sigmundgranaas.forgero.minecraft.common.predicate.flag;

import com.sigmundgranaas.forgero.minecraft.common.predicate.KeyPair;
import com.sigmundgranaas.forgero.minecraft.common.predicate.Predicate;

public record PredicatePair<T>(String key,
                               Predicate<T> predicateEntry) implements KeyPair<Predicate<T>> {
	@Override
	public Predicate<T> value() {
		return predicateEntry();
	}
}
