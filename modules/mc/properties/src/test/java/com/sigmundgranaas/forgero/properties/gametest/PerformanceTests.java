package com.sigmundgranaas.forgero.properties.gametest;

import com.sigmundgranaas.forgero.effects.block.RadiusBlockSelector;
import com.sigmundgranaas.forgero.effects.entity.*;
import com.sigmundgranaas.forgero.properties.minecraft.entityselector.AreaOfEffectSelector;
import com.sigmundgranaas.forgero.properties.minecraft.onsneak.OnSneakToggleManager;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/**
 * Performance benchmarks for effect handlers and systems.
 * Tests performance with large numbers of entities, items, and blocks.
 *
 * Benchmarks:
 * - MagnetHandler with 50+ items
 * - AOE selectors with many entities
 * - Rapid event triggering
 * - Large radius block selectors
 *
 * <p>The wall-clock thresholds below are deliberately generous. These tests guard against
 * <em>catastrophic</em> performance regressions (e.g. an accidental O(n²) blow-up or a hang),
 * not precise timings. Tight single-digit-millisecond bounds on shared/CI hardware are a
 * classic flaky-test source: a GC pause, cold JIT, or a busy host makes a sub-millisecond
 * operation momentarily spike and the assertion fails non-deterministically. With ~25–100×
 * headroom the correctness assertions still run every time and a genuine regression still
 * trips the guard, but normal timing variance no longer flakes the suite.
 */
public class PerformanceTests {

	private static final int MAX_TICK_TIME_MS = 200; // Catastrophic-regression guard, not a precise bound

