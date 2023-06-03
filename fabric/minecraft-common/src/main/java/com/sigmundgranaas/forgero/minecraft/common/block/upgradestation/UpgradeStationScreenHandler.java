package com.sigmundgranaas.forgero.minecraft.common.block.upgradestation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.composite.ConstructedState;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;

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
	public final int horizontalSpacing = 24; // adjust this value as needed
	private final SimpleInventory inventory;
	private final ScreenHandlerContext context;
	private final PlayerEntity player;
	private final CompositeSlot compositeSlot;
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
		this.player = playerInventory.player;
		this.context = context;
		this.inventory = new SimpleInventory(1);
		inventory.addListener(this::onContentChanged);
		//some inventories do custom logic when a player opens it.
		inventory.onOpen(playerInventory.player);
		SimpleInventory compositeInventory = new SimpleInventory(1);
		compositeInventory.addListener(this::onCompositeSlotChanged);
		this.compositeSlot = new CompositeSlot(compositeInventory, 0, 80, 10);
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
				this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 84 + m * 18));
			}
		}
		//The player Hotbar
		for (m = 0; m < 9; ++m) {
			this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 142));
		}

	}

	public void onCompositeSlotChanged(Inventory compositeInventory) {
		var component = service.convert(compositeInventory.getStack(0));
		if (component.isPresent()) {
			this.slotList.forEach(this.slots::remove);
			slotList.clear();
			ToolTree toolTree = new ToolTree(component.get());
			toolTree.buildTree();
			placeSlots(toolTree.getRoot(), 80, 35, 2);
		} else {
			this.slotList.forEach(this.slots::remove);
			slotList.clear();
		}
	}

	private void placeSlots(TreeNode node, int parentOffsetX, int offsetY, int slotSpacing) {
		// Calculate total number of slots for the node, including children's slots
		int totalSlots = node.getLeafCount();
		// Calculate total slot width and spacing width
		int totalSlotWidth = totalSlots * SLOT_SIZE;
		int totalSpacingWidth = (totalSlots - 1) * slotSpacing;
		int totalWidth = totalSlotWidth + totalSpacingWidth;
		// Calculate starting offset X
		int startOffsetX = parentOffsetX - totalWidth / 2;

		int currentWidth = 0;

		for (TreeNode child : node.getChildren()) {
			int childSlots = child.getLeafCount();
			int childSlotWidth = childSlots * SLOT_SIZE;
			int childSpacingWidth = (childSlots - 1) * slotSpacing;
			int childWidth = childSlotWidth + childSpacingWidth;
			int slotOffsetX = startOffsetX + currentWidth + childWidth / 2;

			var inventory = new SimpleInventory(1);
			var slot = new PositionedSlot(inventory, 0, slotOffsetX, offsetY, child.slot);
			slotList.add(slot);
			addSlot(slot);
			slot.insertStack(service.convert(child.getState()).orElse(ItemStack.EMPTY));

			placeSlots(child, slotOffsetX, offsetY + verticalSpacing, slotSpacing);

			currentWidth += childWidth + slotSpacing;
		}
	}


	private int calculateSubtreeWidthHelper(State state, Map<State, Integer> widthMap) {
		if (widthMap.containsKey(state)) {
			return widthMap.get(state);
		}

		int width = 0;

		if (state instanceof ConstructedState construct) {
			for (State constructed : construct.parts()) {
				width += calculateSubtreeWidthHelper(constructed, widthMap);
			}
		}

		if (state instanceof Composite composite) {
			for (com.sigmundgranaas.forgero.core.state.Slot upgradeSlot : composite.slots()) {
				// Width is added for every slot, whether filled or not
				width += 1;

				// If the slot is filled, recursively calculate the width of the upgrade
				if (upgradeSlot.filled()) {
					width += calculateSubtreeWidthHelper(upgradeSlot.get().get(), widthMap);
				}
			}
		}

		widthMap.put(state, width);
		return width;
	}

	private Map<State, Integer> calculateSubtreeWidth(State root) {
		Map<State, Integer> widthMap = new HashMap<>();
		calculateSubtreeWidthHelper(root, widthMap);
		return widthMap;
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
		return newStack;
	}


	private static class CompositeSlot extends Slot {
		public CompositeSlot(Inventory inventory, int index, int x, int y) {
			super(inventory, index, x, y);

		}
	}

	public static class PositionedSlot extends Slot {
		public final int xPosition;
		public final int yPosition;
		@Nullable
		public final com.sigmundgranaas.forgero.core.state.Slot slot;

		public PositionedSlot(Inventory inventory, int index, int xPosition, int yPosition, @Nullable com.sigmundgranaas.forgero.core.state.Slot slot) {
			super(inventory, index, xPosition, yPosition);
			this.xPosition = xPosition;
			this.yPosition = yPosition;
			this.slot = slot;
		}

		public @Nullable com.sigmundgranaas.forgero.core.state.Slot getSlot() {
			return slot;
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
