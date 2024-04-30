package com.sigmundgranaas.forgero.minecraft.common.block.upgradestation;

import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.FORGERO_IDENTIFIER;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.attribute.Category;
import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.composite.ConstructedState;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.SlotContainer;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import org.jetbrains.annotations.NotNull;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;

public class UpgradeStationScreenHandler extends ScreenHandler {

	private static final int SLOT_SIZE = 18;

	public final int verticalSpacing = 25;
	public final int compositeSlotY = 20;
	protected final CompositeSlot compositeSlot;
	protected final List<PositionedSlot> slotPool;
	private final SimpleInventory compositeInventory;
	private final ScreenHandlerContext context;
	private final StateService service;
	private final int maxSlots = 100;
	private PlayerEntity entity;
	private boolean isBuildingTree = false;
	private int activeSlots;

	//This constructor gets called on the client when the server wants it to open the screenHandler,
	//The client will call the other constructor with an empty Inventory and the screenHandler will automatically
	//sync this empty inventory with the inventory on the server.
	public UpgradeStationScreenHandler(int syncId, PlayerInventory playerInventory) {
		this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
		this.entity = playerInventory.player;
	}

	//This constructor gets called from the BlockEntity on the server without calling the other constructor first, the server knows the inventory of the container
	//and can therefore directly provide it as an argument. This inventory will then be synced to the client.
	public UpgradeStationScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
		super(UpgradeStationScreenHandler.UPGRADE_STATION_SCREEN_HANDLER, syncId);
		this.context = context;
		this.compositeInventory = new SimpleInventory(1);
		this.entity = playerInventory.player;
		compositeInventory.addListener(this::onCompositeSlotChanged);
		//some inventories do custom logic when a player opens it.
		compositeInventory.onOpen(playerInventory.player);
		this.compositeSlot = new CompositeSlot(compositeInventory, 0, 80, compositeSlotY, null);
		this.service = StateService.INSTANCE.uncached();
		//This will place the slot in the correct locations for a 3x3 Grid. The slots exist on both server and client!
		//This will not render the background of the slots however, this is the Screens job
		this.addSlot(compositeSlot);

		this.slotPool = new ArrayList<>(maxSlots);

		this.activeSlots = 0;


		int i;

		for (i = 0; i < 9; ++i) {
			this.addSlot(new Slot(playerInventory, this.slots.size() - 1, 8 + i * 18, 196));
		}

