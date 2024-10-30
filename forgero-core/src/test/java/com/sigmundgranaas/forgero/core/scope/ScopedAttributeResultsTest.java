package com.sigmundgranaas.forgero.core.scope;

import com.sigmundgranaas.forgero.core.attribute.AttributeComponent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScopedAttributeResultsTest {

	@Test
	void testEmpty() {
		ScopedAttributeResults results = ScopedAttributeResults.empty();
		assertTrue(results.isEmpty());
		assertTrue(results.results().isEmpty());
		assertTrue(results.unresolvedResults().isEmpty());
		assertTrue(results.potentialInvalidations().isEmpty());
	}

	@Test
	void testAddResolved() {
		AttributeComponent attr1 = AttributeComponent.add("attack", 1);
		AttributeComponent attr2 = AttributeComponent.add("defence", 1);

		ScopedAttributeResults results = ScopedAttributeResults.empty()
				.addResolved(List.of(attr1))
				.addResolved(List.of(attr2));

		assertEquals(2, results.results().size());
		assertTrue(results.unresolvedResults().isEmpty());
	}

	@Test
	void testAddUnresolved() {
		AttributeComponent attr1 = AttributeComponent.add("attack", 1);
		String scopeId = "scope1";

		ScopedAttributeResults results = ScopedAttributeResults.empty()
				.addUnresolved(scopeId, List.of(attr1));

		assertTrue(results.results().isEmpty());
		assertTrue(results.hasUnresolvedAttributesFor(scopeId));
		assertEquals(1, results.unresolvedResults().get(scopeId).size());
	}

	@Test
	void testResolve() {
		AttributeComponent attr1 = AttributeComponent.add("attack", 1);
		String scopeId = "scope1";
		Scope scope = Scope.simple(scopeId);

		ScopedAttributeResults results = ScopedAttributeResults.empty()
				.addUnresolved(scopeId, List.of(attr1))
				.resolve(scope);

		assertEquals(1, results.results().size());
		assertFalse(results.hasUnresolvedAttributesFor(scopeId));
	}

	@Test
	void testMerge() {
		AttributeComponent attr1 = AttributeComponent.add("attack", 1);
		AttributeComponent attr2 = AttributeComponent.add("defence", 1);
		String scopeId = "scope1";

		ScopedAttributeResults results1 = ScopedAttributeResults.empty()
				.addResolved(List.of(attr1));

		ScopedAttributeResults results2 = ScopedAttributeResults.empty()
				.addUnresolved(scopeId, List.of(attr2))
				.addPotentialInvalidation(scopeId);

		ScopedAttributeResults merged = results1.merge(results2);

		assertEquals(1, merged.results().size());
		assertTrue(merged.hasUnresolvedAttributesFor(scopeId));
		assertTrue(merged.potentialInvalidations().contains(scopeId));
	}

	@Test
	void testImmutability() {
		AttributeComponent attr1 = AttributeComponent.add("attack", 1);
		String scopeId = "scope1";

		ScopedAttributeResults results = ScopedAttributeResults.empty()
				.addResolved(List.of(attr1))
				.addUnresolved(scopeId, List.of(attr1));

		assertThrows(UnsupportedOperationException.class, () ->
				results.results().add(attr1));
		assertThrows(UnsupportedOperationException.class, () ->
				results.unresolvedResults().put(scopeId, List.of()));
		assertThrows(UnsupportedOperationException.class, () ->
				results.potentialInvalidations().add(scopeId));
	}
}
