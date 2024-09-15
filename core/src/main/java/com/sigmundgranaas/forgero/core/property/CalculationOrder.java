package com.sigmundgranaas.forgero.core.property;

import com.mojang.serialization.Codec;

/**
 * Enum for describing in which order calculations should occur.
 */
public enum CalculationOrder {
	BASE(0),
	BASE_MULTIPLICATION(2),
	MIDDLE(5),
	END(10);

	public static final Codec<CalculationOrder> CODEC = Codec.STRING.xmap(
			CalculationOrder::valueOf,
			CalculationOrder::name
	);

	private final int value;

	CalculationOrder(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
