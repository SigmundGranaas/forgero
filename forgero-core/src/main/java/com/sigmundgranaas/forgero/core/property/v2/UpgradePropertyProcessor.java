package com.sigmundgranaas.forgero.core.property.v2;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.sigmundgranaas.forgero.core.context.Contexts;
import com.sigmundgranaas.forgero.core.property.Attribute;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.attribute.AttributeBuilder;
import com.sigmundgranaas.forgero.core.property.attribute.Category;
import com.sigmundgranaas.forgero.core.util.Identifiers;

public class UpgradePropertyProcessor implements PropertyProcessor {
	private final Set<Category> categories;

	public UpgradePropertyProcessor(Set<Category> categories) {
		this.categories = categories;
	}

	@Override
	public List<Property> process(List<Property> propertyList) {
		var otherProperties = propertyList.stream().filter(property -> !isAttribute(property)).toList();
		var attributes = Property.stream(propertyList)
				.getAttributes()
				.filter(this::filterAttribute)
				.map(this::mapToUndefined).toList();

		return Stream.of(otherProperties, attributes)
				.flatMap(List::stream)
				.map(Property.class::cast)
				.toList();
	}

	private boolean filterAttribute(Attribute attribute) {
		if (attribute.getContext().test(Contexts.UNDEFINED) || attribute.getCategory() == Category.ALL || attribute.getCategory() == Category.PASS) {
			return true;
		} else return categories.contains(attribute.getCategory());
	}

	private Attribute mapToUndefined(Attribute attribute) {
		return AttributeBuilder.createAttributeBuilderFromAttribute(attribute)
				.applyCategory(Category.PASS)
				.applyContext(Contexts.UNDEFINED)
				.applyId(Identifiers.EMPTY_IDENTIFIER)
				.build();
	}

	private boolean isAttribute(Property property) {
		return property instanceof com.sigmundgranaas.forgero.core.property.Attribute;
	}
}
