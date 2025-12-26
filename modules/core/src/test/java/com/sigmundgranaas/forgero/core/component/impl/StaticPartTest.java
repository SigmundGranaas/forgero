package com.sigmundgranaas.forgero.core.component.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.component.api.Component;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes.DURABILITY;
import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.*;
import static com.sigmundgranaas.forgero.testutils.TestIdentifiers.*;
import static org.junit.jupiter.api.Assertions.*;

class StaticPartTest {

	@Test
	void createsStaticPartWithAllProperties() {
		// ARRANGE
		OpenIdentifier id = id("iron_ingot");
		OpenIdentifier metalTag = id("metal");

		// ACT
		Component part = part(id)
				.withTag(metalTag)
				.withAttribute(DURABILITY, 250f)
				.build();

		// ASSERT
		assertEquals(id, part.id());
		assertTrue(part.getTags().contains(metalTag));

		List<Attribute> attributes = part.properties(Attribute.KEY);
		assertEquals(1, attributes.size());
		assertEquals(DURABILITY, attributes.get(0).type());
		assertEquals(250f, attributes.get(0).value());
	}
}
