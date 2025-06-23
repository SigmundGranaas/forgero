package com.sigmundgranaas.forgero.identifier;

import com.sigmundgranaas.forgero.core.identifier.IdentifierFactory;
import com.sigmundgranaas.forgero.core.identifier.OpenIdentifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IdentifierFactoryTest {

	@Test
	void factoryWithDefaultBuilderUsesDefaultNamespace() {
		IdentifierFactory factory = new IdentifierFactory.Builder().build();
		OpenIdentifier id = factory.of("my_item");

		// Assert it defaults to "forgero"
		assertEquals("forgero", id.namespace());
		assertEquals("my_item", id.path());
	}

	@Test
	void factoryWithCustomNamespace() {
		IdentifierFactory factory = new IdentifierFactory.Builder()
				.defaultNamespace("custom")
				.build();

		OpenIdentifier id = factory.of("my_item");

		assertEquals("custom", id.namespace());
		assertEquals("my_item", id.path());
	}

	@Test
	void factoryHandlesFullyQualifiedStrings() {
		IdentifierFactory factory = new IdentifierFactory.Builder()
				.defaultNamespace("custom")
				.build();

		// The default namespace should be ignored here
		OpenIdentifier id = factory.of("minecraft:stone");

		assertEquals("minecraft", id.namespace());
		assertEquals("stone", id.path());
	}

	@Test
	void directCreationMethodWorks() {
		IdentifierFactory factory = new IdentifierFactory.Builder().build();
		OpenIdentifier id = factory.of("direct", "path");

		assertEquals("direct", id.namespace());
		assertEquals("path", id.path());
	}
}
