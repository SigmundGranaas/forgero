package com.sigmundgranaas.forgero.core.attribute.computation;

public record Multiplication(long multiplier) implements Computation {
	@Override
	public long apply(long value) {
		return multiplier * value;
	}
}
