package com.sigmundgranaas.forgero.core.attribute.computation;


@FunctionalInterface
public interface Computation {
	long apply(long value);
}
