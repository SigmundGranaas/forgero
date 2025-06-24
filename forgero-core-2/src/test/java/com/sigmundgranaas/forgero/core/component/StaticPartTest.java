package com.sigmundgranaas.forgero.core.component;

import com.sigmundgranaas.forgero.core.component.variant.StaticPart;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.property.api.Property;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes.DURABILITY;
import static org.junit.jupiter.api.Assertions.*;

class StaticPartTest {

	@Test
	void createsStaticPartWithAllProperties() {
		// ARRANGE
		OpenIdentifier id = new OpenIdentifier("forgero", "iron_ingot");
		Set<OpenIdentifier> tags = Set.of(new OpenIdentifier("forgero", "metal"));
		List<Property> properties = List.of(new Attribute(DURABILITY, 250f));

		// ACT
		StaticPart part = new StaticPart(id, tags, properties);

		// ASSERT
		assertEquals(id, part.id());
		assertEquals(tags, part.getTags());
		assertEquals(properties, part.getProperties());
	}
}
