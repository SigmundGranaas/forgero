package com.sigmundgranaas.forgero.fabric.gametest;

import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.AssemblyStationBlock;
import com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.AssemblyStationScreenHandler;
import com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.entity.AssemblyStationBlockEntity;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import com.sigmundgranaas.forgero.testutil.PlayerFactory;
import com.sigmundgranaas.forgero.testutil.TestPos;
import com.sigmundgranaas.forgero.testutil.Utils;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

import static org.junit.jupiter.api.Assertions.*;

public class AssemblyStationTester {

	private TestPos stationPos;
	private AssemblyStationBlockEntity blockEntity;
	private ServerPlayerEntity player;
	private StateService stateService;

	// Helper setup method for tests that require a block in the world
	public void setup(TestContext context) {
		stationPos = TestPos.of(new BlockPos(1, 1, 1), context);
		context.setBlockState(stationPos.relative(), AssemblyStationBlock.ASSEMBLY_STATION_BLOCK.getDefaultState());
		// Wait a tick for the BlockEntity to be created
		context.waitAndRun(1, () -> {
			var be = context.getWorld().getBlockEntity(stationPos.absolute());
			assertTrue(be instanceof AssemblyStationBlockEntity, "Should be an assembly station block entity at " + stationPos.absolute());
			blockEntity = (AssemblyStationBlockEntity) be;
			player = PlayerFactory.builder(context).pos(stationPos.absolute().up()).build().createPlayer();
			stateService = StateService.INSTANCE;
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "assembly_station_test", required = true)
	public void unableToDisassembleDamagedTool(TestContext context) {
		setup(context);
		context.runAtTick(2, () -> {
			var state = stateService.find("forgero:diamond-pickaxe").orElseThrow();
			var tool = stateService.convert(state).orElse(ItemStack.EMPTY);
			tool.setDamage(1); // Set damage

			var handler = new AssemblyStationScreenHandler(1, player.getInventory(), ScreenHandlerContext.create(context.getWorld(), stationPos.absolute()), blockEntity.getDisassemblyInventory(), blockEntity.getResultInventory(), blockEntity.isInputItemConsumed(), blockEntity.getExpectedResultCount());

			assertFalse(handler.getSlot(0).canInsert(tool), "Should not be able to disassemble damaged tools!");

			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "assembly_station_test")
	public void testSimpleDisassembly(TestContext context) {
		setup(context);

		context.runAtTick(2, () -> {
			ItemStack pickaxe = Utils.fromId("forgero:diamond-pickaxe");

			var handler = new AssemblyStationScreenHandler(0, player.getInventory(), ScreenHandlerContext.create(context.getWorld(), stationPos.absolute()), blockEntity.getDisassemblyInventory(), blockEntity.getResultInventory(), blockEntity.isInputItemConsumed(), blockEntity.getExpectedResultCount());
			handler.getSlot(0).setStack(pickaxe.copy());

			ItemStack head = blockEntity.getResultInventory().getStack(0);
			ItemStack handle = blockEntity.getResultInventory().getStack(1);

			assertNotNull(stateService.convert(head).orElse(null), "Head should be a valid Forgero state");
			assertNotNull(stateService.convert(handle).orElse(null), "Handle should be a valid Forgero state");

			assertEquals("diamond-pickaxe_head", stateService.convert(head).get().name());
			assertEquals("oak-handle", stateService.convert(handle).get().name());
			assertTrue(blockEntity.getResultInventory().getStack(2).isEmpty(), "There should be no third item");

			assertEquals(pickaxe.getItem(), blockEntity.getDisassemblyInventory().getStack(0).getItem());

			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "assembly_station_test")
	public void testDisassemblyWithBinding(TestContext context) {
		setup(context);
		context.runAtTick(2, () -> {
			State pickaxeState = stateService.find("forgero:diamond-pickaxe").get();
			State bindingState = stateService.find("forgero:leather-scrappy_binding").get();
			pickaxeState = ((Composite) pickaxeState).upgrade(bindingState);
			ItemStack pickaxeWithBinding = stateService.convert(pickaxeState).get();

			var handler = new AssemblyStationScreenHandler(0, player.getInventory(), ScreenHandlerContext.create(context.getWorld(), stationPos.absolute()), blockEntity.getDisassemblyInventory(), blockEntity.getResultInventory(), blockEntity.isInputItemConsumed(), blockEntity.getExpectedResultCount());
			handler.getSlot(0).setStack(pickaxeWithBinding.copy());

			ItemStack head = blockEntity.getResultInventory().getStack(0);
			ItemStack handle = blockEntity.getResultInventory().getStack(1);
			ItemStack binding = blockEntity.getResultInventory().getStack(2);

			assertEquals("diamond-pickaxe_head", stateService.convert(head).get().name());
			assertEquals("oak-handle", stateService.convert(handle).get().name());
			assertEquals("leather-scrappy_binding", stateService.convert(binding).get().name());
			assertTrue(blockEntity.getResultInventory().getStack(3).isEmpty());

			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "assembly_station_test")
	public void testStatePersistenceOnClose(TestContext context) {
		setup(context);

		context.runAtTick(2, () -> {
			ItemStack pickaxe = Utils.fromId("forgero:diamond-pickaxe");
			var handler = new AssemblyStationScreenHandler(0, player.getInventory(), ScreenHandlerContext.create(context.getWorld(), stationPos.absolute()), blockEntity.getDisassemblyInventory(), blockEntity.getResultInventory(), blockEntity.isInputItemConsumed(), blockEntity.getExpectedResultCount());
			handler.getSlot(0).setStack(pickaxe.copy());
		});

		context.runAtTick(5, () -> {
			// Re-read block entity from world to ensure we're not just looking at the cached Java object
			var reloadedBlockEntity = (AssemblyStationBlockEntity) context.getWorld().getBlockEntity(stationPos.absolute());
			assertNotNull(reloadedBlockEntity);

			assertFalse(reloadedBlockEntity.isInputItemConsumed(), "Input item should not be consumed yet");
			assertTrue(reloadedBlockEntity.getExpectedResultCount() > 0, "Expected result count should be greater than 0");
			assertFalse(reloadedBlockEntity.getDisassemblyInventory().isEmpty(), "Disassembly inventory should not be empty");
			assertFalse(reloadedBlockEntity.getResultInventory().isEmpty(), "Result inventory should not be empty");
			ItemStack head = reloadedBlockEntity.getResultInventory().getStack(0);
			assertEquals("diamond-pickaxe_head", stateService.convert(head).get().name());
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "assembly_station_test")
	public void testInputConsumedOnResultPickup(TestContext context) {
		setup(context);
		context.runAtTick(2, () -> {
			ItemStack pickaxe = Utils.fromId("forgero:diamond-pickaxe");
			var handler = new AssemblyStationScreenHandler(0, player.getInventory(), ScreenHandlerContext.create(context.getWorld(), stationPos.absolute()), blockEntity.getDisassemblyInventory(), blockEntity.getResultInventory(), blockEntity.isInputItemConsumed(), blockEntity.getExpectedResultCount());
			handler.getSlot(0).setStack(pickaxe.copy());

			// Simulate player taking an item from a result slot
			handler.quickMove(player, 1); // quickMove from slot 1 (first result slot)
		});

		context.runAtTick(4, () -> {
			assertTrue(blockEntity.getDisassemblyInventory().isEmpty(), "Input item should be consumed and removed from slot");
			assertTrue(blockEntity.isInputItemConsumed(), "inputItemConsumed flag should be true");
			assertFalse(blockEntity.getResultInventory().isEmpty(), "Other result items should still be present");

			// One item was moved, so we check the handle.
			ItemStack handle = blockEntity.getResultInventory().getStack(1);
			assertEquals("oak-handle", stateService.convert(handle).get().name());
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "assembly_station_test", tickLimit = 20)
	public void testInputSlotLockingAndReset(TestContext context) {
		setup(context);

		context.runAtTick(2, () -> {
			ItemStack pickaxe = Utils.fromId("forgero:diamond-pickaxe");
			var handler = new AssemblyStationScreenHandler(0, player.getInventory(), ScreenHandlerContext.create(context.getWorld(), stationPos.absolute()), blockEntity.getDisassemblyInventory(), blockEntity.getResultInventory(), blockEntity.isInputItemConsumed(), blockEntity.getExpectedResultCount());
			handler.getSlot(0).setStack(pickaxe.copy());

			// Take one item
			handler.quickMove(player, 1); // result slot 1
		});

		context.runAtTick(5, () -> {
			var handler = new AssemblyStationScreenHandler(0, player.getInventory(), ScreenHandlerContext.create(context.getWorld(), stationPos.absolute()), blockEntity.getDisassemblyInventory(), blockEntity.getResultInventory(), blockEntity.isInputItemConsumed(), blockEntity.getExpectedResultCount());
			ItemStack anotherPickaxe = Utils.fromId("forgero:iron-pickaxe");
			assertFalse(handler.getSlot(0).canInsert(anotherPickaxe), "Should not be able to insert item while results are present");

			// Take the last item
			handler.quickMove(player, 2); // The second part is now in the first result slot
		});


		context.runAtTick(8, () -> {
			assertTrue(blockEntity.getResultInventory().isEmpty(), "Result inventory should be empty");
			assertFalse(blockEntity.isInputItemConsumed(), "Station state should be reset (input consumed flag)");
			assertEquals(0, blockEntity.getExpectedResultCount(), "Station state should be reset (expected results count)");

			var handler = new AssemblyStationScreenHandler(0, player.getInventory(), ScreenHandlerContext.create(context.getWorld(), stationPos.absolute()), blockEntity.getDisassemblyInventory(), blockEntity.getResultInventory(), blockEntity.isInputItemConsumed(), blockEntity.getExpectedResultCount());
			ItemStack anotherPickaxe = Utils.fromId("forgero:iron-pickaxe");
			assertTrue(handler.getSlot(0).canInsert(anotherPickaxe), "Should be able to insert item after results are cleared");

			handler.getSlot(0).setStack(anotherPickaxe.copy());
			context.runAtTick(1, () -> {
				assertFalse(blockEntity.getResultInventory().isEmpty(), "New disassembly should have started and populated results");
				context.complete();
			});
		});
	}
}
