package com.sigmundgranaas.forgero.fabric.gametest;

import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.service.StateService;
import com.sigmundgranaas.forgero.testutil.PlayerFactory;
import com.sigmundgranaas.forgero.testutil.TestPos;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

import java.util.ArrayList;
import java.util.List;

import static com.sigmundgranaas.forgero.fabric.gametest.AttributeApplicationTest.createFloor;
import static com.sigmundgranaas.forgero.testutil.Utils.fromId;

public class HandlerInteractionTests {

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "ItemInteractionTest", tickLimit = 120)
	public void testMagneticFunction(TestContext context) {
		TestPos playerPos = TestPos.of(new BlockPos(3, 1, 3), context);

		createFloor(context);

		State gem = StateService.INSTANCE.find("forgero:magnetico-gem").get();
		Composite binding = (Composite) StateService.INSTANCE.find("forgero:oak-binding").get();
		Composite tool = (Composite) StateService.INSTANCE.find("forgero:oak-pickaxe").get();

		State upgraded = tool.upgrade(binding.upgrade(gem));


		// Spawn a player with a magnetico gem that triggers the magnetic effect
		PlayerEntity player = PlayerFactory.builder(context)
				.pos(playerPos.absolute())
				.stack(() -> StateService.INSTANCE.convert(upgraded).get())
				.build()
				.createPlayer();

		// Drop assorted items within a 2-block radius of the player
		dropItemsAroundPlayer(context, playerPos, 2);

		// Make sure the player is properly ticked
		context.runAtEveryTick(player::baseTick);

		// After 100 ticks, check if all items are within 1.5 blocks of the player
		context.runAtTick(100, () -> {
			// Define a bounding box around the player
			Box boundingBox = new Box(
					playerPos.absolute().getX() - 2, playerPos.absolute().getY() - 2, playerPos.absolute().getZ() - 2,
					playerPos.absolute().getX() + 2, playerPos.absolute().getY() + 2, playerPos.absolute().getZ() + 2);

			// Get entities within the bounding box
			List<Entity> entitiesWithinBox = context.getWorld().getOtherEntities(player, boundingBox);

			context.assertFalse(entitiesWithinBox.isEmpty(), "There should be items scattered around the player, but there were none.");

			for (Entity entity : entitiesWithinBox) {
				if (entity instanceof ItemEntity) {
					double distance = player.getPos().distanceTo(entity.getPos());

					context.assertTrue(distance <= 1.5, "Item not within 1.5 blocks of the player: " + entity);
				}
			}

			context.complete();
		});
	}

	private void dropItemsAroundPlayer(TestContext context, TestPos playerPos, int radius) {
		ItemStack[] itemsToDrop = {
				new ItemStack(Items.COBBLESTONE),
				new ItemStack(Items.IRON_INGOT),
				new ItemStack(Items.GOLD_INGOT),
				new ItemStack(Items.EMERALD),
				new ItemStack(Items.DIAMOND)
		};

		// Drop items in a circle around the player
		for (int i = 0; i < itemsToDrop.length; i++) {
			double angle = 2 * Math.PI * i / itemsToDrop.length;
			double xOffset = Math.cos(angle) * radius;
			double zOffset = Math.sin(angle) * radius;
			context.getWorld().spawnEntity(new ItemEntity(context.getWorld(), playerPos.absolute().getX() + xOffset, playerPos.absolute().getY(), playerPos.absolute().getZ() + zOffset, itemsToDrop[i]));
		}
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "PiglinBehaviorTest", tickLimit = 120)
	public void testPiglinsAggressiveWithIron(TestContext context) {
		TestPos playerPos = TestPos.of(new BlockPos(3, 1, 3), context);
		createFloor(context);

		// Spawn a player holding a gold ingot, which triggers piglin aggression
		ServerPlayerEntity player = PlayerFactory.builder(context)
				.pos(playerPos.absolute())
				.gameMode(GameMode.SURVIVAL)
				.stack(() -> new ItemStack(Items.IRON_INGOT))
				.playerName("Agressive")
				.build()
				.createPlayer();
		
		// Spawn piglins around the player
		spawnPiglinsAroundPlayer(context, playerPos, 2).forEach(entity -> entity.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, player.getPos()));

		// After 100 ticks, check the player's health to determine if piglins were aggressive
		context.runAtTick(100, () -> {
			boolean isHealthDecreased = player.getHealth() < player.getMaxHealth();
			context.assertTrue(isHealthDecreased, "Piglins should be aggressive towards the player.");

			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "PiglinBehaviorTest", tickLimit = 120)
	public void testPiglinsNotAggressiveWithGoldUpgrade(TestContext context) {
		TestPos playerPos = TestPos.of(new BlockPos(3, 1, 3), context);

		createFloor(context);
		// Spawn a player holding an item that does not trigger aggression
		PlayerEntity player = PlayerFactory.builder(context)
				.pos(playerPos.absolute())
				.gameMode(GameMode.SURVIVAL)
				.stack(() -> fromId("forgero:gold-pickaxe"))
				.playerName("Not agressive")
				.build()
				.createPlayer();

		spawnPiglinsAroundPlayer(context, playerPos, 2).forEach(entity -> entity.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, player.getPos()));

		// After 100 ticks, check the player's health to determine if piglins were aggressive
		context.runAtTick(100, () -> {
			boolean isHealthUnchanged = player.getHealth() == player.getMaxHealth();
			context.assertTrue(isHealthUnchanged, "Piglins should not be aggressive towards the player holding a gold weapon.");

			context.complete();
		});
	}

	private List<PiglinEntity> spawnPiglinsAroundPlayer(TestContext context, TestPos playerPos, int radius) {
		List<PiglinEntity> piglins = new ArrayList<>();
		for (int i = 0; i < 4; i++) {
			double angle = 2 * Math.PI * i / 4;
			double xOffset = Math.cos(angle) * radius;
			double zOffset = Math.sin(angle) * radius;
			PiglinEntity entity = new PiglinEntity(EntityType.PIGLIN, context.getWorld());
			Vec3d pos = new Vec3d(playerPos.absolute().getX() + xOffset, playerPos.absolute().getY(), playerPos.absolute().getZ() + zOffset);
			entity.setPos(pos.getX(), pos.getY(), pos.getZ());
			context.getWorld().spawnEntity(entity);
			entity.tick();
			piglins.add(entity);
			entity.initialize(context.getWorld(), context.getWorld().getLocalDifficulty(playerPos.absolute()), SpawnReason.MOB_SUMMONED, null, null);
		}
		return piglins;
	}
}
