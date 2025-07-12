package com.sigmundgranaas.forgero.fabric.gametest;

import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.block.upgradestation.UpgradeStationBlock;
import com.sigmundgranaas.forgero.minecraft.common.block.upgradestation.UpgradeStationScreenHandler;
import com.sigmundgranaas.forgero.minecraft.common.block.upgradestation.entity.UpgradeStationBlockEntity;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import com.sigmundgranaas.forgero.testutil.PlayerFactory;
import com.sigmundgranaas.forgero.testutil.TestPos;
import com.sigmundgranaas.forgero.testutil.Utils;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.concurrent.atomic.AtomicReference;

public class UpgradeStationTester {

	private TestPos stationPos;
	private UpgradeStationBlockEntity blockEntity;
	private ServerPlayerEntity player;
	private StateService stateService;

	/**
	 * Sets up the test environment by placing a complete Upgrade Station (both left and right parts)
	 * and creating a mock player. This method waits for the BlockEntity to be initialized.
	 *
	 * @param context The TestContext for the running test.
	 */
	public void setup(TestContext context) {
		// The LEFT part of the station will be placed, which contains the BlockEntity.
		stationPos = TestPos.of(new BlockPos(1, 1, 1), context);
		Direction facing = Direction.NORTH;
		BlockPos leftPos = stationPos.relative();
		BlockPos rightPos = leftPos.offset(facing.rotateCounterclockwise(Direction.Axis.Y));

		// Manually place both parts of the station, as context.setBlockState does not trigger onPlaced
		context.setBlockState(leftPos, UpgradeStationBlock.UPGRADE_STATION_BLOCK.getDefaultState()
				.with(UpgradeStationBlock.FACING, facing)
				.with(UpgradeStationBlock.PART, UpgradeStationBlock.UpgradeStationBlockPart.LEFT));

		context.setBlockState(rightPos, UpgradeStationBlock.UPGRADE_STATION_BLOCK.getDefaultState()
				.with(UpgradeStationBlock.FACING, facing)
				.with(UpgradeStationBlock.PART, UpgradeStationBlock.UpgradeStationBlockPart.RIGHT));

		// Wait a tick for the BlockEntity to be instantiated by the world.
		context.waitAndRun(1, () -> {
			var be = context.getWorld().getBlockEntity(stationPos.absolute());
			context.assertTrue(be instanceof UpgradeStationBlockEntity, "Should be an upgrade station block entity at " + stationPos.absolute());
			blockEntity = (UpgradeStationBlockEntity) be;
			player = PlayerFactory.builder(context).pos(stationPos.absolute().up(2)).build().createPlayer();
			stateService = StateService.INSTANCE;
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "upgrade_station_test", required = true)
	public void testPlaceToolAndSlotsAppear(TestContext context) {
		setup(context);
		context.runAtTick(2, () -> {
			ItemStack pickaxe = Utils.fromId("forgero:diamond-pickaxe");
			var handler = new UpgradeStationScreenHandler(1, player.getInventory(), ScreenHandlerContext.create(context.getWorld(), stationPos.absolute()));

			// Simulate placing the pickaxe in the station
			handler.getSlot(0).setStack(pickaxe);

			// A diamond pickaxe should have slots for its parts and a binding.
			// We check if any of the pooled slots have been enabled and configured.
			long enabledSlots = handler.getSlotPool().stream().filter(Slot::isEnabled).count();
			context.assertTrue(enabledSlots > 0, "Placing a tool should enable the upgrade slots.");

			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "upgrade_station_test", required = true)
	public void testSimpleUpgrade(TestContext context) {
		setup(context);
		context.runAtTick(2, () -> {
			ItemStack pickaxe = Utils.fromId("forgero:diamond-pickaxe");
			ItemStack binding = Utils.fromId("forgero:oak-binding");

			var handler = new UpgradeStationScreenHandler(1, player.getInventory(), ScreenHandlerContext.create(context.getWorld(), stationPos.absolute()));
			handler.getSlot(0).setStack(pickaxe.copy());

			// Find the binding slot and place the binding
			var bindingSlotOpt = handler.getSlotPool().stream()
					.filter(s -> s instanceof UpgradeStationScreenHandler.PositionedSlot)
					.filter(s -> s.isEnabled() && s.getSlot() != null && "TOOL_BINDING".equals(s.getSlot().typeName()))
					.findFirst();

			context.assertTrue(bindingSlotOpt.isPresent(), "Could not find a binding slot for the pickaxe.");
			bindingSlotOpt.get().setStack(binding.copy());

			// The main item should now be upgraded
			ItemStack upgradedPickaxe = handler.getSlot(0).getStack();
			State upgradedState = stateService.convert(upgradedPickaxe).orElse(null);

			context.assertTrue(upgradedState != null, "Upgraded pickaxe could not be converted to a state.");
			context.assertTrue(upgradedState instanceof Composite, "Upgraded item state should be a composite.");
			boolean hasBinding = ((Composite) upgradedState).upgrades().stream().anyMatch(s -> s.name().contains("binding"));
			context.assertTrue(hasBinding, "Pickaxe in main slot was not upgraded with the binding.");

			// Also check the BlockEntity's inventory is synced
			ItemStack stackInBE = blockEntity.getCompositeInventory().getStack(0);
			context.assertFalse(stackInBE.isEmpty(), "Block entity should contain the upgraded item");
			State stateInBE = stateService.convert(stackInBE).orElse(null);
			context.assertTrue(stateInBE != null, "Item in BE could not be converted to a state.");
			boolean hasBindingInBE = ((Composite) stateInBE).upgrades().stream().anyMatch(s -> s.name().contains("binding"));
			context.assertTrue(hasBindingInBE, "Item in block entity was not upgraded.");

			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "upgrade_station_test")
	public void testInvalidUpgrade(TestContext context) {
		setup(context);
		context.runAtTick(2, () -> {
			ItemStack pickaxe = Utils.fromId("forgero:diamond-pickaxe");
			ItemStack stick = new ItemStack(Items.STICK);

			var handler = new UpgradeStationScreenHandler(1, player.getInventory(), ScreenHandlerContext.create(context.getWorld(), stationPos.absolute()));
			handler.getSlot(0).setStack(pickaxe.copy());

			var bindingSlotOpt = handler.getSlotPool().stream()
					.filter(s -> s instanceof UpgradeStationScreenHandler.PositionedSlot)
					.filter(s -> s.isEnabled() && s.getSlot() != null && "TOOL_BINDING".equals(s.getSlot().typeName()))
					.findFirst();

			context.assertTrue(bindingSlotOpt.isPresent(), "Could not find a binding slot for the pickaxe.");
			var bindingSlot = bindingSlotOpt.get();

			// Attempt to insert an invalid item
			context.assertFalse(bindingSlot.canInsert(stick), "Should not be able to insert a stick into a binding slot.");

			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "upgrade_station_test")
	public void testRemovingUpgradeRevertsTool(TestContext context) {
		setup(context);

		AtomicReference<UpgradeStationScreenHandler> handler = new AtomicReference<>();

		context.runAtTick(2, () -> {
			ItemStack pickaxe = Utils.fromId("forgero:diamond-pickaxe");
			ItemStack binding = Utils.fromId("forgero:oak-binding");

			handler.set(new UpgradeStationScreenHandler(1, player.getInventory(), ScreenHandlerContext.create(context.getWorld(), stationPos.absolute())));

			handler.get().getSlot(0).setStack(pickaxe.copy());

			var bindingSlotOpt = handler.get().getSlotPool().stream()
					.filter(s -> s instanceof UpgradeStationScreenHandler.PositionedSlot)
					.filter(s -> s.isEnabled() && s.getSlot() != null && "TOOL_BINDING".equals(s.getSlot().typeName()))
					.findFirst();

			context.assertTrue(bindingSlotOpt.isPresent(), "Could not find binding slot to perform test.");
			var bindingSlot = bindingSlotOpt.get();

			bindingSlot.setStack(binding.copy());

			// Check it was upgraded
			State upgradedState = stateService.convert(handler.get().getSlot(0).getStack()).orElse(null);
			context.assertTrue(upgradedState != null, "Could not convert upgraded state.");
			context.assertTrue(((Composite) upgradedState).upgrades().stream().anyMatch(s -> s.name().contains("binding")), "Pickaxe should be upgraded first.");

			// Now, remove the upgrade by taking the stack
			// The tree has been reconstructed with new slots, so we need to fetch the new slot.
			handler.get().getSlotPool().stream()
					.filter(s -> s instanceof UpgradeStationScreenHandler.PositionedSlot)
					.filter(s -> s.isEnabled() && s.getSlot() != null && "TOOL_BINDING".equals(s.getSlot().typeName()))
					.findFirst().get().takeStack(1);

			// Check if it reverted
			State revertedState = stateService.convert(handler.get().getSlot(0).getStack()).orElse(null);
			context.assertTrue(revertedState != null, "Could not convert reverted state.");
			long bindingCount = ((Composite) revertedState).upgrades().stream().filter(s -> s.name().contains("binding")).count();
			context.assertTrue(0 == bindingCount , "Pickaxe should have reverted to its original state.");

			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "upgrade_station_test")
	public void testStatePersistenceOnClose(TestContext context) {
		setup(context);
		context.runAtTick(2, () -> {
			ItemStack pickaxe = Utils.fromId("forgero:diamond-pickaxe");
			var handler = new UpgradeStationScreenHandler(1, player.getInventory(), ScreenHandlerContext.create(context.getWorld(), stationPos.absolute()));
			handler.getSlot(0).setStack(pickaxe.copy());
			// Simulate closing the screen, which should trigger onClosed and save the state to the BlockEntity
			handler.onClosed(player);
		});

		context.runAtTick(4, () -> {
			var reloadedBlockEntity = (UpgradeStationBlockEntity) context.getWorld().getBlockEntity(stationPos.absolute());
			context.assertTrue(reloadedBlockEntity != null, "Block entity was not found after reopening.");

			ItemStack stackInBE = reloadedBlockEntity.getCompositeInventory().getStack(0);
			context.assertFalse(stackInBE.isEmpty(), "Inventory should not be empty after closing and reopening.");

			State stateInBe = stateService.convert(stackInBE).orElse(null);
			context.assertTrue(stateInBe != null, "Could not convert item in BE to state.");
			context.assertTrue("diamond-pickaxe".equals( stateInBe.name()), "Item in BE has incorrect name.");
			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "upgrade_station_test")
	public void testBreakingBlockDropsTool(TestContext context) {
		setup(context);
		context.runAtTick(2, () -> {
			ItemStack pickaxe = Utils.fromId("forgero:diamond-pickaxe");
			blockEntity.getCompositeInventory().setStack(0, pickaxe);
		});

		context.runAtTick(4, () -> {
			// Break the main (LEFT) part of the block, which should trigger drops
			context.removeBlock(stationPos.relative());
		});

		context.runAtTick(6, () -> {
			context.expectItemAt(Utils.fromId("forgero:diamond-pickaxe").getItem(), stationPos.relative(), 1);

			// Check the other block part is also gone
			BlockPos rightPartPos = stationPos.absolute().offset(Direction.NORTH.rotateCounterclockwise(Direction.Axis.Y));
			context.assertTrue(context.getWorld().getBlockState(rightPartPos).isAir(), "The other part of the station should also be removed.");

			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "upgrade_station_test")
	public void testBreakingOtherPartDropsTool(TestContext context) {
		setup(context);
		final BlockPos[] rightPartPos = new BlockPos[1];

		context.runAtTick(2, () -> {
			ItemStack pickaxe = Utils.fromId("forgero:diamond-pickaxe");
			blockEntity.getCompositeInventory().setStack(0, pickaxe);
			rightPartPos[0] = stationPos.relative().offset(Direction.NORTH.rotateCounterclockwise(Direction.Axis.Y));
		});

		context.runAtTick(4, () -> {
			// Break the other (RIGHT) part of the block
			context.removeBlock(rightPartPos[0]);
		});

		context.runAtTick(6, () -> {
			context.expectItemAt(Utils.fromId("forgero:diamond-pickaxe").getItem(), rightPartPos[0], 1);

			// Check the main block part is also gone
			context.assertTrue(context.getWorld().getBlockState(stationPos.absolute()).isAir(), "The main part of the station should also be removed.");

			context.complete();
		});
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "upgrade_station_test", tickLimit = 40)
	public void testQuickMoveUpgrade(TestContext context) {
		setup(context);
		context.runAtTick(2, () -> {
			ItemStack pickaxe = Utils.fromId("forgero:diamond-pickaxe");
			ItemStack binding = Utils.fromId("forgero:oak-binding");

			// Place binding in player inventory
			int playerInvSlotIndex = 10;
			player.getInventory().setStack(playerInvSlotIndex, binding.copy());

			var handler = new UpgradeStationScreenHandler(1, player.getInventory(), ScreenHandlerContext.create(context.getWorld(), stationPos.absolute()));
			handler.getSlot(0).setStack(pickaxe.copy());

			var playerSlotInHandler = handler.slots.stream()
					.filter(s -> s.inventory == player.getInventory() && s.getIndex() == playerInvSlotIndex)
					.findFirst()
					.orElseThrow();

			// Simulate shift-click
			handler.quickMove(player, playerSlotInHandler.id);

			// Verify binding moved to an upgrade slot
			var bindingSlotInStation = handler.getSlotPool().stream()
					.filter(s -> s.hasStack() && s.getStack().isOf(binding.getItem()))
					.findFirst()
					.orElse(null);

			context.assertTrue(bindingSlotInStation != null, "Binding was not moved to an upgrade slot.");
			State bindingState = stateService.convert(bindingSlotInStation.getStack()).get();
			context.assertTrue("oak-binding".equals(bindingState.name()), "Item in slot is not the correct binding.");

			// Verify player inventory is empty at that slot
			context.assertTrue(player.getInventory().getStack(playerInvSlotIndex).isEmpty(), "Player inventory should be empty after quick move.");

			// Verify tool is upgraded
			State upgradedState = stateService.convert(handler.getSlot(0).getStack()).orElse(null);
			context.assertTrue(upgradedState != null, "Could not convert upgraded tool to state.");
			boolean hasBinding = ((Composite) upgradedState).upgrades().stream().anyMatch(s -> s.name().contains("binding"));
			context.assertTrue(hasBinding, "Pickaxe in main slot was not upgraded after quick move.");

			context.complete();
		});
	}
}
