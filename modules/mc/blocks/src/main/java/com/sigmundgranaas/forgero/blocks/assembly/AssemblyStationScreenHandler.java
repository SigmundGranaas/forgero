package com.sigmundgranaas.forgero.blocks.assembly;

import com.sigmundgranaas.forgero.blocks.api.StationContext;
import com.sigmundgranaas.forgero.blocks.assembly.DisassemblyService.DisassemblyResult;
import com.sigmundgranaas.forgero.core.component.api.Component;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Screen handler for the Assembly Station (disassembly station).
 * <p>
 * This handler manages the UI for disassembling Forgero items into their parts.
 * <p>
 * Layout:
 * <ul>
 *   <li>Slot 0: Input slot for the item to disassemble</li>
 *   <li>Slots 1-9: Result slots showing the disassembled parts</li>
 *   <li>Remaining: Player inventory</li>
 * </ul>
 */
public class AssemblyStationScreenHandler extends ScreenHandler {

	public static final int INPUT_SLOT_X = 34;
	public static final int INPUT_SLOT_Y = 34;
	public static final int RESULT_START_X = 92;
	public static final int RESULT_START_Y = 17;
	public static final int RESULT_COLS = 3;
	public static final int RESULT_ROWS = 3;

	private final StationContext context;
	private final PlayerEntity player;
	private final ScreenHandlerContext screenContext;
	private final SimpleInventory inputInventory;
	private final SimpleInventory resultInventory;
	private final DeconstructionSlot inputSlot;
	private final DisassemblyService disassemblyService;

	private DisassemblyResult currentResult = DisassemblyResult.empty();
	private boolean isProcessing = false;

	/**
	 * Screen handler type for registration.
	 */
	public static final ScreenHandlerType<AssemblyStationScreenHandler> TYPE =
			new ScreenHandlerType<>(AssemblyStationScreenHandler::clientFactory, FeatureFlags.VANILLA_FEATURES);

	private static AssemblyStationScreenHandler clientFactory(int syncId, PlayerInventory inventory) {
		return new AssemblyStationScreenHandler(syncId, inventory, null, ScreenHandlerContext.EMPTY);
	}

