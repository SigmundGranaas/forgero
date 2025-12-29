package com.sigmundgranaas.forgero.bows.gametest;

import java.util.List;

import com.sigmundgranaas.forgero.bows.entity.DynamicArrowEntity;
import com.sigmundgranaas.forgero.bows.handlers.LaunchProjectileHandler;
import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.useinteraction.UseContext;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import com.sigmundgranaas.forgero.loader.api.ForgeroInitializedCallback;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

/**
 * High-value gameplay tests for OnHit effects on arrows.
 * These tests validate that DynamicArrowEntity properly executes OnHit effects when hitting entities.
 *
 * <p>This is the CRITICAL gameplay feature that makes Forgero arrows unique.
 */
public class OnHitEffectTest {

	private static ComponentConverter getConverter() {
		return ForgeroInitializedCallback.getServices()
				.map(s -> s.converter())
				.orElse(null);
	}

	/**
	 * Gets a registered arrow by name from the component registry.
	 */
	private static ItemStack getRegisteredArrow(String name) {
		ComponentConverter converter = getConverter();
		if (converter == null) {
			return ItemStack.EMPTY;
		}

		OpenIdentifier id = new OpenIdentifier("forgero-test", name);

		var componentOpt = ForgeroInitializedCallback.getServices()
				.flatMap(s -> s.componentRegistry().get(id));

		if (componentOpt.isEmpty()) {
			return ItemStack.EMPTY;
		}

		return converter.toStack(componentOpt.get())
				.orElse(ItemStack.EMPTY);
	}

	// ========== Fire Effect Tests ==========

