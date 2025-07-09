package com.sigmundgranaas.forgero.data.loading.api.data.loader;

import com.sigmundgranaas.forgero.core.attribute.api.operator.AdditionOperator;
import com.sigmundgranaas.forgero.core.attribute.api.operator.MultiplicationOperator;
import com.sigmundgranaas.forgero.core.attribute.api.operator.Operator;
import com.sigmundgranaas.forgero.data.loading.impl.codec.AttributeCodecs;

import java.util.function.Function;

/**
 * A utility class to map operator and order strings from data to runtime objects.
 */
public class OperatorMapper implements Function<String, Operator> {
	@Override
	public Operator apply(String operator) {
		return switch (operator) {
			case AttributeCodecs.ADDITION_OPERATOR -> AdditionOperator.getInstance();
			case AttributeCodecs.MULTIPLICATION_OPERATOR -> MultiplicationOperator.getInstance();
			default -> AdditionOperator.getInstance();
		};
	}

	public int leveledOrder(String order) {
		return switch (order) {
			case AttributeCodecs.BASE_ORDER -> 0;
			case AttributeCodecs.MIDDLE_ORDER -> 1;
			case AttributeCodecs.END_ORDER -> 2;
			default -> 1;
		};
	}
}