	public AssemblyStationScreenHandler(
			int syncId,
			PlayerInventory playerInventory,
			StationContext context,
			ScreenHandlerContext screenContext
	) {
		super(TYPE, syncId);
		this.context = context;
		this.player = playerInventory.player;
		this.screenContext = screenContext;
		this.inputInventory = new SimpleInventory(1);
		this.resultInventory = new SimpleInventory(RESULT_COLS * RESULT_ROWS);

		inputInventory.addListener(this::onInputChanged);
		inputInventory.onOpen(player);

		this.disassemblyService = context != null ? DisassemblyService.create(context) : null;

		// Input slot
		this.inputSlot = new DeconstructionSlot(inputInventory, 0, INPUT_SLOT_X, INPUT_SLOT_Y, resultInventory, context);
		this.addSlot(inputSlot);

		// Result slots (3x3 grid)
		for (int row = 0; row < RESULT_ROWS; row++) {
			for (int col = 0; col < RESULT_COLS; col++) {
				int index = row * RESULT_COLS + col + 1;
				int x = RESULT_START_X + col * 18;
				int y = RESULT_START_Y + row * 18;
				this.addSlot(new ResultSlot(resultInventory, index - 1, x, y));
			}
		}

		// Player inventory
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
			}
		}

		// Player hotbar
		for (int col = 0; col < 9; col++) {
			this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
		}
	}

	private void onInputChanged(Inventory inventory) {
		if (isProcessing) return;
		isProcessing = true;

		try {
			ItemStack inputStack = inventory.getStack(0);

			if (inputStack.isEmpty()) {
				// Check if all result slots are empty (reassembly happened)
				if (currentResult != null && isAllResultsRemoved()) {
					onItemReassembled();
				}
				clearResults();
				currentResult = DisassemblyResult.empty();
			} else {
				// Disassemble the item
				performDisassembly(inputStack);
			}
		} finally {
			isProcessing = false;
		}
	}

	private void performDisassembly(ItemStack inputStack) {
		screenContext.run((world, pos) -> {
			if (world.isClient() || disassemblyService == null || context == null) {
				return;
			}

			context.converter().toComponent(inputStack).ifPresent(component -> {
				currentResult = disassemblyService.disassemble(component);

				// Place parts in result slots
				List<ItemStack> parts = currentResult.parts();
				for (int i = 0; i < Math.min(parts.size(), resultInventory.size()); i++) {
					ItemStack part = parts.get(i).copy();
					resultInventory.setStack(i, part);

					// Sync to client
					if (player instanceof ServerPlayerEntity serverPlayer) {
						int slotIndex = i + 1; // +1 because slot 0 is input
						serverPlayer.networkHandler.sendPacket(
								new ScreenHandlerSlotUpdateS2CPacket(syncId, nextRevision(), slotIndex, part)
						);
					}
				}

				// Consume the input item
				inputSlot.markAsDisassembled();
			});
		});
	}

	private void clearResults() {
		resultInventory.clear();
	}

	private boolean isAllResultsRemoved() {
		for (int i = 0; i < resultInventory.size(); i++) {
			if (!resultInventory.getStack(i).isEmpty()) {
				return false;
			}
		}
		return true;
	}

	private void onItemReassembled() {
		// The item has been fully disassembled and all parts taken
		inputInventory.clear();
	}

	@Override
	protected void dropInventory(PlayerEntity player, Inventory inventory) {
		if (inputSlot.isEmpty()) {
			super.dropInventory(player, inventory);
		} else {
			super.dropInventory(player, new SimpleInventory(inputSlot.getStack()));
		}
	}

	@Override
	public void onClosed(PlayerEntity player) {
		super.onClosed(player);
		screenContext.run((world, pos) -> {
			dropInventory(player, inputInventory);
		});
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return inputInventory.canPlayerUse(player);
	}

	@Override
	public ItemStack quickMove(PlayerEntity player, int slotIndex) {
		ItemStack newStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(slotIndex);

		if (slot.hasStack() && (inputSlot.canInsert(slot.getStack()) || slotIndex < resultInventory.size() + 1)) {
			ItemStack originalStack = slot.getStack();
			newStack = originalStack.copy();

			if (slotIndex < resultInventory.size() + 1) {
				// From input/result to player inventory
				if (!this.insertItem(originalStack, resultInventory.size() + 1, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else {
				// From player inventory to input
				if (!this.insertItem(originalStack, 0, 1, false)) {
					return ItemStack.EMPTY;
				}
			}

			if (originalStack.isEmpty()) {
				slot.setStack(ItemStack.EMPTY);
			} else {
				slot.markDirty();
			}
		}

		return newStack;
	}

	/**
	 * The input slot for items to disassemble.
	 */
	private static class DeconstructionSlot extends Slot {
		private final Inventory resultInventory;
		private final StationContext context;
		private boolean disassembled = false;

		public DeconstructionSlot(Inventory inventory, int index, int x, int y, Inventory resultInventory, StationContext context) {
			super(inventory, index, x, y);
			this.resultInventory = resultInventory;
			this.context = context;
		}

		@Override
		public int getMaxItemCount() {
			return 1;
		}

		public boolean isEmpty() {
			return inventory.isEmpty();
		}

		public void markAsDisassembled() {
			disassembled = true;
		}

		public void consumeInput() {
			if (!inventory.getStack(0).isEmpty()) {
				inventory.getStack(0).decrement(1);
			}
			disassembled = false;
		}

		@Override
		public boolean canInsert(ItemStack stack) {
			if (context == null) {
				return true; // Client-side
			}

			// Check if damaged
			if (stack.getDamage() > 0) {
				return false;
			}

			// Check if results are empty
			for (int i = 0; i < resultInventory.size(); i++) {
				if (!resultInventory.getStack(i).isEmpty()) {
					return false;
				}
			}

			// Check if can be disassembled
			return context.converter().toComponent(stack)
					.map(c -> DisassemblyService.create(context).canDisassemble(c))
					.orElse(false);
		}
	}

	/**
	 * Result slots that show disassembled parts.
	 */
	private static class ResultSlot extends Slot {
		public ResultSlot(Inventory inventory, int index, int x, int y) {
			super(inventory, index, x, y);
		}

		@Override
		public boolean canInsert(ItemStack stack) {
			return false; // Output only
		}
	}
}
