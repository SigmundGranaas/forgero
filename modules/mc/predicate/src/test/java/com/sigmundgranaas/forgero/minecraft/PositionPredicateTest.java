package com.sigmundgranaas.forgero.minecraft;

import com.sigmundgranaas.forgero.predicate.minecraft.util.NumericPredicate;
import com.sigmundgranaas.forgero.predicate.minecraft.util.PositionPredicate;
import com.sigmundgranaas.forgero.tools.Bootstrapped;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PositionPredicateTest implements Bootstrapped {

	@Test
	void testYCoordinate() {
		NumericPredicate yPredicate = new NumericPredicate(Optional.of(64.0), Optional.empty(), Optional.empty());
		PositionPredicate predicate = new PositionPredicate(Optional.empty(), Optional.of(yPredicate), Optional.empty());

		// A block at y=63 has its center at 63.5, which fails y >= 64.0. Correct.
		assertFalse(predicate.test(new BlockPos(0, 63, 0)));
		// A block at y=64 has its center at 64.5, which passes y >= 64.0. Correct.
		assertTrue(predicate.test(new BlockPos(0, 64, 0)));
		assertTrue(predicate.test(new BlockPos(100, 100, 100)));
	}

	@Test
	void testMultipleCoordinates() {
		// To include blocks at x=10, the center (10.5) must be <= 10.5
		NumericPredicate xPredicate = new NumericPredicate(Optional.empty(), Optional.of(10.5), Optional.empty());
		// To match a block at z=20, the center must be exactly 20.5
		NumericPredicate zPredicate = new NumericPredicate(Optional.empty(), Optional.empty(), Optional.of(20.5));
		PositionPredicate predicate = new PositionPredicate(Optional.of(xPredicate), Optional.empty(), Optional.of(zPredicate));

		// Center is (10.5, 50.5, 20.5). x <= 10.5 is true. z == 20.5 is true. -> TRUE
		assertTrue(predicate.test(new BlockPos(10, 50, 20)));
		// Center is (-4.5, -4.5, 20.5). x <= 10.5 is true. z == 20.5 is true. -> TRUE
		assertTrue(predicate.test(new BlockPos(-5, -5, 20)));
		// Center is (11.5, 50.5, 20.5). x <= 10.5 is false. -> FALSE
		assertFalse(predicate.test(new BlockPos(11, 50, 20))); // X fails
		// Center is (10.5, 50.5, 21.5). z == 20.5 is false. -> FALSE
		assertFalse(predicate.test(new BlockPos(10, 50, 21))); // Z fails
	}

	@Test
	void testEmptyPredicate() {
		PositionPredicate predicate = new PositionPredicate(Optional.empty(), Optional.empty(), Optional.empty());

		assertTrue(predicate.test(BlockPos.ORIGIN));
		assertTrue(predicate.test(new BlockPos(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE)));
	}
}
