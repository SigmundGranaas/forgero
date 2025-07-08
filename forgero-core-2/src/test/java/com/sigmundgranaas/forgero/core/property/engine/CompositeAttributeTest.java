package com.sigmundgranaas.forgero.core.property.engine;

import com.sigmundgranaas.forgero.core.attribute.api.CompositeAttribute;
import com.sigmundgranaas.forgero.core.attribute.api.CompositeAttributeComponent;
import com.sigmundgranaas.forgero.core.attribute.impl.computation.operator.AdditionOperator;
import com.sigmundgranaas.forgero.core.attribute.impl.computation.operator.DivisionOperator;
import com.sigmundgranaas.forgero.core.attribute.impl.computation.operator.MultiplicationOperator;
import com.sigmundgranaas.forgero.core.attribute.impl.computation.operator.SubtractionOperator;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CompositeAttributeTest {

	private final OpenIdentifier TEST_TYPE = new OpenIdentifier("test", "test_attribute");
	private final OpenIdentifier TEST_KEY = new OpenIdentifier("test", "test_composite_key");
	private final OpenIdentifier ANOTHER_TYPE = new OpenIdentifier("test", "another_attribute");
	private final OpenIdentifier ANOTHER_KEY = new OpenIdentifier("test", "another_composite_key");

	@Test
	void testOf_successfulCreation() {
		CompositeAttributeComponent comp1 = new CompositeAttributeComponent(TEST_TYPE, 10f, AdditionOperator.getInstance(), 0, TEST_KEY);
		CompositeAttributeComponent comp2 = new CompositeAttributeComponent(TEST_TYPE, 2f, MultiplicationOperator.getInstance(), 0, TEST_KEY);

		Optional<CompositeAttribute> result = CompositeAttribute.of(TEST_TYPE, TEST_KEY, List.of(comp1, comp2));

		assertTrue(result.isPresent());
		CompositeAttribute composite = result.get();
		assertEquals(TEST_TYPE, composite.type());
		assertEquals(TEST_KEY, composite.compositeKey());
		assertEquals(2, composite.getComposites().size());
		assertEquals(AdditionOperator.getInstance(), composite.operator()); // Default operator
		assertEquals(0, composite.group()); // Default group
	}

	@Test
	void testOf_emptyComponents_returnsEmptyOptional() {
		Optional<CompositeAttribute> result = CompositeAttribute.of(TEST_TYPE, TEST_KEY, Collections.emptyList());
		assertFalse(result.isPresent());
	}

	@Test
	void testOf_mismatchedType_returnsEmptyOptional() {
		CompositeAttributeComponent comp1 = new CompositeAttributeComponent(TEST_TYPE, 10f, AdditionOperator.getInstance(), 0, TEST_KEY);
		// Component with different type
		CompositeAttributeComponent comp2 = new CompositeAttributeComponent(ANOTHER_TYPE, 2f, MultiplicationOperator.getInstance(), 0, TEST_KEY);

		Optional<CompositeAttribute> result = CompositeAttribute.of(TEST_TYPE, TEST_KEY, List.of(comp1, comp2));
		assertFalse(result.isPresent());
	}

	@Test
	void testOf_mismatchedCompositeKey_returnsEmptyOptional() {
		CompositeAttributeComponent comp1 = new CompositeAttributeComponent(TEST_TYPE, 10f, AdditionOperator.getInstance(), 0, TEST_KEY);
		// Component with different compositeKey
		CompositeAttributeComponent comp2 = new CompositeAttributeComponent(TEST_TYPE, 2f, MultiplicationOperator.getInstance(), 0, ANOTHER_KEY);

		Optional<CompositeAttribute> result = CompositeAttribute.of(TEST_TYPE, TEST_KEY, List.of(comp1, comp2));
		assertFalse(result.isPresent());
	}

	@Test
	void testOf_singleOperatorType_returnsEmptyOptional() {
		CompositeAttributeComponent comp1 = new CompositeAttributeComponent(TEST_TYPE, 10f, AdditionOperator.getInstance(), 0, TEST_KEY);
		// Both components use the same operator type (AdditionOperator)
		CompositeAttributeComponent comp2 = new CompositeAttributeComponent(TEST_TYPE, 5f, AdditionOperator.getInstance(), 0, TEST_KEY);

		Optional<CompositeAttribute> result = CompositeAttribute.of(TEST_TYPE, TEST_KEY, List.of(comp1, comp2));
		assertFalse(result.isPresent());
	}

	@Test
	void testOf_valueCalculation() {
		// (0 + 10) * 2 - 5 = 10 * 2 - 5 = 20 - 5 = 15
		CompositeAttributeComponent comp1 = new CompositeAttributeComponent(TEST_TYPE, 10f, AdditionOperator.getInstance(), 0, TEST_KEY);
		CompositeAttributeComponent comp2 = new CompositeAttributeComponent(TEST_TYPE, 2f, MultiplicationOperator.getInstance(), 1, TEST_KEY);
		CompositeAttributeComponent comp3 = new CompositeAttributeComponent(TEST_TYPE, 5f, SubtractionOperator.getInstance(), 2, TEST_KEY);

		Optional<CompositeAttribute> result = CompositeAttribute.of(TEST_TYPE, TEST_KEY, List.of(comp1, comp2, comp3));

		assertTrue(result.isPresent());
		CompositeAttribute composite = result.get();
		assertEquals(15f, composite.value(), 0.001f);
	}

	@Test
	void testOf_getComposites_returnsUnmodifiableList() {
		CompositeAttributeComponent comp1 = new CompositeAttributeComponent(TEST_TYPE, 10f, AdditionOperator.getInstance(), 0, TEST_KEY);
		CompositeAttributeComponent comp2 = new CompositeAttributeComponent(TEST_TYPE, 2f, MultiplicationOperator.getInstance(), 0, TEST_KEY);

		Optional<CompositeAttribute> result = CompositeAttribute.of(TEST_TYPE, TEST_KEY, List.of(comp1, comp2));

		assertTrue(result.isPresent());
		CompositeAttribute composite = result.get();
		List<CompositeAttributeComponent> composites = composite.getComposites();

		assertThrows(UnsupportedOperationException.class, () -> composites.add(new CompositeAttributeComponent(TEST_TYPE, 1f, DivisionOperator.getInstance(), 0, TEST_KEY)));
	}
}