		for (i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlot(new Slot(playerInventory, this.slots.size() - 1, 8 + j * 18, 138 + i * 18));
			}
		}


		// initialization of the slot pool
		for (int j = 0; j < maxSlots; j++) {
			var inventory = new SimpleInventory(1);
			var slot = new PositionedSlot(inventory, 0, 0, 0, null, null, null); //initialize with dummy values, will be populated later
			this.slotPool.add(slot);
			this.addSlot(slot);
		}
	}

	public void onCompositeSlotChanged(Inventory compositeInventory) {
		if (!isBuildingTree) {
			this.isBuildingTree = true;
			var component = service.convert(compositeInventory.getStack(0).copy());
			if (component.isPresent() && component.get() instanceof Composite composite) {
				var newState = composite.copy();
				this.compositeSlot.state = newState;
				clearSlotsAndRebuildTree(newState);
			} else {
				for (int i = 0; i < this.maxSlots; i++) {
					slotPool.get(i).clear();
				}
				compositeSlot.state = null;
				compositeSlot.inventory.clear();
			}
			updateToClient();
			sendContentUpdates();
			this.isBuildingTree = false;
		}

	}

	private InventoryChangedListener createPartSlotFn(PositionedSlot slot) {
		return (Inventory partInventory) -> this.context.run((world, pos) -> {
			if (!world.isClient()) {
				if (!isBuildingTree && compositeSlot.state != null && entity instanceof ServerPlayerEntity) {
					isBuildingTree = true;
					ItemStack stack = partInventory.getStack(0).copy();
					var component = service.convert(stack);
					if (component.isPresent()) {
						slot.slot = slot.container.set(component.get(), slot.slot);
					} else if (slot.container != null) {
						slot.slot = slot.container.empty(slot.getSlot());
					}
					isBuildingTree = false;
					ItemStack newState = service.convert(compositeSlot.state).get();
				
					var nbt = compositeSlot.inventory.getStack(0).copy().getOrCreateNbt();
					nbt.put(FORGERO_IDENTIFIER, newState.getOrCreateNbt().get(FORGERO_IDENTIFIER));
					newState.setNbt(nbt);
					compositeSlot.setStack(newState);
				}
			}
		});
	}

	private void clearSlotsAndRebuildTree(State state) {
		for (int i = 0; i < this.maxSlots; i++) {
			slotPool.get(i).clear();
		}
		this.activeSlots = 0;
		ToolTree toolTree = new ToolTree(state);
		toolTree.buildTree();
		placeSlots(toolTree.getRoot(), 75, compositeSlotY + verticalSpacing, 1, compositeSlot, this.entity);
	}

	private void placeSlots(TreeNode node, int parentOffsetX, int offsetY, int slotSpacing, Slot parent, PlayerEntity entity) {
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

			PositionedSlot slot = this.slotPool.get(this.activeSlots);
			int index = this.slots.indexOf(slot);
			var newInventory = new SimpleInventory(1);
			SlotContainer container = ((Composite) node.state).getSlotContainer();

			PositionedSlot newSlot = new PositionedSlot(newInventory, 0, slotOffsetX, offsetY, child.slot, parent, container);

			newSlot.id = slot.id;
			newInventory.addListener(this.createPartSlotFn(newSlot));
			this.slots.set(index, newSlot);
			this.slotPool.set(this.activeSlots, newSlot);
			var state = child.getState();

			this.activeSlots++;

			if (!(state instanceof EmptyState)) {
				if (entity instanceof ServerPlayerEntity serverPlayerEntity) {
					ItemStack stack = service.convert(state).orElse(ItemStack.EMPTY);
					newSlot.inventory.setStack(0, stack.copy());
				}
				placeSlots(child, slotOffsetX + (5 * placedSlots), offsetY + verticalSpacing, slotSpacing, newSlot, entity);
			}
			placedSlots += 1;
			currentWidth += childWidth + slotSpacing;
		}
	}

	@Override
	public void onClosed(PlayerEntity player) {
		super.onClosed(player);
		this.context.run((world, pos) -> {
			this.dropInventory(player, this.compositeInventory);
		});
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return true;
	}

	@Override
	public ItemStack quickMove(PlayerEntity player, int invSlot) {
		ItemStack newStack = ItemStack.EMPTY;
		if (this.slots.size() > invSlot) {
			Slot slot = this.slots.get(invSlot);
			if (slot.hasStack()) {
				ItemStack originalStack = slot.getStack();
				newStack = originalStack.copy();
				if (invSlot < this.compositeInventory.size()) {
					// When the slot is in the player's inventory, try moving the stack to the composite inventory
					if (!this.insertItem(originalStack, this.compositeInventory.size(), this.slots.size(), true)) {
						return ItemStack.EMPTY;
					}
				} else {
					// When the slot is in the composite inventory, try moving the stack to the player's inventory
					if (!this.insertItem(originalStack, 0, this.compositeInventory.size(), false)) {
						return ItemStack.EMPTY;
					}
				}

				// If the original stack is empty after moving, clear the slot; otherwise, handle any leftovers
				if (originalStack.isEmpty()) {
					slot.setStack(ItemStack.EMPTY);
				} else {
					slot.onQuickTransfer(originalStack, newStack);
				}
			}
		}

		return newStack;
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

		@Override
		public State strip() {
			return this;
		}

		@Override
		public @NotNull List<Property> getRootProperties(Matchable target, MatchContext context) {
			return getRootProperties();
		}
	}

	// Empty state representing an unfilled slot
	public static class ToolTree {
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
					if (upgradeSlot.category().contains(Category.UNDEFINED)) {
						continue;
					}
					State childState = upgradeSlot.filled() ? upgradeSlot.get().get() : new EmptyState();
					TreeNode childNode = new TreeNode(childState, upgradeSlot);
					node.addChild(childNode);
					this.buildTreeHelper(childNode);
				}
			}
		}
	}

	public static class PositionedSlot extends Slot {
		public int xPosition;
		public int yPosition;
		@Nullable
		public com.sigmundgranaas.forgero.core.state.Slot slot;
		@Nullable
		public Slot parent;
		private SlotContainer container;

		public PositionedSlot(Inventory inventory, int index, int xPosition, int yPosition, @Nullable com.sigmundgranaas.forgero.core.state.Slot slot, @Nullable Slot parent, SlotContainer container) {
			super(inventory, index, xPosition, yPosition);
			this.xPosition = xPosition;
			this.yPosition = yPosition;
			this.slot = slot;
			this.id = index;
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
			return slot != null && stateOpt.isPresent() && slot.empty().test(stateOpt.get(), MatchContext.of());
		}


		public void clear() {
			this.xPosition = 0;
			this.yPosition = 0;
			this.slot = null;
			this.parent = null;
			this.container = null;
			this.inventory.setStack(0, ItemStack.EMPTY); // clear stack
		}

		@Override
		public boolean isEnabled() {
			return this.slot != null || this.container != null || this.parent != null;
		}

	}

	public static ScreenHandlerType<UpgradeStationScreenHandler> UPGRADE_STATION_SCREEN_HANDLER = new ScreenHandlerType<>(UpgradeStationScreenHandler::new, FeatureFlags.VANILLA_FEATURES);
}
