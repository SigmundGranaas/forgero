package com.sigmundgranaas.forgero.core.property.attribute;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import com.sigmundgranaas.forgero.core.context.Context;
import com.sigmundgranaas.forgero.core.context.Contexts;
import com.sigmundgranaas.forgero.core.property.Attribute;
import com.sigmundgranaas.forgero.core.property.NumericOperation;
import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import com.sigmundgranaas.forgero.core.util.match.Matchable;


public record SimpleAttribute(String attribute,
                              float value) implements Attribute {


	@Override
	public String getAttributeType() {
		return attribute;
	}

	@Override
	public Matchable getPredicate() {
		return Matchable.DEFAULT_TRUE;
	}

	@Override
	public NumericOperation getOperation() {
		return NumericOperation.ADDITION;
	}

	@Override
	public float getValue() {
		return value;
	}

	@Override
	public float leveledValue() {
		return value;
	}

	@Override
	public Category getCategory() {
		return Category.UNDEFINED;
	}

	@Override
	public int getLevel() {
		return 1;
	}

	@Override
	public List<String> targets() {
		return Collections.emptyList();
	}

	@Override
	public String targetType() {
		return null;
	}

	@Override
	public Context getContext() {
		return Contexts.UNDEFINED;
	}

	@Override
	public int getPriority() {
		return 1;
	}

	@Override
	public String getId() {
		return String.format("%s-%s", attribute, value);
	}

	@Override
	public String type() {
		return getAttributeType();
	}


	@Override
	public int hashCode() {
		return Objects.hash(attribute, value);
	}

	@Override
	public ComputedAttribute compute() {
		return ComputedAttribute.of(leveledValue(), type());
	}

	@Override
	public Function<Float, Float> getCalculation() {
		return (value) -> value + getValue();
	}

	@Override
	public String toString() {
		return "Attribute{" +
				"attribute=" + attribute +
				", value=" + value +
				'}';
	}
}
