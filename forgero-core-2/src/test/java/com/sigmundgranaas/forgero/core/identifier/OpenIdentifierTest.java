package com.sigmundgranaas.forgero.core.identifier;

import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OpenIdentifierTest {

	@Test
	void createsIdentifierAndExposesParts() {
		OpenIdentifier id = new OpenIdentifier("forgero", "sword");

		assertEquals("forgero", id.namespace());
		assertEquals("sword", id.path());
	}

	@Test
	void toStringReturnsCorrectFormat() {
		OpenIdentifier id = new OpenIdentifier("forgero", "iron_pickaxe_head");
		assertEquals("forgero:iron_pickaxe_head", id.toString());
	}

	@Test
	void constructorThrowsOnInvalidNamespace() {
		assertThrows(IllegalArgumentException.class, () -> new OpenIdentifier("INVALID", "test"));
		assertThrows(IllegalArgumentException.class, () -> new OpenIdentifier("bad space", "test"));
		assertThrows(IllegalArgumentException.class, () -> new OpenIdentifier("bad:char", "test"));
	}

	@Test
	void constructorThrowsOnInvalidPath() {
		assertThrows(IllegalArgumentException.class, () -> new OpenIdentifier("forgero", "InvalidPath"));
		assertThrows(IllegalArgumentException.class, () -> new OpenIdentifier("forgero", "bad path"));
		assertThrows(IllegalArgumentException.class, () -> new OpenIdentifier("forgero", "bad:char"));
	}
}
