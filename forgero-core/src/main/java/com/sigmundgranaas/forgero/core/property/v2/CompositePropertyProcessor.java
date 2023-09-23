package com.sigmundgranaas.forgero.core.property.v2;

import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.sigmundgranaas.forgero.core.context.Contexts;
import com.sigmundgranaas.forgero.core.property.Attribute;
import com.sigmundgranaas.forgero.core.property.CalculationOrder;
import com.sigmundgranaas.forgero.core.property.NumericOperation;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.attribute.AttributeBuilder;
import com.sigmundgranaas.forgero.core.property.attribute.Category;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

public class CompositePropertyProcessor implements PropertyProcessor {
	@Override
	public List<Property> process(List<Property> propertyList, Matchable target, MatchContext context) {
		return combineCompositeProperties(propertyList, target, context);
	}


	private List<Property> combineCompositeProperties(List<Property> props, Matchable target, MatchContext context) {
		List<Property> compositeAttributes = Property.stream(props)
				.getAttributes()
				.filter(attribute -> attribute.getContext().test(Contexts.COMPOSITE))
				.filter(attribute -> attribute.applyCondition(target, context))
				.collect(Collectors.toList());

		var newValues = new ArrayList<Property>();
		Set<String> types = compositeAttributes.stream().map(Property::type).collect(Collectors.toUnmodifiableSet());
		for (String type : types) {
			List<Attribute> compAttributes = compositeAttributes.stream()
					.filter(prop -> prop instanceof Attribute attribute1 && attribute1.getAttributeType().equals(type))
					.map(Attribute.class::cast)
					.toList();
			boolean lessThanTwo = compAttributes.size() < 2;

			boolean onlyFromSameSource = compAttributes.stream()
					.map(Attribute::source)
					.flatMap(Optional::stream)
					.collect(Collectors.toUnmodifiableSet())
					.size() < 2;
			if (lessThanTwo || onlyFromSameSource) {
				continue;
			}

			var newBaseAttribute = new AttributeBuilder(type)
					.applyOperation(NumericOperation.ADDITION)
					.applyOrder(CalculationOrder.BASE)
					.applyContext(Contexts.UNDEFINED)
					.applyCategory(Category.PASS);

			newBaseAttribute
					.applyValue(Property.stream(compositeAttributes).applyAttribute(type));


			String combinedId = compositeAttributes.stream()
					.filter(attr -> attr.type().equals(type))
					.map(com.sigmundgranaas.forgero.core.property.Attribute.class::cast)
					.map(com.sigmundgranaas.forgero.core.property.Attribute::getId)
					.reduce(EMPTY_IDENTIFIER, String::join);

			combinedId = combinedId + UUID.randomUUID();
			newBaseAttribute.applyId(combinedId);

			var attribute = newBaseAttribute.build();
			if (attribute.getValue() != 0) {
				newValues.add(attribute);
			}
		}
		return newValues;
	}

}
