package com.sigmundgranaas.forgero.minecraft.common.block.upgradestation;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.composite.ConstructedState;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.SlotContainer;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public class UpgradeStationScreenHandler extends ScreenHandler {

	private static final int SLOT_SIZE = 18;
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
	public final int verticalSpacing = 25;   // adjust this value as needed
	public final int compositeSlotY = 20;
	protected final CompositeSlot compositeSlot;
	private final PlayerEntity entity;
	private final SimpleInventory inventory;
	private final ScreenHandlerContext context;
	private final StateService service;
	private final List<Slot> slotList;

	//This constructor gets called on the client when the server wants it to open the screenHandler,
	//The client will call the other constructor with an empty Inventory and the screenHandler will automatically
	//sync this empty inventory with the inventory on the server.
	public UpgradeStationScreenHandler(int syncId, PlayerInventory playerInventory) {
		this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
	}

	//This constructor gets called from the BlockEntity on the server without calling the other constructor first, the server knows the inventory of the container
	//and can therefore directly provide it as an argument. This inventory will then be synced to the client.
	public UpgradeStationScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
		super(UpgradeStationScreenHandler.UPGRADE_STATION_SCREEN_HANDLER, syncId);
		this.context = context;
		this.inventory = new SimpleInventory(1);
		this.entity = playerInventory.player;
		inventory.addListener(this::onContentChanged);
		//some inventories do custom logic when a player opens it.
		inventory.onOpen(playerInventory.player);
		SimpleInventory compositeInventory = new SimpleInventory(1);
		compositeInventory.addListener(this::onCompositeSlotChanged);
		this.compositeSlot = new CompositeSlot(compositeInventory, 0, 80, compositeSlotY, null);
		this.service = StateService.INSTANCE;
		this.slotList = new ArrayList<>();
		//This will place the slot in the correct locations for a 3x3 Grid. The slots exist on both server and client!
		//This will not render the background of the slots however, this is the Screens job
		int m;
		int l;

		this.addSlot(compositeSlot);

		//The player inventory
		for (m = 0; m < 3; ++m) {
			for (l = 0; l < 9; ++l) {
				this.addSlot(new Slot(playerInventory, l + m * 9, 8 + l * 18, 138 + m * 18));
			}
		}
		//The player Hotbar
		for (m = 0; m < 9; ++m) {
			this.addSlot(new Slot(playerInventory, m + 27, 8 + m * 18, 196));
		}
	}

	public void onCompositeSlotChanged(Inventory compositeInventory) {
		var component = service.convert(compositeInventory.getStack(0));
		if (component.isPresent()) {
			this.compositeSlot.state = component.get();
			this.slotList.forEach(this.slots::remove);
			slotList.clear();
			ToolTree toolTree = new ToolTree(component.get());
			toolTree.buildTree();
			placeSlots(toolTree.getRoot(), 75, compositeSlotY + verticalSpacing, 1, compositeSlot, entity.getWorld(), entity);
		} else if (this.slotList.size() > 0) {
			compositeSlot.state = null;
			this.slotList.forEach(slot -> slot.inventory.clear());
		}
	}


	public void refreshTree(State state) {
		this.context.run((world, pos) -> {
			this.slotList.forEach(slot -> {
				slot.inventory.clear();
				slot.inventory.markDirty();
			});

			this.slotList.forEach(this.slots::remove);
			slotList.clear();
			compositeSlot.inventory.setStack(0, service.convert(state).get());

			ToolTree toolTree = new ToolTree(state);
			toolTree.buildTree();
			placeSlots(toolTree.getRoot(), 75, compositeSlotY + verticalSpacing, 1, compositeSlot, world, this.entity);
		});
	}

	private void placeSlots(TreeNode node, int parentOffsetX, int offsetY, int slotSpacing, Slot parent, World world, PlayerEntity entity) {
		// Calculate total number of slots for the node, including children's slots
		int totalSlots = node.getLeafCount();
		// Calculate total slot width and spacing width
		int totalSlotWidth = totalSlots * SLOT_SIZE;
		int totalSpacingWidth = (totalSlots - 1) * slotSpacing;
		int totalWidth = totalSlotWidth + totalSpacingWidth;
		// Calculate starting offset X
		int startOffsetX = parentOffsetX - totalWidth / 2;

		int currentWidth = 0;
		int placedSlots = 0;
		for (TreeNode child : node.getChildren()) {
			int childSlots = child.getLeafCount();
			int childSlotWidth = childSlots * SLOT_SIZE;
			int childSpacingWidth = (childSlots - 1) * slotSpacing;
			int childWidth = childSlotWidth + childSpacingWidth;
			int slotOffsetX = startOffsetX + currentWidth + (childWidth / 2);

			var inventory = new SimpleInventory(1);
			var slot = new PositionedSlot(inventory, 0, slotOffsetX, offsetY, child.slot, parent, ((Composite) node.state).getSlotContainer());
			slotList.add(slot);
			addSlot(slot);
			ItemStack stack = service.convert(child.getState()).orElse(ItemStack.EMPTY);
			if (!world.isClient() && entity instanceof ServerPlayerEntity serverPlayerEntity) {
				slot.inventory.setStack(0, stack);
				setPreviousTrackedSlot(slot.id, stack);
				serverPlayerEntity.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(this.syncId, this.nextRevision(), slot.id, stack));
			}
			placeSlots(child, slotOffsetX + (5 * placedSlots), offsetY + verticalSpacing, slotSpacing, slot, world, entity);
			placedSlots += 1;
			currentWidth += childWidth + slotSpacing;
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
		return true;
	}

	// Shift + Player Inv Slot
	@Override
	public ItemStack transferSlot(PlayerEntity player, int invSlot) {
		ItemStack newStack = ItemStack.EMPTY;
		if (this.slots.size() >= invSlot) {
			Slot slot = this.slots.get(invSlot);
			if (slot.hasStack()) {
				ItemStack originalStack = slot.getStack();
				newStack = originalStack;
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
		}

		return newStack;
	}

	public CompositeSlot getCompositeSlot() {
		return compositeSlot;
	}


	public static class CompositeSlot extends Slot {
		public int x;
		public int y;
		@Nullable
		protected State state;

		public CompositeSlot(Inventory inventory, int index, int x, int y, @Nullable State state) {
			super(inventory, index, x, y);
			this.state = state;
		}

		@Override
		public int getMaxItemCount() {
			return 1;
		}

		@Override
		public boolean canInsert(ItemStack stack) {
			return StateService.INSTANCE.convert(stack).isPresent() && StateService.INSTANCE.convert(stack).get() instanceof Composite;
		}
	}

	public static class TreeNode {
		private final State state;
		private final List<TreeNode> children;
		@Nullable
		private final com.sigmundgranaas.forgero.core.state.Slot slot;
		private TreeNode parent;

		public TreeNode(State state, @Nullable com.sigmundgranaas.forgero.core.state.Slot slot) {
			this.state = state;
			this.children = new ArrayList<>();
			this.slot = slot;
		}

		public int getLeafCount() {
			if (children.isEmpty()) {
				return 1;
			} else {
				return children.stream().mapToInt(TreeNode::getLeafCount).sum();
			}
		}


		public void addChild(TreeNode child) {
			child.parent = this;
			this.children.add(child);
		}

		public State getState() {
			return this.state;
		}

		public List<TreeNode> getChildren() {
			return this.children;
		}

		public TreeNode getParent() {
			return this.parent;
		}

		public State getParentPart() {
			if (this.parent == null) {
				return null;
			}
			return this.parent.getState();
		}
	}

	public static class EmptyState implements State {
		@Override
		public String name() {
			return "empty";
		}

		@Override
		public String nameSpace() {
			return "forgero";
		}

		@Override
		public Type type() {
			return Type.of("EMPTY");
		}
	}

	public class PositionedSlot extends Slot {
		public final int xPosition;
		public final int yPosition;
		private final SlotContainer container;
		@Nullable
		public com.sigmundgranaas.forgero.core.state.Slot slot;
		@Nullable
		public Slot parent;

		public PositionedSlot(Inventory inventory, int index, int xPosition, int yPosition, @Nullable com.sigmundgranaas.forgero.core.state.Slot slot, @Nullable Slot parent, SlotContainer container) {
			super(inventory, index, xPosition, yPosition);
			this.xPosition = xPosition;
			this.yPosition = yPosition;
			this.slot = slot;
			this.parent = parent;
			this.container = container;
		}

		public @Nullable com.sigmundgranaas.forgero.core.state.Slot getSlot() {
			return slot;
		}

		@Override
		public int getMaxItemCount() {
			return 1;
		}

		@Override
		public boolean canTakeItems(PlayerEntity playerEntity) {
			return slot != null;
		}


		@Override
		public boolean canInsert(ItemStack stack) {
			var stateOpt = StateService.INSTANCE.convert(stack);
			return slot != null && stateOpt.isPresent() && container.canUpgrade(stateOpt.get());
		}

		@Override
		public void onTakeItem(PlayerEntity player, ItemStack stack) {
			if (this.slot != null && this.slot.get().isPresent() && this.parent != null && compositeSlot.state != null && compositeSlot.state instanceof Composite composite) {
				this.slot = this.container.empty(slot);
				refreshTree(compositeSlot.state);
			}
			super.onTakeItem(player, stack);
		}

		@Override
		public ItemStack insertStack(ItemStack stack, int count) {
			if (this.canInsert(stack)) {
				var stateOpt = StateService.INSTANCE.convert(stack);
				if (stateOpt.isPresent() && this.parent != null && this.slot != null && compositeSlot.state != null && compositeSlot.state instanceof Composite composite) {
					this.slot = container.set(stateOpt.get()).orElse(slot);
					refreshTree(compositeSlot.state);
				}
			}

			return super.insertStack(stack, count);
		}
	}

	// Empty state representing an unfilled slot

	public class ToolTree {
		private final TreeNode root;

		public ToolTree(State rootState) {
			this.root = new TreeNode(rootState, null);
		}

		public TreeNode getRoot() {
			return this.root;
		}

		// Method to build the tree structure recursively from the given root state
		public void buildTree() {
			this.buildTreeHelper(this.root);
		}

		private void buildTreeHelper(TreeNode node) {
			State state = node.getState();
			if (state instanceof ConstructedState construct) {
				for (State constructed : construct.parts()) {
					if (constructed instanceof Composite) {
						TreeNode childNode = new TreeNode(constructed, null);
						node.addChild(childNode);
						this.buildTreeHelper(childNode);
					}
				}
			}
			if (state instanceof Composite composite) {
				for (com.sigmundgranaas.forgero.core.state.Slot upgradeSlot : composite.slots()) {
					State childState = upgradeSlot.filled() ? upgradeSlot.get().get() : new EmptyState();
					TreeNode childNode = new TreeNode(childState, upgradeSlot);
					node.addChild(childNode);
					this.buildTreeHelper(childNode);
				}
			}
		}
	}

	public static ScreenHandlerType<UpgradeStationScreenHandler> UPGRADE_STATION_SCREEN_HANDLER = new ScreenHandlerType<>(UpgradeStationScreenHandler::new);
}
