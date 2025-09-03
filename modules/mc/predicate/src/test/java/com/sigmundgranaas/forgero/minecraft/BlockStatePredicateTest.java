package com.sigmundgranaas.forgero.minecraft;

import com.sigmundgranaas.forgero.predicate.minecraft.block.BlockStatePropertyPredicate;
import com.sigmundgranaas.forgero.tools.Bootstrapped;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.property.Properties;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockStatePredicateTest implements Bootstrapped {

	@Test
	void testBooleanPropertyMatch() {
		BlockStatePropertyPredicate predicate = new BlockStatePropertyPredicate(Map.of("lit", "true"));
		BlockState furnaceState = Blocks.FURNACE.getDefaultState().with(Properties.LIT, true);
		assertTrue(predicate.test(furnaceState));
	}

	@Test
	void testBooleanPropertyMismatch() {
		BlockStatePropertyPredicate predicate = new BlockStatePropertyPredicate(Map.of("lit", "false"));
		BlockState furnaceState = Blocks.FURNACE.getDefaultState().with(Properties.LIT, true);
		assertFalse(predicate.test(furnaceState));
	}

	@Test
	void testEnumPropertyMatch() {
		BlockStatePropertyPredicate predicate = new BlockStatePropertyPredicate(Map.of("facing", "north"));
		BlockState furnaceState = Blocks.FURNACE.getDefaultState().with(Properties.HORIZONTAL_FACING, net.minecraft.util.math.Direction.NORTH);
		assertTrue(predicate.test(furnaceState));
	}

	@Test
	void testIntegerPropertyMatch() {
		BlockStatePropertyPredicate predicate = new BlockStatePropertyPredicate(Map.of("age", "7"));
		BlockState wheatState = Blocks.WHEAT.getDefaultState().with(Properties.AGE_7, 7);
		assertTrue(predicate.test(wheatState));
	}

	@Test
	void testIntegerPropertyMismatch() {
		BlockStatePropertyPredicate predicate = new BlockStatePropertyPredicate(Map.of("age", "5"));
		BlockState wheatState = Blocks.WHEAT.getDefaultState().with(Properties.AGE_7, 7);
		assertFalse(predicate.test(wheatState));
	}

	@Test
	void testMultiplePropertiesMatch() {
		BlockStatePropertyPredicate predicate = new BlockStatePropertyPredicate(Map.of("facing", "south", "lit", "true"));
		BlockState furnaceState = Blocks.FURNACE.getDefaultState()
				.with(Properties.HORIZONTAL_FACING, net.minecraft.util.math.Direction.SOUTH)
				.with(Properties.LIT, true);
		assertTrue(predicate.test(furnaceState));
	}

	@Test
	void testMultiplePropertiesOneMismatch() {
		BlockStatePropertyPredicate predicate = new BlockStatePropertyPredicate(Map.of("facing", "south", "lit", "false"));
		BlockState furnaceState = Blocks.FURNACE.getDefaultState()
				.with(Properties.HORIZONTAL_FACING, net.minecraft.util.math.Direction.SOUTH)
				.with(Properties.LIT, true);
		assertFalse(predicate.test(furnaceState));
	}

	@Test
	void testNonExistentProperty() {
		BlockStatePropertyPredicate predicate = new BlockStatePropertyPredicate(Map.of("non_existent_prop", "any_value"));
		BlockState furnaceState = Blocks.FURNACE.getDefaultState();
		assertFalse(predicate.test(furnaceState));
	}

	@Test
	void testEmptyPredicate() {
		BlockStatePropertyPredicate predicate = new BlockStatePropertyPredicate(Map.of());
		BlockState anyState = Blocks.STONE.getDefaultState();
		assertTrue(predicate.test(anyState));
	}
}
