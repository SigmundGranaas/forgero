package com.sigmundgranaas.forgero.core.property;

import com.mojang.serialization.Codec;

/**
 * The four different types of operations possible. Currently, only Addition and Multiplication are possible.
 */
public enum NumericOperation {
	ADDITION,
	SUBTRACTION,
	MULTIPLICATION,
	DIVISION,
	FORCE;

	public static final Codec<NumericOperation> CODEC = Codec.STRING.xmap(
			NumericOperation::valueOf,
			NumericOperation::name
	);
}
