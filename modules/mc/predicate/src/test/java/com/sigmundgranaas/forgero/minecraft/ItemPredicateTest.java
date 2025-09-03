package com.sigmundgranaas.forgero.minecraft;

import com.sigmundgranaas.forgero.predicate.minecraft.item.ItemPredicate;
import com.sigmundgranaas.forgero.predicate.minecraft.util.NumericPredicate;
import com.sigmundgranaas.forgero.tools.Bootstrapped;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemPredicateTest implements Bootstrapped {

	private static final TagKey<Item> SWORDS_TAG = TagKey.of(Registries.ITEM.getKey(), net.minecraft.util.Identifier.of("minecraft", "swords"));

	@Test
	void testSingleItemMatch() {
		ItemPredicate predicate = new ItemPredicate(Optional.of(List.of(Items.DIAMOND_SWORD)), Optional.empty(), Optional.empty());
		ItemStack stack = new ItemStack(Items.DIAMOND_SWORD);
		assertTrue(predicate.test(stack));
	}

	@Test
	void testSingleItemMismatch() {
		ItemPredicate predicate = new ItemPredicate(Optional.of(List.of(Items.DIAMOND_SWORD)), Optional.empty(), Optional.empty());
		ItemStack stack = new ItemStack(Items.IRON_SWORD);
		assertFalse(predicate.test(stack));
	}

	@Test
	void testListItemMatch() {
		ItemPredicate predicate = new ItemPredicate(Optional.of(List.of(Items.DIAMOND_SWORD, Items.IRON_SWORD)), Optional.empty(), Optional.empty());
		assertTrue(predicate.test(new ItemStack(Items.DIAMOND_SWORD)));
		assertTrue(predicate.test(new ItemStack(Items.IRON_SWORD)));
		assertFalse(predicate.test(new ItemStack(Items.GOLDEN_SWORD)));
	}

	@Test
	void testCountMatch() {
		NumericPredicate countPredicate = new NumericPredicate(Optional.of(5.0), Optional.empty(), Optional.empty());
		ItemPredicate predicate = new ItemPredicate(Optional.empty(), Optional.empty(), Optional.of(countPredicate));

		ItemStack stack = new ItemStack(Items.ARROW, 10);
		assertTrue(predicate.test(stack));

		ItemStack stack2 = new ItemStack(Items.ARROW, 4);
		assertFalse(predicate.test(stack2));
	}

	@Test
	void testCombinedPredicateMatch() {
		NumericPredicate countPredicate = new NumericPredicate(Optional.empty(), Optional.of(1.0), Optional.empty());
		ItemPredicate predicate = new ItemPredicate(Optional.of(List.of(Items.DIAMOND_SWORD)), Optional.empty(), Optional.of(countPredicate));

		ItemStack stack = new ItemStack(Items.DIAMOND_SWORD, 1);
		assertTrue(predicate.test(stack));
	}

	@Test
	void testCombinedPredicateMismatch() {
		NumericPredicate countPredicate = new NumericPredicate(Optional.empty(), Optional.of(1.0), Optional.empty());
		ItemPredicate predicate = new ItemPredicate(Optional.of(List.of(Items.DIAMOND_SWORD)), Optional.of(SWORDS_TAG), Optional.of(countPredicate));

		ItemStack wrongItem = new ItemStack(Items.IRON_SWORD, 1);
		assertFalse(predicate.test(wrongItem));

		ItemStack wrongCount = new ItemStack(Items.DIAMOND_SWORD, 2);
		assertFalse(predicate.test(wrongCount));
	}

	@Test
	void testEmptyStack() {
		ItemPredicate emptyPredicate = new ItemPredicate(Optional.empty(), Optional.empty(), Optional.empty());
		ItemPredicate itemPredicate = new ItemPredicate(Optional.of(List.of(Items.STONE)), Optional.empty(), Optional.empty());
		ItemPredicate countPredicate = new ItemPredicate(Optional.empty(), Optional.empty(), Optional.of(new NumericPredicate(Optional.of(1.0), Optional.empty(), Optional.empty())));

		// An empty stack should only match a completely empty predicate.
		assertTrue(emptyPredicate.test(ItemStack.EMPTY));
		assertFalse(itemPredicate.test(ItemStack.EMPTY));
		assertFalse(countPredicate.test(ItemStack.EMPTY));
	}
}
