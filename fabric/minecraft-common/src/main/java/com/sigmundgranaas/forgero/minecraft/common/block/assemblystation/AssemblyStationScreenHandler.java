package com.sigmundgranaas.forgero.minecraft.common.block.assemblystation;

import java.util.List;
import java.util.stream.IntStream;

import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.state.DisassemblyHandler;
import com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.state.EmptyHandler;
import com.sigmundgranaas.forgero.minecraft.common.conversion.StateConverter;
import com.sigmundgranaas.forgero.minecraft.common.resources.DisassemblyRecipeLoader;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;

public class AssemblyStationScreenHandler extends ScreenHandler {

	public static ScreenHandler dummyHandler = new ScreenHandler(ScreenHandlerType.CRAFTING, 0) {
		@Override
		public ItemStack transferSlot(PlayerEntity player, int index) {
			return ItemStack.EMPTY;
		}

		@Override
		public boolean canUse(PlayerEntity player) {
			return true;
		}
	};
	private final SimpleInventory inventory;
	private final ScreenHandlerContext context;
	private final PlayerEntity player;
	private final DeconstructionSlot compositeSlot;

	private final DisassemblyHandler disassemblyHandler = new EmptyHandler();

	//This constructor gets called on the client when the server wants it to open the screenHandler,
	//The client will call the other constructor with an empty Inventory and the screenHandler will automatically
	//sync this empty inventory with the inventory on the server.
	public AssemblyStationScreenHandler(int syncId, PlayerInventory playerInventory) {
		this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
	}

	//This constructor gets called from the BlockEntity on the server without calling the other constructor first, the server knows the inventory of the container
	//and can therefore directly provide it as an argument. This inventory will then be synced to the client.
	public AssemblyStationScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
		super(AssemblyStationScreenHandler.ASSEMBLY_STATION_SCREEN_HANDLER, syncId);
		this.player = playerInventory.player;
		this.context = context;
		this.inventory = new SimpleInventory(10);
		inventory.addListener(this::onContentChanged);
		//some inventories do custom logic when a player opens it.
		inventory.onOpen(playerInventory.player);
		SimpleInventory compositeInventory = new SimpleInventory(1);
		compositeInventory.addListener(this::onCompositeSlotChanged);
		this.compositeSlot = new DeconstructionSlot(compositeInventory, 0, 34, 34, inventory);

		//This will place the slot in the correct locations for a 3x3 Grid. The slots exist on both server and client!
		//This will not render the background of the slots however, this is the Screens job
		int m;
		int l;

		this.addSlot(compositeSlot);

		//Our inventory
		for (m = 0; m < 3; ++m) {
			this.addSlot(new Slot(inventory, m + 1, 92 + m * 18, 17));
		}

		for (m = 0; m < 3; ++m) {
			this.addSlot(new Slot(inventory, m + 4, 92 + m * 18, 35));
		}

		for (m = 0; m < 3; ++m) {
			this.addSlot(new Slot(inventory, m + 7, 92 + m * 18, 53));
		}


