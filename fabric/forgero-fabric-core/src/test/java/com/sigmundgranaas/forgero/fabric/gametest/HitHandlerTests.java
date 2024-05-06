package com.sigmundgranaas.forgero.fabric.gametest;

import static com.sigmundgranaas.forgero.fabric.gametest.AttributeApplicationTest.createFloor;

import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitEntity.ConvertHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitEntity.DisarmHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitEntity.FireHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitEntity.KnockbackHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitEntity.LifeStealHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitEntity.LightningStrikeHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitEntity.StatusEffectHandler;
import com.sigmundgranaas.forgero.testutil.PlayerFactory;
import com.sigmundgranaas.forgero.testutil.TestPos;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;

import java.util.Objects;

public class HitHandlerTests {

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "hitHandler", tickLimit = 120)
	public void testFireHandlerOnEntity(TestContext context) {
		TestPos playerPos = TestPos.of(new BlockPos(3, 1, 3), context);
		createFloor(context);

		PlayerEntity player = PlayerFactory.builder(context)
				.pos(playerPos.absolute())
				.build()
				.createPlayer();


		PigEntity target = context.spawnEntity(EntityType.PIG, playerPos.offset(new BlockPos(1, 0, 1)).relative());

		// Apply FireHandler effects on the target entity
		FireHandler fireHandler = fireHandler();
		fireHandler.onHit(player, context.getWorld(), target);

		// After 1 tick, check if the target entity is on fire
		context.runAtTick(1, () -> {
			// Verify that the target is on fire
			context.assertTrue(target.getFireTicks() > 1, "Target entity is not on fire.");

			context.assertTrue(target.getHealth() < target.getMaxHealth(), "Target entity has not taken any damage.");

			// Verify the target is on fire for the expected duration
			context.assertTrue(target.getFireTicks() == (fireHandler.duration() * 20) - 1, "Target entity is not on fire for the expected duration.");

			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "hitHandler", tickLimit = 120)
	public void testFireHandlerOnBlock(TestContext context) {
		TestPos playerPos = TestPos.of(new BlockPos(3, 1, 3), context);

		// Place a burnable block next to the player
		TestPos targetBlockPos = TestPos.of(new BlockPos(1, 2, 1), context);
		context.setBlockState(targetBlockPos.relative(), Blocks.OAK_PLANKS);

		PlayerEntity player = PlayerFactory.builder(context)
				.pos(playerPos.absolute())
				.build()
				.createPlayer();


		FireHandler fireHandler = fireBlockHandler();
		fireHandler.onHit(player, context.getWorld(), targetBlockPos.absolute());

		// After 1 tick, check if the block at targetBlockPos is set on fire
		context.runAtTick(1, () -> {
			BlockState stateAtTarget = context.getWorld().getBlockState(targetBlockPos.absolute().up());
			// Verify the block is replaced with fire
			context.assertTrue(stateAtTarget.getBlock() == Blocks.FIRE, "Target block is not set on fire as expected.");

			context.complete();
		});
	}

	public static FireHandler fireHandler() {
		String handler = """
				{
					"type": "minecraft:fire",
				    "duration": 5
				    }
								
				""";
		return Utils.handlerFromString(handler, FireHandler.BUILDER);
	}

	public static FireHandler fireBlockHandler() {
		String handler = """
				{
				        "type": "minecraft:fire",
				        "duration": 5
				    }
				   
				""";
		return Utils.handlerFromString(handler, FireHandler.BUILDER);
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "hitHandler", tickLimit = 120)
	public void testDisarmHandler(TestContext context) {
		TestPos playerPos = TestPos.of(new BlockPos(3, 1, 3), context);

		createFloor(context);

		// Spawn a player that will act as the source of the attack
		PlayerEntity player = PlayerFactory.builder(context)
				.pos(playerPos.absolute())
				.build()
				.createPlayer();

		// Spawn a target entity (e.g., a zombie) and give it an item to hold
		ZombieEntity target = context.spawnEntity(EntityType.ZOMBIE, playerPos.relative());
		target.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));

		// Create an instance of DisarmHandler from JSON
		DisarmHandler disarmHandler = disarmHandler();

		// Trigger the onHit method simulating the player hitting the target
		disarmHandler.onHit(player, context.getWorld(), target);

		// After a brief delay, check if the target has been disarmed
		context.runAtTick(1, () -> {
			// Ensure the target's main hand is now empty
			context.assertTrue(target.getMainHandStack().isEmpty(), "Target entity's main hand is not empty after being disarmed.");

			BlockPos first = TestPos.of(new BlockPos(0, 0, 0), context).absolute();
			BlockPos second =  TestPos.of(new BlockPos(7, 7, 7), context).absolute();
			// Check for a dropped item entity to ensure the item was dropped
			boolean itemDropped = context.getWorld().getEntitiesByClass(ItemEntity.class, new Box(first.toCenterPos(), second.toCenterPos()), entity -> entity.getStack().getItem() == Items.DIAMOND_SWORD).size() == 1;
			context.assertTrue(itemDropped, "The disarmed item was not dropped in the world.");

			context.complete();
		});
	}

	public static DisarmHandler disarmHandler() {
		String json = """
				{
				    "type": "forgero:disarm",
				    "target": "minecraft:targeted_entity"
				}
				""";
		return Utils.handlerFromString(json, DisarmHandler.BUILDER);
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "hitHandler", tickLimit = 120)
	public void testConvertHandler(TestContext context) {
		TestPos playerPos = TestPos.of(new BlockPos(3, 1, 3), context);
		createFloor(context);

		PlayerEntity player = PlayerFactory.builder(context)
				.pos(playerPos.absolute())
				.build()
				.createPlayer();

		Entity target = context.spawnEntity(EntityType.PIG, playerPos.offset(1, 0, 1).relative());

		ConvertHandler convertHandler = convertHandler();
		convertHandler.onHit(player, context.getWorld(), target);

		// After a brief delay, check if the target has been converted
		context.runAtTick(20, () -> {
			// Verify that the original target is removed
			context.assertTrue(target.isRemoved(), "Original target entity has not been removed.");

			boolean conversionSuccessful = context.getWorld().getEntitiesByClass(ZombieEntity.class, new Box(playerPos.offset(-3, -2, -3).absolute().toCenterPos(), playerPos.offset(3, 2, 3).absolute().toCenterPos()), Objects::nonNull).size() == 1;
			context.assertTrue(conversionSuccessful, "Target entity was not converted to the specified type.");

			context.complete();
		});
	}

	public static ConvertHandler convertHandler() {
		String json = """
				{
				    "type": "minecraft:convert",
				    "convert_to": "minecraft:zombie",
				    "target": "minecraft:targeted_entity"
				}
				""";
		return Utils.handlerFromString(json, ConvertHandler.BUILDER);

	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "KnockbackHandlerTest", tickLimit = 120)
	public void testKnockbackHandlerPush(TestContext context) {
		TestPos playerPos = TestPos.of(new BlockPos(3, 1, 3), context);

		PlayerEntity player = PlayerFactory.builder(context)
				.pos(playerPos.absolute())
				.build()
				.createPlayer();

		Entity target = context.spawnEntity(EntityType.PIG, playerPos.offset(1, 0, 1).relative());

		// Configure and apply a push direction knockback handler
		KnockbackHandler knockbackHandlerPush = pushKnockbackHandler();
		knockbackHandlerPush.onHit(player, context.getWorld(), target);

		context.runAtTick(1, () -> {
			Vec3d expectedDirection = target.getPos().subtract(player.getPos()).normalize();
			Vec3d actualVelocity = target.getVelocity();

			// Verify that the target's velocity is in the expected direction for a push
			context.assertTrue(actualVelocity.dotProduct(expectedDirection) > 0, "Target entity was not pushed away from the source.");

			context.complete();
		});
	}


	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "KnockbackHandlerTest", tickLimit = 120)
	public void testKnockbackHandlerPull(TestContext context) {
		TestPos playerPos = TestPos.of(new BlockPos(3, 1, 3), context);

		PlayerEntity player = PlayerFactory.builder(context)
				.pos(playerPos.absolute())
				.build()
				.createPlayer();

		Entity target = context.spawnEntity(EntityType.PIG, playerPos.offset(1, 0, 1).relative());

		// Configure and apply a pull direction knockback handler
		KnockbackHandler knockbackHandlerPull = pullKnockbackHandler();
		knockbackHandlerPull.onHit(player, context.getWorld(), target);

		context.runAtTick(1, () -> {
			Vec3d expectedDirection = player.getPos().subtract(target.getPos()).normalize();
			Vec3d actualVelocity = target.getVelocity();

			// Verify that the target's velocity is in the expected direction for a pull
			context.assertTrue(actualVelocity.dotProduct(expectedDirection) > 0, "Target entity was not pulled towards the source.");

			context.complete();
		});
	}


	public static KnockbackHandler pushKnockbackHandler() {
		String json = """
				{
				        "type": "minecraft:knockback",
				        "target": "minecraft:targeted_entity",
				        "force": 1.5,
				        "direction": "push"
				    }
				    		
				""";
		return Utils.handlerFromString(json, KnockbackHandler.BUILDER);
	}

	public static KnockbackHandler pullKnockbackHandler() {
		String json = """
				{
								
				        "type": "minecraft:knockback",
				        "target": "minecraft:targeted_entity",
				        "force": 1.5,
				        "direction": "pull"
				    }
								
				""";
		return Utils.handlerFromString(json, KnockbackHandler.BUILDER);
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "LifeStealHandlerTest", tickLimit = 120)
	public void testLifeStealHandler(TestContext context) {
		TestPos playerPos = TestPos.of(new BlockPos(3, 1, 3), context);

		PlayerEntity player = PlayerFactory.of(context, playerPos);

		// Set player's health to half to see the healing effect clearly
		player.setHealth(player.getMaxHealth() / 2);

		// Spawn a target entity with a set amount of health
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, playerPos.offset(1, 0, 1).relative());
		target.setHealth(target.getMaxHealth());

		// Create an instance of LifeStealHandler from JSON
		LifeStealHandler lifeStealHandler = lifeStealHandler();

		// Trigger the onHit method simulating the player hitting the target
		lifeStealHandler.onHit(player, context.getWorld(), target);

		context.runAtTick(1, () -> {
			// Verify the target's health decreased
			context.assertTrue(target.getHealth() < target.getMaxHealth(), "Target entity's health was not reduced.");

			// Verify the source (player) health increased
			context.assertTrue(player.getHealth() > player.getMaxHealth() / 2, "Source entity's (player) health was not increased.");

			context.complete();
		});
	}

	public static LifeStealHandler lifeStealHandler() {
		String json = """
				{
				    "type": "forgero:life_steal",
				    "target": "minecraft:targeted_entity",
				    "amount": 2.0
				}
				""";
		return Utils.handlerFromString(json, LifeStealHandler.BUILDER);
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "LightningStrikeHandlerTest", tickLimit = 120)
	public void testLightningStrikeHandler(TestContext context) {
		TestPos playerPos = TestPos.of(new BlockPos(3, 1, 3), context);
		createFloor(context);

		PlayerEntity player = PlayerFactory.of(context, playerPos);

		// Spawn a target entity (e.g., a zombie) that will be struck by lightning
		Entity target = context.spawnEntity(EntityType.ZOMBIE, playerPos.offset(1, 0, 1).relative());

		// Create an instance of LightningStrikeHandler from JSON
		LightningStrikeHandler lightningStrikeHandler = lightningStrikeHandler();

		// Trigger the onHit method simulating the player hitting the target
		lightningStrikeHandler.onHit(player, context.getWorld(), target);

		// Check for a lightning bolt entity at the target's location
		context.runAtTick(1, () -> {
			boolean lightningStruck = context.getWorld().getEntitiesByClass(LightningEntity.class, new Box(target.getPos().subtract(3, 3, 3), target.getPos().add(3, 3, 3)), e -> true).size() == 1;
			context.assertTrue(lightningStruck, "A lightning bolt was not spawned at the target entity's location.");

			context.complete();
		});
	}

	public static LightningStrikeHandler lightningStrikeHandler() {
		String json = """
				{
				    "type": "minecraft:lightning_strike",
				    "target": "minecraft:targeted_entity"
				}
				""";
		return Utils.handlerFromString(json, LightningStrikeHandler.BUILDER);
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "StatusEffectHandlerTest", tickLimit = 120)
	public void testStatusEffectHandler(TestContext context) {
		TestPos playerPos = TestPos.of(new BlockPos(3, 1, 3), context);
		createFloor(context);

		PlayerEntity player = PlayerFactory.of(context, playerPos);

		// Spawn a target entity (e.g., a zombie) that will receive the status effect
		LivingEntity target = context.spawnEntity(EntityType.PIG, playerPos.offset(1, 0, 1).relative());

		// Create an instance of StatusEffectHandler from JSON
		StatusEffectHandler statusEffectHandler = statusEffectHandler();

		// Trigger the onHit method simulating the player hitting the target
		statusEffectHandler.onHit(player, context.getWorld(), target);

		// Verify the status effect is applied correctly
		context.runAtTick(1, () -> {
			boolean hasEffect = target.hasStatusEffect(StatusEffects.POISON);
			context.assertTrue(hasEffect, "Target entity does not have the expected Poison effect.");

			if (hasEffect) {
				StatusEffectInstance effectInstance = target.getStatusEffect(StatusEffects.POISON);
				context.assertTrue(0 == effectInstance.getAmplifier(), "Poison effect level is not as expected.");
				context.assertTrue(599 == effectInstance.getDuration(), "Poison effect duration is not as expected.");
			}

			context.complete();
		});
	}

	public static StatusEffectHandler statusEffectHandler() {
		String json = """
				{
				  "type": "minecraft:status_effect",
				   "target": "minecraft:targeted_entity",
				   "effect": "minecraft:poison",
				   "level": 1,
				   "duration": 600
				}
				""";
		return Utils.handlerFromString(json, StatusEffectHandler.BUILDER);
	}
}
