package com.sigmundgranaas.forgero.core.property.v2;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.sigmundgranaas.forgero.core.context.Contexts;
import com.sigmundgranaas.forgero.core.property.Attribute;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.attribute.AttributeBuilder;
import com.sigmundgranaas.forgero.core.property.attribute.Category;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Weight;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

public class UpgradePropertyProcessor implements PropertyProcessor {
	private final Set<Category> categories;

	public UpgradePropertyProcessor(Set<Category> categories) {
		this.categories = categories;
	}

	@Override
	public List<Property> process(List<Property> propertyList, Matchable target, MatchContext context) {
		var otherProperties = propertyList.stream().filter(property -> !isAttribute(property)).toList();
		var attributes = Property.stream(propertyList)
				.getAttributes()
				.filter(attribute -> filterAttribute(attribute, target, context))
				.map(this::mapToUndefined).toList();

		return Stream.of(otherProperties, attributes)
				.flatMap(List::stream)
				.map(Property.class::cast)
				.toList();
	}

	private boolean filterAttribute(Attribute attribute, Matchable target, MatchContext context) {
		if (!attribute.applyCondition(target, context)) {
			return false;
		}
		// Temporary fix for weight attribute having an effect on pulling speed
		if (categories.contains(Category.UNDEFINED) && attribute.getAttributeType().equals(Weight.KEY)) {
			return false;
		}
		if (attribute.getContext().test(Contexts.UNDEFINED) || attribute.getCategory() == Category.ALL || attribute.getCategory() == Category.PASS) {
			return true;
		} else return categories.contains(attribute.getCategory());
	}

	private Attribute mapToUndefined(Attribute attribute) {
		return AttributeBuilder.createAttributeBuilderFromAttribute(attribute)
				.applyCategory(Category.PASS)
				.applyContext(Contexts.UNDEFINED)
				.build();
	}

	private boolean isAttribute(Property property) {
		return property instanceof com.sigmundgranaas.forgero.core.property.Attribute;
	}
}
