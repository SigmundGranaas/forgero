package com.sigmundgranaas.forgero.core.component.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.property.api.Property;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes.DURABILITY;
import static org.junit.jupiter.api.Assertions.*;

class StaticPartTest {

	@Test
	void createsStaticPartWithAllProperties() {
		// ARRANGE
		OpenIdentifier id = new OpenIdentifier("forgero", "iron_ingot");
		Set<OpenIdentifier> tags = Set.of(new OpenIdentifier("forgero", "metal"));
		List<Attribute> attributes = List.of(new SimpleAttribute(DURABILITY, 250f));
		Map<String, List<?>> properties = new HashMap<>();
		properties.put("forgero:attributes", attributes);
		// ACT
		StaticComponent part = new StaticComponent(id, tags, properties);

		// ASSERT
		assertEquals(id, part.id());
		assertEquals(tags, part.getTags());
		assertEquals(properties, part.propertiesAsMap());
	}
}
