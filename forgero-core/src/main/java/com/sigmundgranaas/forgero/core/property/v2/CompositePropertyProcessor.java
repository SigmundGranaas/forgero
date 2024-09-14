package com.sigmundgranaas.forgero.core.property.v2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.sigmundgranaas.forgero.core.context.Contexts;
import com.sigmundgranaas.forgero.core.property.Attribute;
import com.sigmundgranaas.forgero.core.property.CalculationOrder;
import com.sigmundgranaas.forgero.core.property.NumericOperation;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.attribute.AttributeBuilder;
import com.sigmundgranaas.forgero.core.property.attribute.Category;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

public class CompositePropertyProcessor implements PropertyProcessor {
	private List<Property> results;

	@Override
	public List<Property> process(List<Property> propertyList, Matchable target, MatchContext context) {
		if(results == null){
			this.results = combineCompositeProperties(propertyList, target, context);
		}
		return results;
	}

	private List<Property> combineCompositeProperties(List<Property> props, Matchable target, MatchContext context) {
		Map<String, List<Attribute>> groupedAttributes = props.stream()
				.filter(prop -> prop instanceof Attribute)
				.map(Attribute.class::cast)
				.filter(attribute -> attribute.getContext().test(Contexts.COMPOSITE))
				.filter(attribute -> attribute.applyCondition(target, context))
				.collect(Collectors.groupingBy(Property::type));

		List<Property> newValues = new ArrayList<>();

		for (Map.Entry<String, List<Attribute>> entry : groupedAttributes.entrySet()) {
			String type = entry.getKey();
			List<Attribute> compAttributes = entry.getValue().stream().sorted().toList();

			if (compAttributes.size() < 2) {
				continue;
			}

			Set<PropertyContainer> sources = new HashSet<>();
			float value = 0f;
			StringBuilder idBuilder = new StringBuilder();

			for (Attribute attribute : compAttributes) {
				assert (!attribute.isDynamic()): "Composite attributes should not use dynamic conditions, due to performance. Consider creating an attribute with a dynamic condition using the order: END";
				attribute.source().ifPresent(sources::add);
				value += attribute.applyAttribute(target, context, value);
				idBuilder.append(attribute.getId());
			}

			if (sources.size() < 2 || value == 0) {
				continue;
			}

			String combinedId = idBuilder + FastRandomString.generateRandomString();
			Property newAttribute = new AttributeBuilder(type)
					.applyOperation(NumericOperation.ADDITION)
					.applyOrder(CalculationOrder.BASE)
					.applyContext(Contexts.UNDEFINED)
					.applyCategory(Category.PASS)
					.applyValue(value)
					.applyId(combinedId)
					.build();

			newValues.add(newAttribute);
		}

		return newValues;
	}

}
