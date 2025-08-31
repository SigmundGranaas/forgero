package com.sigmundgranaas.forgero.data.loading.impl.codec;

import com.sigmundgranaas.forgero.core.attribute.api.operator.*;
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

	public String toString(Operator operator) {
		if (operator instanceof AdditionOperator) return AttributeCodecs.ADDITION_OPERATOR;
		if (operator instanceof MultiplicationOperator) return AttributeCodecs.MULTIPLICATION_OPERATOR;
		if (operator instanceof SubtractionOperator) return "forgero:subtraction";
		if (operator instanceof DivisionOperator) return "forgero:division";
		if (operator instanceof MaxOperator) return "forgero:max";
		if (operator instanceof MinOperator) return "forgero:min";
		return AttributeCodecs.ADDITION_OPERATOR;
	}

	public String fromOrder(int group) {
		return switch (group) {
			case 0 -> AttributeCodecs.BASE_ORDER;
			case 1 -> AttributeCodecs.MIDDLE_ORDER;
			case 2 -> AttributeCodecs.END_ORDER;
			default -> AttributeCodecs.BASE_ORDER;
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
