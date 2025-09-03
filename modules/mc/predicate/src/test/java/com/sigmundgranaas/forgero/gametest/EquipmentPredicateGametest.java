package com.sigmundgranaas.forgero.gametest;

import com.sigmundgranaas.forgero.predicate.minecraft.item.EquipmentPredicate;
import com.sigmundgranaas.forgero.predicate.minecraft.item.ItemPredicate;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Optional;

public class EquipmentPredicateGametest {
	private static final String EMPTY_STRUCTURE = FabricGameTest.EMPTY_STRUCTURE;

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testMainhandMatch(TestContext context) {
		ItemPredicate ironSword = new ItemPredicate(Optional.of(List.of(Items.IRON_SWORD)), Optional.empty(), Optional.empty());
		EquipmentPredicate predicate = new EquipmentPredicate(Optional.of(ironSword), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
		LivingEntity entity = context.spawnEntity(EntityType.ZOMBIE, BlockPos.ORIGIN);

		entity.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
		context.assertTrue(predicate.test(entity), "Predicate should match when holding iron sword");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testMainhandMismatch(TestContext context) {
		ItemPredicate ironSword = new ItemPredicate(Optional.of(List.of(Items.IRON_SWORD)), Optional.empty(), Optional.empty());
		EquipmentPredicate predicate = new EquipmentPredicate(Optional.of(ironSword), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
		LivingEntity entity = context.spawnEntity(EntityType.ZOMBIE, BlockPos.ORIGIN);

		entity.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
		context.assertFalse(predicate.test(entity), "Predicate should not match when holding diamond sword");

		entity.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
		context.assertFalse(predicate.test(entity), "Predicate should not match when holding nothing");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testHeadAndChestMatch(TestContext context) {
		ItemPredicate helmet = new ItemPredicate(Optional.of(List.of(Items.IRON_HELMET)), Optional.empty(), Optional.empty());
		ItemPredicate chestplate = new ItemPredicate(Optional.of(List.of(Items.IRON_CHESTPLATE)), Optional.empty(), Optional.empty());
		EquipmentPredicate predicate = new EquipmentPredicate(Optional.empty(), Optional.empty(), Optional.of(helmet), Optional.of(chestplate), Optional.empty(), Optional.empty());
		LivingEntity entity = context.spawnEntity(EntityType.ZOMBIE, BlockPos.ORIGIN);

		entity.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
		entity.equipStack(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
		context.assertTrue(predicate.test(entity), "Predicate should match with correct armor");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testAllSlotsMatch(TestContext context) {
		ItemPredicate sword = new ItemPredicate(Optional.of(List.of(Items.DIAMOND_SWORD)), Optional.empty(), Optional.empty());
		ItemPredicate shield = new ItemPredicate(Optional.of(List.of(Items.SHIELD)), Optional.empty(), Optional.empty());
		ItemPredicate helmet = new ItemPredicate(Optional.of(List.of(Items.DIAMOND_HELMET)), Optional.empty(), Optional.empty());
		ItemPredicate chest = new ItemPredicate(Optional.of(List.of(Items.DIAMOND_CHESTPLATE)), Optional.empty(), Optional.empty());
		ItemPredicate legs = new ItemPredicate(Optional.of(List.of(Items.DIAMOND_LEGGINGS)), Optional.empty(), Optional.empty());
		ItemPredicate feet = new ItemPredicate(Optional.of(List.of(Items.DIAMOND_BOOTS)), Optional.empty(), Optional.empty());

		EquipmentPredicate predicate = new EquipmentPredicate(Optional.of(sword), Optional.of(shield), Optional.of(helmet), Optional.of(chest), Optional.of(legs), Optional.of(feet));
		LivingEntity entity = context.spawnEntity(EntityType.ZOMBIE, BlockPos.ORIGIN);

		entity.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
		entity.equipStack(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
		entity.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
		entity.equipStack(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE));
		entity.equipStack(EquipmentSlot.LEGS, new ItemStack(Items.DIAMOND_LEGGINGS));
		entity.equipStack(EquipmentSlot.FEET, new ItemStack(Items.DIAMOND_BOOTS));

		context.assertTrue(predicate.test(entity), "Predicate should match with all slots equipped correctly");
		context.complete();
	}
}
