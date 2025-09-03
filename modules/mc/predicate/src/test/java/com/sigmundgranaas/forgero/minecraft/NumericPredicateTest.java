package com.sigmundgranaas.forgero.minecraft;

import com.sigmundgranaas.forgero.predicate.minecraft.util.NumericPredicate;
import com.sigmundgranaas.forgero.tools.Bootstrapped;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NumericPredicateTest implements Bootstrapped {

	@Test
	void testMin() {
		NumericPredicate predicate = new NumericPredicate(Optional.of(10.0), Optional.empty(), Optional.empty());
		assertTrue(predicate.test(10.0));
		assertTrue(predicate.test(15.0));
		assertFalse(predicate.test(9.9));
	}

	@Test
	void testMax() {
		NumericPredicate predicate = new NumericPredicate(Optional.empty(), Optional.of(20.0), Optional.empty());
		assertTrue(predicate.test(20.0));
		assertTrue(predicate.test(15.0));
		assertFalse(predicate.test(20.1));
	}

	@Test
	void testEqual() {
		NumericPredicate predicate = new NumericPredicate(Optional.empty(), Optional.empty(), Optional.of(5.0));
		assertTrue(predicate.test(5.0));
		assertFalse(predicate.test(5.1));
		assertFalse(predicate.test(4.9));
	}

	@Test
	void testRange() {
		NumericPredicate predicate = new NumericPredicate(Optional.of(0.0), Optional.of(100.0), Optional.empty());
		assertTrue(predicate.test(0.0));
		assertTrue(predicate.test(50.0));
		assertTrue(predicate.test(100.0));
		assertFalse(predicate.test(-0.1));
		assertFalse(predicate.test(100.1));
	}

	@Test
	void testAllConditions() {
		// All conditions are ANDed together.
		NumericPredicate predicate = new NumericPredicate(Optional.of(0.0), Optional.of(10.0), Optional.of(5.0));
		assertTrue(predicate.test(5.0));
		assertFalse(predicate.test(4.0)); // Fails equal check
		assertFalse(predicate.test(6.0)); // Fails equal check

		// A logically impossible predicate
		NumericPredicate conflictingPredicate = new NumericPredicate(Optional.of(0.0), Optional.of(10.0), Optional.of(11.0));
		assertFalse(conflictingPredicate.test(11.0)); // Fails max check
	}


	@Test
	void testEmptyPredicate() {
		NumericPredicate predicate = new NumericPredicate(Optional.empty(), Optional.empty(), Optional.empty());
		assertTrue(predicate.test(Double.MIN_VALUE));
		assertTrue(predicate.test(0.0));
		assertTrue(predicate.test(Double.MAX_VALUE));
	}
}