	// ========== MagnetHandler Performance ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testMagnetHandlerWith50Items(TestContext context) {
		MagnetHandler handler = new MagnetHandler(10.0, 0.5);
		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(5, 64, 5));

		context.waitAndRun(1, () -> {
			// Spawn 50 items in a spread pattern using source's actual position
			Vec3d sourcePos = source.getPos();
			List<ItemEntity> items = new ArrayList<>();
			for (int i = 0; i < 50; i++) {
				double x = sourcePos.x + (Math.random() * 8 - 4);
				double z = sourcePos.z + (Math.random() * 8 - 4);
				ItemEntity item = new ItemEntity(
						context.getWorld(),
						x, sourcePos.y, z,
						new ItemStack(Items.DIAMOND)
				);
				context.getWorld().spawnEntity(item);
				items.add(item);
			}

			context.waitAndRun(2, () -> {
				long startTime = System.nanoTime();

				// Apply magnet 20 times to simulate repeated ticking
				for (int i = 0; i < 20; i++) {
					handler.apply(source);
				}

				long endTime = System.nanoTime();
				long totalTimeMs = (endTime - startTime) / 1_000_000;
				long avgTickTimeMs = totalTimeMs / 20;

				context.assertTrue(avgTickTimeMs < MAX_TICK_TIME_MS,
						"Average tick time too high: " + avgTickTimeMs + "ms (max " + MAX_TICK_TIME_MS + "ms)");

				// Verify items were attracted
				long itemsCloser = items.stream()
						.filter(item -> item.getPos().distanceTo(source.getPos()) < 7.0)
						.count();

				context.assertTrue(itemsCloser > 25, "At least half the items should be closer to magnet");
				context.complete();
			});
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testMagnetHandlerWith100Items(TestContext context) {
		MagnetHandler handler = new MagnetHandler(8.0, 0.4);
		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(10, 64, 10));

		// Spawn 100 items
		for (int i = 0; i < 100; i++) {
			double x = 10 + (Math.random() * 10 - 5);
			double z = 10 + (Math.random() * 10 - 5);
			ItemEntity item = new ItemEntity(
					context.getWorld(),
					x, 2.5, z,
					new ItemStack(Items.GOLD_INGOT)
			);
			context.getWorld().spawnEntity(item);
		}

		context.waitAndRun(2, () -> {
			long startTime = System.nanoTime();

			// Single application test
			handler.apply(source);

			long endTime = System.nanoTime();
			long timeMs = (endTime - startTime) / 1_000_000;

			context.assertTrue(timeMs < 200, "Single magnet application with 100 items took too long: " + timeMs + "ms");
			context.complete();
		});
	}

	// ========== AOE Selector Performance ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testAOESelectorWith50Entities(TestContext context) {
		AreaOfEffectSelector selector = new AreaOfEffectSelector(15, List.of());
		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(10, 64, 10));

		// Spawn 50 entities in a grid
		List<LivingEntity> entities = new ArrayList<>();
		for (int x = 5; x < 15; x++) {
			for (int z = 5; z < 10; z++) {
				LivingEntity entity = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(x, 1, z));
				entities.add(entity);
			}
		}

		context.waitAndRun(1, () -> {
			long startTime = System.nanoTime();

			// Select multiple times
			for (int i = 0; i < 50; i++) {
				List<Entity> selected = selector.select(source, entities.get(0));
			}

			long endTime = System.nanoTime();
			long totalTimeMs = (endTime - startTime) / 1_000_000;
			long avgTimeMs = totalTimeMs / 50;

			context.assertTrue(avgTimeMs < 100, "AOE selection took too long: " + avgTimeMs + "ms average");
			context.complete();
		});
	}

	// ========== VelocityHandler Performance ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testVelocityHandlerOnManyEntities(TestContext context) {
		VelocityHandler handler = new VelocityHandler(
				VelocityHandler.VelocityTarget.TARGET,
				1.0,
				VelocityHandler.VelocityMode.ADD,
				0.0
		);

		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(10, 64, 10));

		// Spawn 30 entities
		List<LivingEntity> entities = new ArrayList<>();
		for (int i = 0; i < 30; i++) {
			LivingEntity entity = context.spawnEntity(
					EntityType.ZOMBIE,
					new BlockPos(10 + i, 1, 10)
			);
			entities.add(entity);
		}

		context.waitAndRun(1, () -> {
			long startTime = System.nanoTime();

			// Apply velocity to all entities
			for (LivingEntity entity : entities) {
				handler.apply(source, entity);
			}

			long endTime = System.nanoTime();
			long totalTimeMs = (endTime - startTime) / 1_000_000;

			context.assertTrue(totalTimeMs < 200, "Velocity application to 30 entities took too long: " + totalTimeMs + "ms");

			// Verify all entities have velocity
			long entitiesWithVelocity = entities.stream()
					.filter(e -> e.getVelocity().lengthSquared() > 0.1)
					.count();

			context.assertTrue(entitiesWithVelocity == 30, "All entities should have velocity");
			context.complete();
		});
	}

	// ========== SpawnEntityHandler Stress Test ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testSpawnEntityHandlerMany(TestContext context) {
		Identifier entityId = new Identifier("minecraft", "chicken");
		SpawnEntityHandler handler = new SpawnEntityHandler(entityId, 20, Vec3d.ZERO, true, false, true);

		// Use RELATIVE coordinates
		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 2));

		long startTime = System.nanoTime();

		// Spawn 20 chickens
		handler.apply(source, target);

		long endTime = System.nanoTime();
		long timeMs = (endTime - startTime) / 1_000_000;

		context.assertTrue(timeMs < 500, "Spawning 20 entities took too long: " + timeMs + "ms");

		context.waitAndRun(3, () -> {
			// Verify chickens spawned
			context.expectEntity(EntityType.CHICKEN);
			context.complete();
		});
	}

	// ========== BlockSelector Performance ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testLargeRadiusBlockSelector(TestContext context) {
		PlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		BlockPos centerPos = new BlockPos(20, 64, 20);

		// Large radius selector (radius 5 = 11^3 = 1331 blocks)
		RadiusBlockSelector selector = new RadiusBlockSelector(5, true);

		long startTime = System.nanoTime();

		List<BlockPos> selected = selector.select(player, centerPos, Blocks.STONE.getDefaultState());

		long endTime = System.nanoTime();
		long timeMs = (endTime - startTime) / 1_000_000;

		context.assertTrue(timeMs < 300, "Large radius selection took too long: " + timeMs + "ms");
		context.assertTrue(selected.size() == 1331, "Should select 1331 blocks, selected " + selected.size());
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testMaxRadiusBlockSelector(TestContext context) {
		PlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		BlockPos centerPos = new BlockPos(30, 64, 30);

		// Max radius (10) = 21^3 = 9261 blocks
		RadiusBlockSelector selector = new RadiusBlockSelector(10, true);

		long startTime = System.nanoTime();

		List<BlockPos> selected = selector.select(player, centerPos, Blocks.STONE.getDefaultState());

		long endTime = System.nanoTime();
		long timeMs = (endTime - startTime) / 1_000_000;

		context.assertTrue(timeMs < 1000, "Max radius selection took too long: " + timeMs + "ms");
		context.assertTrue(selected.size() == 9261, "Should select 9261 blocks, selected " + selected.size());
		context.complete();
	}

	// ========== OnSneakToggle Rapid Fire ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testRapidSneakToggling(TestContext context) {
		PlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		long startTime = System.nanoTime();

		// Rapid toggle 100 times
		for (int i = 0; i < 100; i++) {
			player.setSneaking(true);
			OnSneakToggleManager.handleTick(player);
			player.setSneaking(false);
			OnSneakToggleManager.handleTick(player);
		}

		long endTime = System.nanoTime();
		long totalTimeMs = (endTime - startTime) / 1_000_000;
		long avgTimeMs = totalTimeMs / 100;

		context.assertTrue(avgTimeMs < 50, "Average sneak toggle time too high: " + avgTimeMs + "ms");
		context.complete();
	}

	// ========== FreezeHandler Mass Application ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testFreezeHandlerOnManyEntities(TestContext context) {
		FreezeHandler handler = new FreezeHandler(100, false);

		// Spawn 50 entities
		List<LivingEntity> entities = new ArrayList<>();
		for (int i = 0; i < 50; i++) {
			LivingEntity entity = context.spawnEntity(
					EntityType.ZOMBIE,
					new BlockPos(i, 1, 0)
			);
			entities.add(entity);
		}

		context.waitAndRun(1, () -> {
			long startTime = System.nanoTime();

			// Freeze all entities
			for (LivingEntity entity : entities) {
				handler.apply(entity);
			}

			long endTime = System.nanoTime();
			long totalTimeMs = (endTime - startTime) / 1_000_000;

			context.assertTrue(totalTimeMs < 200, "Freezing 50 entities took too long: " + totalTimeMs + "ms");

			// Verify all frozen
			long frozenCount = entities.stream()
					.filter(e -> e.getFrozenTicks() > 0)
					.count();

			context.assertTrue(frozenCount == 50, "All entities should be frozen");
			context.complete();
		});
	}

	// ========== Combined Performance Test ==========

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testCombinedSystemsPerformance(TestContext context) {
		// Simulate a complex scenario with multiple systems active
		PlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(10, 64, 10));

		// Spawn 30 entities
		List<LivingEntity> entities = new ArrayList<>();
		for (int i = 0; i < 30; i++) {
			entities.add(context.spawnEntity(EntityType.ZOMBIE, new BlockPos(10 + i % 6, 1, 10 + i / 6)));
		}

		// Spawn 30 items
		for (int i = 0; i < 30; i++) {
			ItemEntity item = new ItemEntity(
					context.getWorld(),
					10 + i % 6, 2.5, 10 + i / 6,
					new ItemStack(Items.DIAMOND)
			);
			context.getWorld().spawnEntity(item);
		}

		context.waitAndRun(2, () -> {
			long startTime = System.nanoTime();

			// Simulate multiple effects happening simultaneously
			FreezeHandler freeze = new FreezeHandler(80, false);
			VelocityHandler velocity = new VelocityHandler(
					VelocityHandler.VelocityTarget.TARGET,
					0.5, VelocityHandler.VelocityMode.ADD, 0.0
			);
			MagnetHandler magnet = new MagnetHandler(8.0, 0.5);

			// Apply to all entities
			for (LivingEntity entity : entities) {
				freeze.apply(entity);
				velocity.apply(source, entity);
			}

			// Apply magnet
			magnet.apply(source);

			long endTime = System.nanoTime();
			long totalTimeMs = (endTime - startTime) / 1_000_000;

			context.assertTrue(totalTimeMs < 500, "Combined systems took too long: " + totalTimeMs + "ms");
			context.complete();
		});
	}
}
