package com.sigmundgranaas.forgero.core.attribute.computation;

public record Addition(long value) implements Computation {
	@Override
	public long apply(long value) {
		return value + this.value;
	}
}
