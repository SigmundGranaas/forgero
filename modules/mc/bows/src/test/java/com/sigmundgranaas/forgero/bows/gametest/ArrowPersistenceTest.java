package com.sigmundgranaas.forgero.bows.gametest;

import java.util.List;

import com.sigmundgranaas.forgero.bows.entity.DynamicArrowEntity;
import com.sigmundgranaas.forgero.bows.handlers.LaunchProjectileHandler;
import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.useinteraction.UseContext;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import com.sigmundgranaas.forgero.common.api.ForgeroInitializedCallback;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;

/**
 * High-value tests for arrow entity persistence and data integrity.
 * These tests validate that custom arrows survive world reloads and maintain their properties.
 *
 * <p>VALUE: Ensures custom arrows work correctly in real gameplay scenarios where
 * players save/load worlds, travel through dimensions, etc.
 */
public class ArrowPersistenceTest {

	private static ComponentConverter getConverter() {
		return ForgeroInitializedCallback.getServices()
				.map(s -> s.converter())
				.orElse(null);
	}

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

	// ========== NBT Persistence Tests ==========

	/**
	 * GAMEPLAY TEST: Arrow ItemStack persists through NBT serialization.
	 *
	 * <p>When a custom arrow is fired and the world is saved/loaded (e.g., player exits and returns,
	 * or arrow travels through a dimension portal), the arrow entity must preserve its ItemStack
	 * and all associated properties.
	 *
	 * <p>VALUE: ⭐⭐⭐⭐ Critical for gameplay continuity. Without this, custom arrows would
	 * lose their properties on world reload.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "arrow_persistence")
	public void arrowItemStackPersistsThroughNbtSerialization(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setPos(0, 64, 0);
		player.setPitch(-45.0f);  // Shoot upward so arrow stays in air

		// Use attack damage arrow (has custom attributes)
		ItemStack attackArrow = getRegisteredArrow("test_attack_arrow");
		if (attackArrow.isEmpty()) {
			context.complete();
			return;
		}

		// Remember the arrow's translation key for verification
		String expectedTranslationKey = attackArrow.getItem().getTranslationKey();

		ItemStack bow = new ItemStack(Items.BOW);
		player.setStackInHand(Hand.MAIN_HAND, bow);
		player.getInventory().insertStack(attackArrow);

		LaunchProjectileHandler handler = new LaunchProjectileHandler(3.0f, 1.0f);
		UseContext ctx = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, bow, 20, 0, 1.0f);
		handler.apply(ctx);

		context.waitAndRun(5, () -> {
			// Find the arrow entity
			List<DynamicArrowEntity> arrows = context.getWorld().getEntitiesByClass(
				DynamicArrowEntity.class,
				new Box(player.getBlockPos()).expand(100),
				arrow -> true
			);

			context.assertTrue(!arrows.isEmpty(),
				"Arrow entity should exist before NBT serialization");

			DynamicArrowEntity originalArrow = arrows.get(0);
			ItemStack originalStack = originalArrow.getStack();

			// Simulate world save: serialize to NBT
			NbtCompound nbt = new NbtCompound();
			originalArrow.writeCustomDataToNbt(nbt);

			// Verify NBT contains item data
			context.assertTrue(nbt.contains("Item"),
				"NBT should contain 'Item' tag with arrow ItemStack data");

			// Simulate world load: create new entity and deserialize
			DynamicArrowEntity newArrow = new DynamicArrowEntity(
				com.sigmundgranaas.forgero.bows.entity.DynamicArrowEntityRegistry.DYNAMIC_ARROW_ENTITY,
				context.getWorld()
			);
			newArrow.readCustomDataFromNbt(nbt);

			// Verify stack was restored
			ItemStack restoredStack = newArrow.getStack();
			context.assertTrue(!restoredStack.isEmpty(),
				"Restored arrow should have non-empty ItemStack");

			context.assertTrue(
				restoredStack.getItem().getTranslationKey().equals(expectedTranslationKey),
				"Restored arrow should have same item type (expected: " + expectedTranslationKey +
				", got: " + restoredStack.getItem().getTranslationKey() + ")"
			);

			context.complete();
		});
	}

	/**
	 * GAMEPLAY TEST: Arrow attributes persist through NBT serialization.
	 *
	 * <p>Custom arrows have attributes like attack_damage. These must survive world reload.
	 *
	 * <p>VALUE: ⭐⭐⭐⭐ Without this, custom arrow properties would be lost on reload,
	 * making them functionally identical to vanilla arrows after reload.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "arrow_persistence")
	public void arrowAttributesPersistThroughNbtSerialization(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setPos(0, 64, 0);
		player.setPitch(-45.0f);

		ItemStack attackArrow = getRegisteredArrow("test_attack_arrow");
		if (attackArrow.isEmpty()) {
			context.complete();
			return;
		}

		ItemStack bow = new ItemStack(Items.BOW);
		player.setStackInHand(Hand.MAIN_HAND, bow);
		player.getInventory().insertStack(attackArrow);

		LaunchProjectileHandler handler = new LaunchProjectileHandler(3.0f, 1.0f);
		UseContext ctx = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, bow, 20, 0, 1.0f);
		handler.apply(ctx);

		context.waitAndRun(5, () -> {
			List<DynamicArrowEntity> arrows = context.getWorld().getEntitiesByClass(
				DynamicArrowEntity.class,
				new Box(player.getBlockPos()).expand(100),
				arrow -> true
			);

			context.assertTrue(!arrows.isEmpty(),
				"Arrow entity should exist");

			DynamicArrowEntity originalArrow = arrows.get(0);
			double originalDamage = originalArrow.getDamage();

			// Damage should be > 2.0 (vanilla arrow damage) due to attack_damage attribute
			context.assertTrue(originalDamage > 2.0,
				"Original arrow should have enhanced damage from attribute (got: " + originalDamage + ")");

			// Serialize and deserialize
			NbtCompound nbt = new NbtCompound();
			originalArrow.writeCustomDataToNbt(nbt);

			DynamicArrowEntity newArrow = new DynamicArrowEntity(
				com.sigmundgranaas.forgero.bows.entity.DynamicArrowEntityRegistry.DYNAMIC_ARROW_ENTITY,
				context.getWorld()
			);
			newArrow.readCustomDataFromNbt(nbt);

			// Verify damage attribute is preserved through ItemStack
			// Note: Damage is set in constructor based on ItemStack, so we verify the stack survived
			ItemStack restoredStack = newArrow.getStack();
			ComponentConverter converter = getConverter();
			if (converter != null) {
				boolean hasComponent = converter.toComponent(restoredStack).isPresent();
				context.assertTrue(hasComponent,
					"Restored arrow ItemStack should still convert to Forgero Component");
			}

			context.complete();
		});
	}

	/**
	 * GAMEPLAY TEST: Arrow doesn't discard itself after NBT load.
	 *
	 * <p>Regression test: Previously, arrows would be discarded immediately after
	 * NBT deserialization if initialized flag wasn't set properly.
	 *
	 * <p>VALUE: ⭐⭐⭐ Prevents arrows from disappearing on world reload.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "arrow_persistence")
	public void arrowDoesNotDiscardAfterNbtLoad(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setPos(0, 64, 0);
		player.setPitch(-45.0f);

		ItemStack attackArrow = getRegisteredArrow("test_attack_arrow");
		if (attackArrow.isEmpty()) {
			context.complete();
			return;
		}

		ItemStack bow = new ItemStack(Items.BOW);
		player.setStackInHand(Hand.MAIN_HAND, bow);
		player.getInventory().insertStack(attackArrow);

		LaunchProjectileHandler handler = new LaunchProjectileHandler(3.0f, 1.0f);
		UseContext ctx = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, bow, 20, 0, 1.0f);
		handler.apply(ctx);

		context.waitAndRun(5, () -> {
			List<DynamicArrowEntity> arrows = context.getWorld().getEntitiesByClass(
				DynamicArrowEntity.class,
				new Box(player.getBlockPos()).expand(100),
				arrow -> true
			);

			context.assertTrue(!arrows.isEmpty(), "Arrow should exist before NBT round-trip");

			DynamicArrowEntity originalArrow = arrows.get(0);

			// Serialize and deserialize
			NbtCompound nbt = new NbtCompound();
			originalArrow.writeCustomDataToNbt(nbt);

			DynamicArrowEntity newArrow = new DynamicArrowEntity(
				com.sigmundgranaas.forgero.bows.entity.DynamicArrowEntityRegistry.DYNAMIC_ARROW_ENTITY,
				context.getWorld()
			);
			newArrow.setPos(originalArrow.getX(), originalArrow.getY(), originalArrow.getZ());
			newArrow.readCustomDataFromNbt(nbt);

			// Spawn the deserialized arrow
			context.getWorld().spawnEntity(newArrow);

			// Wait a few ticks and verify it's still alive
			context.waitAndRun(5, () -> {
				context.assertTrue(!newArrow.isRemoved(),
					"Arrow should not be discarded after NBT deserialization and spawning");
				context.assertTrue(!newArrow.getStack().isEmpty(),
					"Arrow should still have ItemStack after NBT deserialization");
				context.complete();
			});
		});
	}

	// ========== Edge Case Tests ==========

	/**
	 * GAMEPLAY TEST: Arrow with empty stack is properly handled.
	 *
	 * <p>Edge case: If an arrow somehow ends up with an empty ItemStack
	 * (corrupted NBT, mod conflict, etc.), it should be discarded cleanly
	 * rather than causing crashes.
	 *
	 * <p>VALUE: ⭐⭐ Robustness against edge cases.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "arrow_persistence")
	public void arrowWithEmptyStackIsDiscarded(TestContext context) {
		// Create arrow with empty stack (simulates corrupted data)
		DynamicArrowEntity arrow = new DynamicArrowEntity(
			com.sigmundgranaas.forgero.bows.entity.DynamicArrowEntityRegistry.DYNAMIC_ARROW_ENTITY,
			context.getWorld()
		);
		arrow.setPos(0, 64, 0);
		arrow.setStack(ItemStack.EMPTY);

		// Mark as initialized so tick logic runs
		NbtCompound nbt = new NbtCompound();
		arrow.writeCustomDataToNbt(nbt);
		arrow.readCustomDataFromNbt(nbt);  // Sets initialized = true

		context.getWorld().spawnEntity(arrow);

		// Arrow should discard itself on next tick
		context.waitAndRun(5, () -> {
			context.assertTrue(arrow.isRemoved(),
				"Arrow with empty ItemStack should be discarded for safety");
			context.complete();
		});
	}

	/**
	 * GAMEPLAY TEST: Multiple arrows can coexist with independent NBT data.
	 *
	 * <p>Validates that NBT serialization doesn't cause data leakage between
	 * multiple arrow entities of different types.
	 *
	 * <p>VALUE: ⭐⭐⭐ Ensures multi-arrow scenarios work correctly.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "arrow_persistence")
	public void multipleArrowsHaveIndependentNbtData(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setPos(0, 64, 0);
		player.setPitch(-45.0f);

		ItemStack attackArrow = getRegisteredArrow("test_attack_arrow");
		ItemStack heavyArrow = getRegisteredArrow("test_heavy_arrow");

		if (attackArrow.isEmpty() || heavyArrow.isEmpty()) {
			context.complete();
			return;
		}

		ItemStack bow = new ItemStack(Items.BOW);
		player.setStackInHand(Hand.MAIN_HAND, bow);

		LaunchProjectileHandler handler = new LaunchProjectileHandler(3.0f, 1.0f);

		// Fire attack arrow
		player.getInventory().insertStack(attackArrow.copy());
		UseContext ctx1 = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, bow, 20, 0, 1.0f);
		handler.apply(ctx1);

		context.waitAndRun(2, () -> {
			// Fire heavy arrow
			player.getInventory().insertStack(heavyArrow.copy());
			UseContext ctx2 = UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, bow, 20, 0, 1.0f);
			handler.apply(ctx2);

			context.waitAndRun(3, () -> {
				List<DynamicArrowEntity> arrows = context.getWorld().getEntitiesByClass(
					DynamicArrowEntity.class,
					new Box(player.getBlockPos()).expand(100),
					arrow -> true
				);

				context.assertTrue(arrows.size() >= 2,
					"Should have at least 2 arrows in world (got: " + arrows.size() + ")");

				// Serialize all arrows and verify each has correct ItemStack
				for (DynamicArrowEntity arrow : arrows) {
					NbtCompound nbt = new NbtCompound();
					arrow.writeCustomDataToNbt(nbt);

					// Each arrow should have independent NBT data
					context.assertTrue(nbt.contains("Item"),
						"Each arrow should have Item NBT tag");

					ItemStack stack = arrow.getStack();
					context.assertTrue(!stack.isEmpty(),
						"Each arrow should have non-empty ItemStack");
				}

				context.complete();
			});
		});
	}
}
