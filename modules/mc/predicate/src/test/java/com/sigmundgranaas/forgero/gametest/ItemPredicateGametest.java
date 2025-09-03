package com.sigmundgranaas.forgero.gametest;

import com.sigmundgranaas.forgero.predicate.minecraft.item.ItemPredicate;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class ItemPredicateGametest {
	private static final String EMPTY_STRUCTURE = FabricGameTest.EMPTY_STRUCTURE;
	private static final TagKey<Item> SWORDS_TAG = TagKey.of(Registries.ITEM.getKey(), new Identifier("minecraft", "swords"));

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testTagMatch(TestContext context) {
		ItemPredicate predicate = new ItemPredicate(Optional.empty(), Optional.of(SWORDS_TAG), Optional.empty());
		ItemStack stack = new ItemStack(Items.DIAMOND_SWORD);
		context.assertTrue(predicate.test(stack), "Diamond sword should be in the swords tag");
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testTagMismatch(TestContext context) {
		ItemPredicate predicate = new ItemPredicate(Optional.empty(), Optional.of(SWORDS_TAG), Optional.empty());
		ItemStack stack = new ItemStack(Items.DIAMOND_PICKAXE);
		context.assertFalse(predicate.test(stack), "Diamond pickaxe should not be in the swords tag");
		context.complete();
	}
}
