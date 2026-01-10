package com.sigmundgranaas.forgero.bows.gametest;

import com.sigmundgranaas.forgero.bows.entity.DynamicArrowEntity;
import com.sigmundgranaas.forgero.bows.entity.DynamicArrowEntityRegistry;
import com.sigmundgranaas.forgero.bows.item.ForgeroArrowItem;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public class DynamicArrowEntityRegistrationTest {

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "entity_registration")
	public void dynamicArrowEntityTypeIsRegistered(TestContext context) {
		EntityType<?> entityType = Registries.ENTITY_TYPE.get(DynamicArrowEntityRegistry.IDENTIFIER);
		
		context.assertTrue(entityType != null, 
				"DynamicArrowEntity type should be registered");
		context.assertTrue(entityType == DynamicArrowEntityRegistry.DYNAMIC_ARROW_ENTITY,
				"Registry should return the same EntityType instance");
		
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "entity_registration")
	public void dynamicArrowEntityCanBeCreatedFromRegistry(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		
		ItemStack arrowStack = getFirstForgeroArrow();
		if (arrowStack.isEmpty()) {
			arrowStack = new ItemStack(Items.ARROW);
		}
		
		DynamicArrowEntity arrow = new DynamicArrowEntity(
				context.getWorld(),
				player,
				arrowStack
		);
		
		context.assertTrue(arrow != null, "Should be able to create DynamicArrowEntity");
		context.assertTrue(!arrow.getStack().isEmpty(), "Arrow should carry the ItemStack");
		
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "entity_registration")
	public void dynamicArrowEntityCarriesItemStackCorrectly(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		
		ItemStack originalStack = getFirstForgeroArrow();
		if (originalStack.isEmpty()) {
			context.complete();
			return;
		}
		
		String expectedTranslationKey = originalStack.getItem().getTranslationKey();
		
		DynamicArrowEntity arrow = new DynamicArrowEntity(
				context.getWorld(),
				player,
				originalStack
		);
		
		ItemStack carriedStack = arrow.getStack();
		
		context.assertTrue(!carriedStack.isEmpty(), 
				"Arrow should carry non-empty ItemStack");
		context.assertTrue(carriedStack.getItem().getTranslationKey().equals(expectedTranslationKey),
				"Arrow should carry the exact item that was passed (expected: " + expectedTranslationKey + 
				", got: " + carriedStack.getItem().getTranslationKey() + ")");
		
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "entity_registration")
	public void dynamicArrowEntityCanBeSpawnedInWorld(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setPos(0, 64, 0);
		
		ItemStack arrowStack = getFirstForgeroArrow();
		if (arrowStack.isEmpty()) {
			arrowStack = new ItemStack(Items.ARROW);
		}
		
		DynamicArrowEntity arrow = new DynamicArrowEntity(
				context.getWorld(),
				player,
				arrowStack
		);
		arrow.setPos(player.getX(), player.getY() + 1, player.getZ());
		arrow.setVelocity(new Vec3d(0, 0.5, 1));
		
		context.getWorld().spawnEntity(arrow);
		
		context.waitAndRun(3, () -> {
			context.assertTrue(!arrow.isRemoved(), 
					"Arrow entity should exist in world after spawning");
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "entity_registration")
	public void dynamicArrowEntityReturnsItemStackForPickup(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		
		ItemStack originalStack = getFirstForgeroArrow();
		if (originalStack.isEmpty()) {
			context.complete();
			return;
		}
		
		DynamicArrowEntity arrow = new DynamicArrowEntity(
				context.getWorld(),
				player,
				originalStack
		);
		
		ItemStack carriedStack = arrow.getStack();
		
		context.assertTrue(!carriedStack.isEmpty(),
				"getStack() should return non-empty ItemStack for rendering and pickup");
		context.assertTrue(carriedStack.getItem().getTranslationKey().equals(originalStack.getItem().getTranslationKey()),
				"Carried stack should match original arrow type");
		
		context.complete();
	}

	private ItemStack getFirstForgeroArrow() {
		for (Item item : Registries.ITEM) {
			if (item instanceof ForgeroArrowItem) {
				return new ItemStack(item);
			}
		}
		
		Identifier ironArrowId = new Identifier("forgero", "iron-arrow");
		Item item = Registries.ITEM.get(ironArrowId);
		if (item != Items.AIR) {
			return new ItemStack(item);
		}
		
		return ItemStack.EMPTY;
	}
}
