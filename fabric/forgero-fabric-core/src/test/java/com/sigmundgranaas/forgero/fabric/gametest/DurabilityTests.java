package com.sigmundgranaas.forgero.fabric.gametest;
import com.sigmundgranaas.forgero.core.condition.Conditions;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.composite.ConstructedTool;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import com.sigmundgranaas.forgero.testutil.PlayerFactory;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

import net.minecraft.world.GameMode;

import org.junit.jupiter.api.Assertions;


public class DurabilityTests {
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "durability_tests", required = true)
	public void normalToolsBreak(TestContext context) {
		ItemStack stack = new ItemStack(Registries.ITEM.get(new Identifier("forgero:diamond-pickaxe")));
		ItemStack offHand = new ItemStack(Registries.ITEM.get(new Identifier("forgero:diamond-pickaxe")));

		var player = PlayerFactory.builder(context)
				.stack(() -> stack)
				.gameMode(GameMode.SURVIVAL)
				.build()
				.createPlayer();

		player.setStackInHand(Hand.OFF_HAND, offHand);


		int durability = stack.getMaxDamage();
		stack.damage(durability, player, (p) -> p.sendToolBreakStatus(Hand.MAIN_HAND));

		player.baseTick();

		Assertions.assertTrue(player.getStackInHand(Hand.MAIN_HAND).isEmpty(), "Tool was not destroyed and removed when taking max damage.");
		Assertions.assertFalse(player.getStackInHand(Hand.OFF_HAND).isDamaged(), "Off hand stack is damaged, this should be unaffected.");
		Assertions.assertFalse(player.getStackInHand(Hand.OFF_HAND).isEmpty(), "Off hand stack is broken, this should be unaffected.");

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "durability_tests", required = true)
	public void unbreakableDoesNotBreak(TestContext context) {
		ItemStack stack = new ItemStack(Registries.ITEM.get(new Identifier("forgero:diamond-sword")));
		ConstructedTool tool = (ConstructedTool) StateService.INSTANCE.convert(stack).get();
		State unbreakable = tool.applyCondition(Conditions.UNBREAKABLE);
		ItemStack finalStack = StateService.INSTANCE.convert(unbreakable).get();

		ItemStack offHand = new ItemStack(Registries.ITEM.get(new Identifier("forgero:diamond-sword")));
		int durability = stack.getMaxDamage();
		var player = PlayerFactory.builder(context)
				.stack(() -> finalStack)
				.gameMode(GameMode.SURVIVAL)
				.build()
				.createPlayer();

		player.setStackInHand(Hand.OFF_HAND, offHand);

		finalStack.damage(durability, player, (p) -> p.sendToolBreakStatus(Hand.MAIN_HAND));
		player.baseTick();

		double damage = player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);

		Assertions.assertFalse(player.getStackInHand(Hand.MAIN_HAND).isEmpty(), "Tool was destroyed even though it is unbreakable.");
		Assertions.assertEquals(damage, 1, "Tool was temporarily damaged, but still has normal attack damage. This is overpowered.");
		Assertions.assertFalse(player.getStackInHand(Hand.OFF_HAND).isDamaged(), "Off hand stack is damaged, this should be unaffected.");
		Assertions.assertFalse(player.getStackInHand(Hand.OFF_HAND).isEmpty(), "Off hand stack is broken, this should be unaffected.");

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "durability_tests", required = true)
	public void offHandBrokenToolsDoesNotAffectMainHand(TestContext context) {
		ItemStack stack = new ItemStack(Registries.ITEM.get(new Identifier("forgero:diamond-pickaxe")));
		ItemStack offHand = new ItemStack(Registries.ITEM.get(new Identifier("forgero:diamond-pickaxe")));

		var player = PlayerFactory.builder(context)
				.stack(() -> stack)
				.gameMode(GameMode.SURVIVAL)
				.build()
				.createPlayer();

		player.setStackInHand(Hand.OFF_HAND, offHand);

		int durability = stack.getMaxDamage();
		offHand.damage(durability, player, (p) -> p.sendToolBreakStatus(Hand.OFF_HAND));

		player.baseTick();

		Assertions.assertTrue(player.getStackInHand(Hand.OFF_HAND).isEmpty(), "Tool was not destroyed and removed when taking max damage.");
		Assertions.assertFalse(player.getStackInHand(Hand.MAIN_HAND).isDamaged(), "Main hand stack is damaged, this should be unaffected.");
		Assertions.assertFalse(player.getStackInHand(Hand.MAIN_HAND).isEmpty(), "Main hand stack is broken, this should be unaffected.");

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "durability_tests", required = true)
	public void unbreakableDoesNotAffectOtherItems(TestContext context) {
		ItemStack stack = new ItemStack(Registries.ITEM.get(new Identifier("forgero:diamond-pickaxe")));
		ConstructedTool tool = (ConstructedTool) StateService.INSTANCE.convert(stack).get();
		State unbreakable = tool.applyCondition(Conditions.UNBREAKABLE);
		ItemStack offHand = StateService.INSTANCE.convert(unbreakable).get();

		ItemStack mainHand = new ItemStack(Registries.ITEM.get(new Identifier("forgero:diamond-pickaxe")));
		int durability = stack.getMaxDamage();
		var player = PlayerFactory.builder(context)
				.stack(() -> mainHand)
				.gameMode(GameMode.SURVIVAL)
				.build()
				.createPlayer();

		player.setStackInHand(Hand.OFF_HAND, offHand);

		offHand.damage(durability, player, (p) -> p.sendToolBreakStatus(Hand.OFF_HAND));
		player.baseTick();

		Assertions.assertFalse(player.getStackInHand(Hand.MAIN_HAND).isEmpty(), "Main hand tool was damaged, even though the off hand stack received the damage.");
		Assertions.assertTrue(player.getStackInHand(Hand.OFF_HAND).isDamaged(), "Off hand stack should be damaged");
		Assertions.assertFalse(player.getStackInHand(Hand.OFF_HAND).isEmpty(), "Off hand stack is broken, this should be in a broken state.");

		context.complete();
	}
}
