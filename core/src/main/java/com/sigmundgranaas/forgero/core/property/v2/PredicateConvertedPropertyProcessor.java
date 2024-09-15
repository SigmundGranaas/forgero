package com.sigmundgranaas.forgero.core.property.v2;

import java.util.List;
import java.util.stream.Collectors;

import com.sigmundgranaas.forgero.core.context.Contexts;
import com.sigmundgranaas.forgero.core.property.CalculationOrder;
import com.sigmundgranaas.forgero.core.property.NumericOperation;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.attribute.AttributeBuilder;
import com.sigmundgranaas.forgero.core.property.attribute.Category;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

public class PredicateConvertedPropertyProcessor implements PropertyProcessor {
	@Override
	public List<Property> process(List<Property> propertyList, Matchable target, MatchContext context) {
		return combineCompositeProperties(propertyList, target, context);
	}

	private List<Property> combineCompositeProperties(List<Property> props, Matchable target, MatchContext context) {
		return Property.stream(props)
				.getAttributes()
				.filter(attribute -> attribute.getContext().test(Contexts.PredicateConverted))
				.filter(attribute -> attribute.applyCondition(target, context))
				.map(attribute -> {
					var newBaseAttribute = new AttributeBuilder(attribute.getAttributeType())
							.applyValue(attribute.getValue())
							.applyOperation(NumericOperation.ADDITION)
							.applyOrder(CalculationOrder.BASE)
							.applyContext(Contexts.UNDEFINED)
							.applyPredicate(Matchable.DEFAULT_TRUE)
							.applyCategory(Category.PASS);
					return newBaseAttribute.build();
				})
				.collect(Collectors.toList());
	}


}
