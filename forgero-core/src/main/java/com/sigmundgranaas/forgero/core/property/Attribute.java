package com.sigmundgranaas.forgero.core.property;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.sigmundgranaas.forgero.core.context.Context;
import com.sigmundgranaas.forgero.core.property.attribute.AttributeBuilder;
import com.sigmundgranaas.forgero.core.property.attribute.Category;
import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for attributes which can affect the attributes of tools.
 * Attributes are designed to be pooled together and calculated in a chain.
 */
public interface Attribute extends Property, Comparable<Attribute> {
	Function<Float, Float> DEFAULT_ATTRIBUTE_CALCULATION = (currentFloat) -> currentFloat;

	default CalculationOrder getOrder() {
		return CalculationOrder.BASE;
	}

	@Override
	default int compareTo(@NotNull Attribute o) {
		if (o.getAttributeType().equals(getAttributeType())) {
			boolean hasDefaultPredicate = getPredicate() == Matchable.DEFAULT_TRUE;
			boolean hasTargetDefaultPredicate = o.getPredicate() == Matchable.DEFAULT_TRUE;
			if (hasDefaultPredicate && !hasTargetDefaultPredicate) {
				return -1;
			} else if (hasTargetDefaultPredicate && !hasDefaultPredicate) {
				return 1;
			}
		}
		int order = getOrder().getValue() - o.getOrder().getValue();
		if (order == 0) {
			return getOperation().ordinal() - o.getOperation().ordinal();
		}
		return order;
	}

	String getAttributeType();

	default Function<Float, Float> getCalculation() {
		return DEFAULT_ATTRIBUTE_CALCULATION;
	}

	default Matchable getPredicate() {
		return Matchable.DEFAULT_TRUE;
	}

	NumericOperation getOperation();

	float getValue();

	Category getCategory();

	int getLevel();

	List<String> targets();

	String targetType();

	Context getContext();

	int getPriority();

	String getId();

	float leveledValue();

	default Optional<PropertyContainer> source() {
		return Optional.empty();
	}

	default Attribute setSource(PropertyContainer source) {
		return AttributeBuilder.createAttributeBuilderFromAttribute(this).applySource(source).build();
	}

	default float applyAttribute(Matchable target, MatchContext context, float currentAttribute) {
		if (this.getPredicate().test(target, context)) {
			return this.getCalculation().apply(currentAttribute);
		}
		return currentAttribute;
	}

	@Override
	default boolean applyCondition(Matchable target, MatchContext context) {
		return this.getPredicate().test(target, context);
	}

	ComputedAttribute compute();

	default PropertyContainer container() {
		return PropertyContainer.of(this);
	}
}
