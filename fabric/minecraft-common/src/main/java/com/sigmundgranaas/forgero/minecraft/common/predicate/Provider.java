package com.sigmundgranaas.forgero.minecraft.common.predicate;

import java.util.function.Function;

@FunctionalInterface
public interface Provider<T, R> extends Function<T, R> {
}