		//The player inventory
		for (m = 0; m < 3; ++m) {
			for (l = 0; l < 9; ++l) {
				this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 84 + m * 18));
			}
		}
		//The player Hotbar
		for (m = 0; m < 9; ++m) {
			this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 142));
		}

	}

	@Override
	protected void dropInventory(PlayerEntity player, Inventory inventory) {
		if (compositeSlot.isEmpty()) {
			super.dropInventory(player, inventory);
		} else {
			super.dropInventory(player, new SimpleInventory(compositeSlot.getStack()));
		}
	}

	@Override
	public void close(PlayerEntity player) {
		super.close(player);
		this.context.run((world, pos) -> {
			this.dropInventory(player, this.inventory);
		});
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return this.inventory.canPlayerUse(player);
	}

	// Shift + Player Inv Slot
	@Override
	public ItemStack transferSlot(PlayerEntity player, int invSlot) {
		ItemStack newStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(invSlot);
		if (slot.hasStack()) {
			ItemStack originalStack = slot.getStack();
			newStack = originalStack.copy();
			if (invSlot < this.inventory.size()) {
				if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.insertItem(originalStack, 0, this.inventory.size(), false)) {
				return ItemStack.EMPTY;
			}

			if (originalStack.isEmpty()) {
				slot.setStack(ItemStack.EMPTY);
			} else {
				slot.markDirty();
			}
		}

		return newStack;
	}

	@Override
	public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
		return false;
	}

	public void onCompositeSlotChanged(Inventory compositeInventory) {
		boolean isEmpty = compositeInventory.isEmpty();
		if (isEmpty) {
			if (compositeSlot.isDeconstructed() && compositeSlot.getComposite().isPresent()) {
				compositeSlot.doneConstructing = false;
				onItemRemovedFromToolSlot();
			}
		} else {
			var stack = StateConverter.of(compositeInventory.getStack(0))
					.filter(Composite.class::isInstance)
					.map(Composite.class::cast);
			if (!compositeSlot.isConstructed() && stack.isPresent()) {
				compositeSlot.addToolToCompositeSlot(stack.get());
				onItemAddedToToolSlot();
			} else if (DisassemblyRecipeLoader.getEntries().stream()
					.anyMatch(entry -> entry.getInput().test(compositeInventory.getStack(0)))) {
				compositeSlot.addToolToCompositeSlot(compositeInventory.getStack(0));
				onItemAddedToToolSlot();
			}
		}
	}

	@Override
	public void onContentChanged(Inventory inventory) {
		this.context.run((world, pos) -> {
			if (!world.isClient && compositeSlot.isRemovable()) {
				ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) player;
				if (!compositeSlot.isEmpty() && compositeSlot.doneConstructing && !isDeconstructedInventory(compositeSlot.construct)) {
					compositeSlot.removeCompositeIngredient();
					serverPlayerEntity.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(this.syncId, this.nextRevision(), 0, ItemStack.EMPTY));
				} else if (!compositeSlot.isEmpty() && compositeSlot.doneConstructing && !isDeconstructedInventory(compositeSlot.getStack())) {
					compositeSlot.removeCompositeIngredient();
					serverPlayerEntity.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(this.syncId, this.nextRevision(), 0, ItemStack.EMPTY));
				}
			}
		});
		super.onContentChanged(inventory);
	}

	private void onItemAddedToToolSlot() {
		var empty = ItemStack.EMPTY;
		this.context.run((world, pos) -> {
			if (!world.isClient) {
				compositeSlot.doneConstructing = false;
				var disassemblyStack = compositeSlot.getStack();
				ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) player;
				var compositeOpt = compositeSlot.getComposite();

				var itemRecipe = DisassemblyRecipeLoader.getEntries().stream()
						.filter(entry -> entry.getInput().test(disassemblyStack))
						.findFirst();

				if (compositeOpt.isPresent()) {
					if (inventory.isEmpty()) {
						var composite = compositeOpt.get();
						var elements = deconstructedItems(composite);
						for (int i = 1; i < elements.size() + 1; i++) {
							var element = elements.get(i - 1);
							inventory.setStack(i, element);
							setPreviousTrackedSlot(i, element);
							serverPlayerEntity.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(this.syncId, this.nextRevision(), i, element));
						}
					}
					compositeSlot.doneConstructing();
				} else if (itemRecipe.isPresent()) {
					if (inventory.isEmpty()) {
						var recipe = itemRecipe.get();
						for (int i = 1; i < recipe.getResults().size() + 1; i++) {
							var element = new ItemStack(recipe.getResults().get(i - 1));
							inventory.setStack(i, element);
							setPreviousTrackedSlot(i, element);
							serverPlayerEntity.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(this.syncId, this.nextRevision(), i, element));
						}
					}
					compositeSlot.doneConstructing();
				}
			}
		});
	}

	private List<ItemStack> deconstructedItems(Composite construct) {
		return construct.components().stream().filter(state -> !state.identifier().contains("schematic")).map(StateConverter::of).toList();
	}

	private boolean isDeconstructedInventory(Composite construct) {
		var deconstructed = deconstructedItems(construct);
		return IntStream.range(0, deconstructed.size()).allMatch(index -> deconstructed.get(index).getItem() == inventory.getStack(index).getItem());
	}

	private boolean isDeconstructedInventory(ItemStack construct) {
		var deconstructed = components(construct);
		return IntStream.range(0, deconstructed.size()).allMatch(index -> deconstructed.get(index) == inventory.getStack(index).getItem());
	}

	private List<Item> components(ItemStack stack) {
		return DisassemblyRecipeLoader.getEntries().stream()
				.filter(entry -> entry.getInput().test(stack))
				.findFirst()
				.map(DisassemblyRecipeLoader.DisassemblyRecipe::getResults)
				.orElse(List.of());
	}

	private void onItemRemovedFromToolSlot() {
		this.context.run((world, pos) -> {
			if (!world.isClient) {
				compositeSlot.removeCompositeIngredient();
				IntStream.range(0, 8).filter(index -> !inventory.isEmpty()).forEach(index -> inventory.getStack(index).decrement(1));
			}
		});
	}

	private static class DeconstructionSlot extends Slot {
		private final Inventory craftingInventory;
		private boolean doneConstructing = false;

		public DeconstructionSlot(Inventory inventory, int index, int x, int y, Inventory craftingInventory) {
			super(inventory, index, x, y);
			this.craftingInventory = craftingInventory;
		}


		public boolean isDeconstructed() {
			return true;
		}

		public boolean isEmpty() {
			return craftingInventory.isEmpty();
		}


		public void addToolToCompositeSlot(ItemStack construct) {
			this.isConstructed = false;
		}

		public void removeCompositeIngredient() {
			this.construct = null;
			this.isConstructed = false;
			this.doneConstructing = true;
			if (!this.inventory.getStack(0).isEmpty()) {
				this.inventory.getStack(0).decrement(1);
			}
		}

		@Override
		public boolean canInsert(ItemStack stack) {
			boolean noDamage = (stack.getOrCreateNbt().contains("Damage") && stack.getOrCreateNbt().getInt("Damage") == 0) || !stack.getItem().isDamageable();

			if (DisassemblyRecipeLoader.getEntries().stream()
					.anyMatch(entry -> entry.getInput().test(stack))) {
				return noDamage;
			}
			boolean isComposite = StateConverter.of(stack).filter(Composite.class::isInstance).isPresent();

			return isComposite && craftingInventory.isEmpty() && noDamage;
		}

		public void doneConstructing() {
			this.doneConstructing = true;
		}

		public boolean isRemovable() {
			if (craftingInventory.isEmpty()) {
				return true;
			}
			return doneConstructing || !isConstructed;
		}
	}

	public static ScreenHandlerType<AssemblyStationScreenHandler> ASSEMBLY_STATION_SCREEN_HANDLER = new ScreenHandlerType<>(AssemblyStationScreenHandler::new);


}