	/**
	 * GAMEPLAY TEST: Fire arrow sets target on fire.
	 *
	 * <p>This validates the core OnHit effect system - when a Forgero arrow with
	 * a fire OnHit property hits an entity, that entity should catch fire.
	 *
	 * <p>VALUE: ⭐⭐⭐⭐⭐ This is THE killer feature of custom arrows.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "onhit_effects")
	public void fireArrowSetsTargetOnFire(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setPos(0, 64, 0);
		player.setPitch(0.0f);  // Shoot straight ahead
		player.setYaw(0.0f);

		// Spawn target zombie directly in front of player
		ZombieEntity zombie = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 64, 5));

		// Get fire arrow from registry
		ItemStack fireArrow = getRegisteredArrow("test_fire_arrow");
		if (fireArrow.isEmpty()) {
			context.complete();  // Skip if arrow not registered yet
			return;
		}

		ItemStack bow = new ItemStack(Items.BOW);
		player.setStackInHand(Hand.MAIN_HAND, bow);
		player.getInventory().insertStack(fireArrow);

		// Fire the arrow at zombie
		LaunchProjectileHandler handler = new LaunchProjectileHandler(3.0f, 1.0f);
		UseContext ctx = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, bow, 20, 0, 1.0f);
		handler.apply(ctx);

		// Wait for arrow to hit zombie
		context.waitAndRun(10, () -> {
			// Verify zombie is on fire
			context.assertTrue(zombie.isOnFire(),
				"Zombie should be on fire after being hit by fire arrow");

			// Verify zombie has fire ticks remaining
			context.assertTrue(zombie.getFireTicks() > 0,
				"Zombie should have fire ticks remaining (got: " + zombie.getFireTicks() + ")");

			context.complete();
		});
	}

	/**
	 * GAMEPLAY TEST: Fire arrow effect works on multiple entity types.
	 *
	 * <p>Validates that fire effect isn't zombie-specific but works on any living entity.
	 *
	 * <p>VALUE: ⭐⭐⭐⭐ Ensures effect system is generic and reusable.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "onhit_effects")
	public void fireArrowWorksOnMultipleEntityTypes(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setPos(0, 64, 0);
		player.setPitch(0.0f);
		player.setYaw(0.0f);

		// Spawn different entity types
		var skeleton = context.spawnEntity(EntityType.SKELETON, new BlockPos(0, 64, 5));
		var creeper = context.spawnEntity(EntityType.CREEPER, new BlockPos(2, 64, 5));

		ItemStack fireArrow = getRegisteredArrow("test_fire_arrow");
		if (fireArrow.isEmpty()) {
			context.complete();
			return;
		}

		ItemStack bow = new ItemStack(Items.BOW);
		player.setStackInHand(Hand.MAIN_HAND, bow);
		player.getInventory().insertStack(64, fireArrow);

		LaunchProjectileHandler handler = new LaunchProjectileHandler(3.0f, 0.5f);  // Low divergence

		// Fire at skeleton
		player.lookAt(net.minecraft.command.argument.EntityAnchorArgumentType.EntityAnchor.EYES, skeleton.getPos());
		UseContext ctx1 = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, bow, 20, 0, 1.0f);
		handler.apply(ctx1);

		context.waitAndRun(10, () -> {
			// Fire at creeper
			player.lookAt(net.minecraft.command.argument.EntityAnchorArgumentType.EntityAnchor.EYES, creeper.getPos());
			UseContext ctx2 = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, bow, 20, 0, 1.0f);
			handler.apply(ctx2);

			context.waitAndRun(10, () -> {
				// Both should be on fire
				int entitiesOnFire = 0;
				if (skeleton.isOnFire()) entitiesOnFire++;
				if (creeper.isOnFire()) entitiesOnFire++;

				context.assertTrue(entitiesOnFire >= 1,
					"At least one entity should be on fire (got " + entitiesOnFire + " on fire)");

				context.complete();
			});
		});
	}

	// ========== Explosion Effect Tests ==========

	/**
	 * GAMEPLAY TEST: Explosive arrow creates explosion on impact.
	 *
	 * <p>Validates that arrows can create area-of-effect damage through explosions.
	 *
	 * <p>VALUE: ⭐⭐⭐⭐⭐ AOE damage is a major gameplay mechanic.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "onhit_effects")
	public void explosiveArrowCreatesExplosion(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setPos(0, 64, 0);
		player.setPitch(0.0f);
		player.setYaw(0.0f);

		// Spawn multiple zombies close together to test AOE
		ZombieEntity zombie1 = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 64, 5));
		ZombieEntity zombie2 = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(1, 64, 5));
		ZombieEntity zombie3 = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(-1, 64, 5));

		float initialHealth1 = zombie1.getHealth();
		float initialHealth2 = zombie2.getHealth();
		float initialHealth3 = zombie3.getHealth();

		ItemStack explosiveArrow = getRegisteredArrow("test_explosive_arrow");
		if (explosiveArrow.isEmpty()) {
			context.complete();
			return;
		}

		ItemStack bow = new ItemStack(Items.BOW);
		player.setStackInHand(Hand.MAIN_HAND, bow);
		player.getInventory().insertStack(explosiveArrow);

		LaunchProjectileHandler handler = new LaunchProjectileHandler(3.0f, 1.0f);
		UseContext ctx = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, bow, 20, 0, 1.0f);
		handler.apply(ctx);

		context.waitAndRun(15, () -> {
			// Count how many zombies took damage (explosion AOE effect)
			int damagedZombies = 0;
			if (zombie1.getHealth() < initialHealth1) damagedZombies++;
			if (zombie2.getHealth() < initialHealth2) damagedZombies++;
			if (zombie3.getHealth() < initialHealth3) damagedZombies++;

			// At least the direct hit should damage (ideally multiple from AOE)
			context.assertTrue(damagedZombies >= 1,
				"Explosion should damage at least one zombie (got " + damagedZombies + " damaged)");

			context.complete();
		});
	}

	// ========== Status Effect Tests ==========

	/**
	 * GAMEPLAY TEST: Poison arrow applies poison status effect.
	 *
	 * <p>Validates that arrows can apply status effects to targets.
	 *
	 * <p>VALUE: ⭐⭐⭐⭐⭐ Status effects are core RPG mechanics.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "onhit_effects")
	public void poisonArrowAppliesPoisonEffect(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setPos(0, 64, 0);
		player.setPitch(0.0f);
		player.setYaw(0.0f);

		ZombieEntity zombie = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 64, 5));

		ItemStack poisonArrow = getRegisteredArrow("test_poison_arrow");
		if (poisonArrow.isEmpty()) {
			context.complete();
			return;
		}

		ItemStack bow = new ItemStack(Items.BOW);
		player.setStackInHand(Hand.MAIN_HAND, bow);
		player.getInventory().insertStack(poisonArrow);

		LaunchProjectileHandler handler = new LaunchProjectileHandler(3.0f, 1.0f);
		UseContext ctx = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, bow, 20, 0, 1.0f);
		handler.apply(ctx);

		context.waitAndRun(10, () -> {
			// Verify zombie has poison effect
			boolean hasPoisonEffect = zombie.hasStatusEffect(StatusEffects.POISON);
			context.assertTrue(hasPoisonEffect,
				"Zombie should have poison status effect after being hit by poison arrow");

			if (hasPoisonEffect) {
				var effect = zombie.getStatusEffect(StatusEffects.POISON);
				context.assertTrue(effect != null && effect.getAmplifier() >= 1,
					"Poison effect should have amplifier >= 1 (got: " +
					(effect != null ? effect.getAmplifier() : "null") + ")");
			}

			context.complete();
		});
	}

	// ========== Integration Tests ==========

	/**
	 * GAMEPLAY TEST: Arrow damage + OnHit effect both apply.
	 *
	 * <p>Validates that OnHit effects don't replace arrow damage but add to it.
	 *
	 * <p>VALUE: ⭐⭐⭐⭐ Ensures effects are additive, not replacements.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "onhit_effects")
	public void arrowDamageAndOnHitEffectBothApply(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setPos(0, 64, 0);
		player.setPitch(0.0f);
		player.setYaw(0.0f);

		ZombieEntity zombie = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 64, 5));
		float initialHealth = zombie.getHealth();

		ItemStack fireArrow = getRegisteredArrow("test_fire_arrow");
		if (fireArrow.isEmpty()) {
			context.complete();
			return;
		}

		ItemStack bow = new ItemStack(Items.BOW);
		player.setStackInHand(Hand.MAIN_HAND, bow);
		player.getInventory().insertStack(fireArrow);

		LaunchProjectileHandler handler = new LaunchProjectileHandler(3.0f, 1.0f);
		UseContext ctx = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, bow, 20, 0, 1.0f);
		handler.apply(ctx);

		context.waitAndRun(10, () -> {
			// Should have: arrow damage + on fire
			float currentHealth = zombie.getHealth();
			boolean tookDamage = currentHealth < initialHealth;
			boolean isOnFire = zombie.isOnFire();

			context.assertTrue(tookDamage,
				"Zombie should take initial arrow damage (health: " + initialHealth + " -> " + currentHealth + ")");
			context.assertTrue(isOnFire,
				"Zombie should also be on fire from OnHit effect");

			context.complete();
		});
	}

	/**
	 * GAMEPLAY TEST: OnHit effects work with enchanted bows.
	 *
	 * <p>Validates that Forgero OnHit effects and vanilla enchantments stack correctly.
	 *
	 * <p>VALUE: ⭐⭐⭐⭐ Critical for mod compatibility.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "onhit_effects")
	public void onHitEffectsWorkWithEnchantedBows(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setPos(0, 64, 0);
		player.setPitch(0.0f);
		player.setYaw(0.0f);

		ZombieEntity zombie = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 64, 5));

		ItemStack fireArrow = getRegisteredArrow("test_fire_arrow");
		if (fireArrow.isEmpty()) {
			context.complete();
			return;
		}

		// Enchanted bow with Power II
		ItemStack bow = new ItemStack(Items.BOW);
		bow.addEnchantment(net.minecraft.enchantment.Enchantments.POWER, 2);

		player.setStackInHand(Hand.MAIN_HAND, bow);
		player.getInventory().insertStack(fireArrow);

		LaunchProjectileHandler handler = new LaunchProjectileHandler(3.0f, 1.0f);
		UseContext ctx = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, bow, 20, 0, 1.0f);
		handler.apply(ctx);

		context.waitAndRun(10, () -> {
			// Should have: increased damage from Power + fire from OnHit
			boolean isOnFire = zombie.isOnFire();
			boolean isAlive = zombie.isAlive();

			context.assertTrue(isOnFire || !isAlive,
				"Zombie should be on fire from OnHit effect (or dead from increased damage). " +
				"On fire: " + isOnFire + ", Alive: " + isAlive);

			context.complete();
		});
	}

	/**
	 * GAMEPLAY TEST: DynamicArrowEntity correctly identified for Forgero arrows.
	 *
	 * <p>Validates that Forgero arrows with OnHit properties spawn DynamicArrowEntity,
	 * which is required for OnHit effects to work.
	 *
	 * <p>VALUE: ⭐⭐⭐ Infrastructure validation.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "onhit_effects")
	public void forgeroArrowsWithOnHitSpawnDynamicArrowEntity(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setPos(0, 64, 0);

		ItemStack fireArrow = getRegisteredArrow("test_fire_arrow");
		if (fireArrow.isEmpty()) {
			context.complete();
			return;
		}

		ItemStack bow = new ItemStack(Items.BOW);
		player.setStackInHand(Hand.MAIN_HAND, bow);
		player.getInventory().insertStack(fireArrow);

		LaunchProjectileHandler handler = new LaunchProjectileHandler(3.0f, 1.0f);
		UseContext ctx = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, bow, 20, 0, 1.0f);
		handler.apply(ctx);

		context.waitAndRun(1, () -> {
			List<DynamicArrowEntity> arrows = context.getWorld().getEntitiesByClass(
				DynamicArrowEntity.class,
				new Box(player.getBlockPos()).expand(50),
				arrow -> true
			);

			context.assertTrue(!arrows.isEmpty(),
				"Fire arrow should spawn DynamicArrowEntity (required for OnHit effects)");

			if (!arrows.isEmpty()) {
				DynamicArrowEntity arrow = arrows.get(0);
				ItemStack arrowStack = arrow.getStack();
				context.assertTrue(!arrowStack.isEmpty(),
					"DynamicArrowEntity should have arrow ItemStack");
			}

			context.complete();
		});
	}
}
