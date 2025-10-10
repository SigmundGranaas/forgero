package com.sigmundgranaas.forgero.tools;

import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.UpgradeSlot;
import com.sigmundgranaas.forgero.core.component.mutation.api.ComponentMutater;
import com.sigmundgranaas.forgero.core.component.mutation.impl.ComponentMutaterImpl;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import com.sigmundgranaas.forgero.core.property.engine.ResolverEngine;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ComponentUpgradeScreen extends Screen {
	private CustomizableComponent component;
	private final CustomizableComponent initialComponent; // Store the original state
	private final ComponentConverter converter;
	private List<UpgradeSlot> slots;
	private ItemStack rootStack;
	private final ComponentMutater mutater = new ComponentMutaterImpl();
	private final Hand hand; // Hand holding the item

	// Interaction state
	private List<ItemStack> availableUpgrades = new ArrayList<>();
	private ItemStack heldStack = ItemStack.EMPTY;
	private Component heldComponent = null;
	private int heldOriginalSlot = -1; // -1 for inventory bar, >=0 for component slot index

	private static final int SLOT_SIZE = 16;
	private static final int ROOT_ITEM_SIZE = 64;
	private static final int LINE_THICKNESS = 2;
	private static final int LINE_COLOR = 0xFF808080; // Gray
	private static final int ITEM_BAR_HEIGHT = 22;
	private static final int ITEM_BAR_SLOT_SIZE = 16;
	private static final int ITEM_BAR_SLOT_PADDING = 3;


	public ComponentUpgradeScreen(CustomizableComponent component, ComponentConverter converter, Hand hand) {
		super(Text.translatable("forgero.dev.upgrade_viewer.title"));
		this.component = component;
		this.initialComponent = component; // Keep a snapshot
		this.converter = converter;
		this.slots = new ArrayList<>(component.getUpgradeSlots());
		this.rootStack = converter.toStack(component).orElse(ItemStack.EMPTY);
		this.hand = hand;
	}

	@Override
	public void removed() {
		super.removed();
		// If the component has changed, send an update packet to the server
		if (!this.component.equals(this.initialComponent)) {
			Optional<ItemStack> updatedStackOpt = converter.toStack(this.component);
			if (updatedStackOpt.isPresent()) {
				NbtCompound nbt = updatedStackOpt.get().getOrCreateNbt();
				ClientPlayNetworking.send(UpgradeComponentPacket.ID, UpgradeComponentPacket.write(this.hand, nbt));
			}
		}
	}


	@Override
	protected void init() {
		super.init();
		filterAvailableUpgrades();

		this.addDrawableChild(ButtonWidget.builder(Text.translatable("forgero.dev.inspector.button"), button -> {
					Resolver resolver = new ResolverEngine();
					ComponentInspector inspector = new ComponentInspector();
					String report = inspector.generateReport(this.component, resolver);
					this.client.setScreen(new ComponentStructureScreen(report, this));
				})
				.position(5, 5)
				.size(60, 20)
				.build());
	}

	private void filterAvailableUpgrades() {
		if (this.client == null || this.client.player == null) {
			return;
		}
		this.availableUpgrades.clear();
		PlayerInventory inventory = this.client.player.getInventory();
		for (int i = 0; i < inventory.size(); i++) {
			ItemStack stack = inventory.getStack(i);
			if (stack.isEmpty()) {
				continue;
			}
			// Don't include the item we are upgrading
			if (hand == Hand.MAIN_HAND && stack == client.player.getMainHandStack()) {
				continue;
			}
			if (hand == Hand.OFF_HAND && stack == client.player.getOffHandStack()) {
				continue;
			}

			Optional<Component> componentOpt = converter.toComponent(stack);
			if (componentOpt.isPresent()) {
				Component potentialUpgrade = componentOpt.get();
				// Check if this item is already in one of the slots (by component ID)
				boolean alreadySlotted = this.slots.stream()
						.map(UpgradeSlot::content)
						.flatMap(Optional::stream)
						.anyMatch(c -> c.id().equals(potentialUpgrade.id()));

				if (alreadySlotted) continue;

				for (UpgradeSlot slot : this.slots) {
					if (slot.validator().test(potentialUpgrade)) {
						this.availableUpgrades.add(stack.copy()); // Use a copy
						break; // Add only once per stack
					}
				}
			}
		}
	}


	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderBackground(context);
		super.render(context, mouseX, mouseY, delta);

		int centerX = this.width / 2;
		int centerY = this.height / 2;

		renderSlotsAndLines(context, centerX, centerY);
		renderComponents(context, centerX, centerY);
		renderItemBar(context);
		renderTooltips(context, mouseX, mouseY, centerX, centerY);

		// Render held item at cursor
		if (!heldStack.isEmpty()) {
			context.drawItem(heldStack, mouseX - 8, mouseY - 8);
		}
	}

	private void renderItemBar(DrawContext context) {
		if (availableUpgrades.isEmpty()) {
			return;
		}
		int barY = this.height - ITEM_BAR_HEIGHT - 5;
		int totalWidth = availableUpgrades.size() * (ITEM_BAR_SLOT_SIZE + ITEM_BAR_SLOT_PADDING * 2);
		int barX = (this.width - totalWidth) / 2;

		context.fill(barX - 2, barY - 2, barX + totalWidth + 2, barY + ITEM_BAR_SLOT_SIZE + 4, 0xC0101010);

		for (int i = 0; i < availableUpgrades.size(); i++) {
			ItemStack stack = availableUpgrades.get(i);
			int itemX = barX + i * (ITEM_BAR_SLOT_SIZE + ITEM_BAR_SLOT_PADDING * 2) + ITEM_BAR_SLOT_PADDING;
			context.drawItem(stack, itemX, barY);
			context.drawItemInSlot(this.textRenderer, stack, itemX, barY);
		}
	}

	private void renderSlotsAndLines(DrawContext context, int centerX, int centerY) {
		if (slots.isEmpty()) {
			return;
		}

		double radius = Math.min(width, height) / 3.0;
		double angleStep = 2 * Math.PI / slots.size();

		for (int i = 0; i < slots.size(); i++) {
			double angle = angleStep * i - (Math.PI / 2); // Start from top
			int slotX = (int) (centerX + radius * Math.cos(angle));
			int slotY = (int) (centerY + radius * Math.sin(angle));

			// Draw clean orthogonal lines instead of a jagged diagonal one
			drawConnectionPath(context, centerX, centerY, slotX, slotY, LINE_THICKNESS, LINE_COLOR);
		}
	}

	private void renderComponents(DrawContext context, int centerX, int centerY) {
		// Render the root component in 3D
		if (this.client != null && !rootStack.isEmpty()) {
			context.getMatrices().push();
			context.getMatrices().translate(centerX, centerY, 150.0F);
			context.getMatrices().scale(1.0F * ROOT_ITEM_SIZE, -1.0F * ROOT_ITEM_SIZE, 1.0F * ROOT_ITEM_SIZE);
			float yRotation = (float) ((System.currentTimeMillis() / 20) % 360);
			context.getMatrices().multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yRotation));
			context.getMatrices().multiply(RotationAxis.POSITIVE_X.rotationDegrees(-20f)); // Static tilt
			MinecraftClient.getInstance().getItemRenderer().renderItem(rootStack, ModelTransformationMode.GUI, false, context.getMatrices(), context.getVertexConsumers(), 15728880, OverlayTexture.DEFAULT_UV, MinecraftClient.getInstance().getItemRenderer().getModel(rootStack, this.client.world, null, 0));
			context.getMatrices().pop();
		}


		// Render the 2D upgrade slots around the root
		if (slots.isEmpty()) {
			return;
		}

		double radius = Math.min(width, height) / 3.0;
		double angleStep = 2 * Math.PI / slots.size();

		for (int i = 0; i < slots.size(); i++) {
			UpgradeSlot slot = slots.get(i);
			double angle = angleStep * i - (Math.PI / 2); // Start from top
			int slotX = (int) (centerX + radius * Math.cos(angle));
			int slotY = (int) (centerY + radius * Math.sin(angle));

			ItemStack stackToRender = slot.content()
					.flatMap(converter::toStack)
					.orElse(new ItemStack(Items.GRAY_STAINED_GLASS_PANE)); // Placeholder for empty slot

			context.drawItem(stackToRender, slotX - SLOT_SIZE / 2, slotY - SLOT_SIZE / 2);
			context.drawItemInSlot(this.textRenderer, stackToRender, slotX - SLOT_SIZE / 2, slotY - SLOT_SIZE / 2);


			// Draw slot name
			String slotName = slot.type().path();
			context.drawText(this.textRenderer, slotName, slotX - this.textRenderer.getWidth(slotName) / 2, slotY + SLOT_SIZE, 0xFFFFFF, true);
		}
	}

	private void renderTooltips(DrawContext context, int mouseX, int mouseY, int centerX, int centerY) {
		// Tooltip for the larger root component
		if (isMouseOver(mouseX, mouseY, centerX - ROOT_ITEM_SIZE / 2, centerY - ROOT_ITEM_SIZE / 2, ROOT_ITEM_SIZE, ROOT_ITEM_SIZE)) {
			context.drawTooltip(this.textRenderer, getTooltipFromItem(this.client, rootStack), mouseX, mouseY);
		}

		if (slots.isEmpty()) {
			return;
		}

		double radius = Math.min(width, height) / 3.0;
		double angleStep = 2 * Math.PI / slots.size();

		for (int i = 0; i < slots.size(); i++) {
			double angle = angleStep * i - (Math.PI / 2);
			int slotX = (int) (centerX + radius * Math.cos(angle));
			int slotY = (int) (centerY + radius * Math.sin(angle));

			if (isMouseOver(mouseX, mouseY, slotX - SLOT_SIZE / 2, slotY - SLOT_SIZE / 2)) {
				UpgradeSlot slot = slots.get(i);
				Optional<ItemStack> upgradeStack = slot.content().flatMap(converter::toStack);
				if (upgradeStack.isPresent()) {
					context.drawTooltip(this.textRenderer, getTooltipFromItem(this.client, upgradeStack.get()), mouseX, mouseY);
				} else {
					List<Text> tooltipLines = new ArrayList<>();
					tooltipLines.add(Text.literal("Empty Slot: " + slot.type().path()));
					if (slot.description() != null && !slot.description().isBlank()) {
						tooltipLines.add(Text.literal(slot.description()));
					}
					context.drawTooltip(this.textRenderer, tooltipLines, mouseX, mouseY);
				}
			}
		}

		// Tooltips for item bar
		if (!availableUpgrades.isEmpty()) {
			int barY = this.height - ITEM_BAR_HEIGHT - 5;
			int totalWidth = availableUpgrades.size() * (ITEM_BAR_SLOT_SIZE + ITEM_BAR_SLOT_PADDING * 2);
			int barX = (this.width - totalWidth) / 2;

			for (int i = 0; i < availableUpgrades.size(); i++) {
				int itemX = barX + i * (ITEM_BAR_SLOT_SIZE + ITEM_BAR_SLOT_PADDING * 2) + ITEM_BAR_SLOT_PADDING;
				if (isMouseOver(mouseX, mouseY, itemX, barY, ITEM_BAR_SLOT_SIZE, ITEM_BAR_SLOT_SIZE)) {
					context.drawTooltip(this.textRenderer, getTooltipFromItem(this.client, availableUpgrades.get(i)), mouseX, mouseY);
					return;
				}
			}
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) { // Left click
			if (handleUpgradeSlotClick(mouseX, mouseY)) return true;
			if (handleItemBarClick(mouseX, mouseY)) return true;
			if (!heldStack.isEmpty()) {
				dropHeldItem();
				return true;
			}
		} else if (button == 1) { // Right click for quick remove
			if (handleUpgradeSlotRightClick(mouseX, mouseY)) return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	private boolean handleUpgradeSlotRightClick(double mouseX, double mouseY) {
		if (slots.isEmpty() || !heldStack.isEmpty()) {
			return false;
		}
		int centerX = this.width / 2;
		int centerY = this.height / 2;
		double radius = Math.min(width, height) / 3.0;
		double angleStep = 2 * Math.PI / slots.size();

		for (int i = 0; i < slots.size(); i++) {
			double angle = angleStep * i - (Math.PI / 2);
			int slotX = (int) (centerX + radius * Math.cos(angle));
			int slotY = (int) (centerY + radius * Math.sin(angle));

			if (isMouseOver((int) mouseX, (int) mouseY, slotX - SLOT_SIZE / 2, slotY - SLOT_SIZE / 2)) {
				UpgradeSlot clickedSlot = slots.get(i);
				clickedSlot.content().ifPresent(content -> converter.toStack(content).ifPresent(stackInSlot -> {
					Component newRootComponent = mutater.removeSlot(this.component, clickedSlot.id());
					updateComponentState(newRootComponent);
					this.availableUpgrades.add(stackInSlot);
				}));
				return true;
			}
		}
		return false;
	}

	private boolean handleUpgradeSlotClick(double mouseX, double mouseY) {
		if (slots.isEmpty()) return false;
		int centerX = this.width / 2;
		int centerY = this.height / 2;
		double radius = Math.min(width, height) / 3.0;
		double angleStep = 2 * Math.PI / slots.size();

		for (int i = 0; i < slots.size(); i++) {
			double angle = angleStep * i - (Math.PI / 2);
			int slotX = (int) (centerX + radius * Math.cos(angle));
			int slotY = (int) (centerY + radius * Math.sin(angle));

			if (isMouseOver((int) mouseX, (int) mouseY, slotX - SLOT_SIZE / 2, slotY - SLOT_SIZE / 2)) {
				UpgradeSlot clickedSlot = slots.get(i);

				if (!heldStack.isEmpty() && heldComponent != null) {
					if (clickedSlot.validator().test(heldComponent)) {
						Optional<Component> previousContent = clickedSlot.content();
						Component newRootComponent = mutater.setSlot(this.component, clickedSlot.id(), heldComponent);
						updateComponentState(newRootComponent);
						if (previousContent.isPresent()) {
							heldStack = converter.toStack(previousContent.get()).orElse(ItemStack.EMPTY);
							heldComponent = previousContent.get();
							heldOriginalSlot = i;
						} else {
							clearHeldItem();
						}
						return true;
					}
				} else if (clickedSlot.content().isPresent()) {
					Component content = clickedSlot.content().get();
					heldStack = converter.toStack(content).orElse(ItemStack.EMPTY);
					heldComponent = content;
					heldOriginalSlot = i;
					Component newRootComponent = mutater.removeSlot(this.component, clickedSlot.id());
					updateComponentState(newRootComponent);
					return true;
				}
				return true;
			}
		}
		return false;
	}

	private boolean handleItemBarClick(double mouseX, double mouseY) {
		int barY = this.height - ITEM_BAR_HEIGHT - 5;
		boolean isMouseOverBar = mouseY >= barY - 2 && mouseY < barY + ITEM_BAR_HEIGHT + 2;
		if (!isMouseOverBar) return false;

		if (!heldStack.isEmpty()) {
			this.availableUpgrades.add(heldStack);
			clearHeldItem();
			return true;
		}

		int totalWidth = availableUpgrades.size() * (ITEM_BAR_SLOT_SIZE + ITEM_BAR_SLOT_PADDING * 2);
		int barX = (this.width - totalWidth) / 2;

		for (int i = 0; i < availableUpgrades.size(); i++) {
			int itemX = barX + i * (ITEM_BAR_SLOT_SIZE + ITEM_BAR_SLOT_PADDING * 2) + ITEM_BAR_SLOT_PADDING;
			if (isMouseOver((int) mouseX, (int) mouseY, itemX, barY, ITEM_BAR_SLOT_SIZE, ITEM_BAR_SLOT_SIZE)) {
				ItemStack stackToPick = this.availableUpgrades.remove(i);
				heldStack = stackToPick;
				heldComponent = converter.toComponent(stackToPick).orElse(null);
				heldOriginalSlot = -1;
				return true;
			}
		}
		return true;
	}

	private void updateComponentState(Component newComponent) {
		if (newComponent instanceof CustomizableComponent newCustomizable) {
			this.component = newCustomizable;
			this.slots = new ArrayList<>(newCustomizable.getUpgradeSlots());
			this.rootStack = converter.toStack(this.component).orElse(ItemStack.EMPTY);
		}
	}

	private void dropHeldItem() {
		if (heldOriginalSlot == -1) {
			this.availableUpgrades.add(heldStack);
		} else if (heldOriginalSlot < slots.size()) {
			UpgradeSlot targetSlot = slots.get(heldOriginalSlot);
			Component newRootComponent = mutater.setSlot(this.component, targetSlot.id(), heldComponent);
			updateComponentState(newRootComponent);
		}
		clearHeldItem();
	}

	private void clearHeldItem() {
		this.heldStack = ItemStack.EMPTY;
		this.heldComponent = null;
		this.heldOriginalSlot = -1;
	}

	private boolean isMouseOver(int mouseX, int mouseY, int x, int y, int width, int height) {
		return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
	}

	private boolean isMouseOver(int mouseX, int mouseY, int x, int y) {
		return isMouseOver(mouseX, mouseY, x, y, SLOT_SIZE, SLOT_SIZE);
	}

	private void drawConnectionPath(DrawContext context, int startX, int startY, int endX, int endY, int thickness, int color) {
		int halfThickness = thickness / 2;
		int ceilHalfThickness = (thickness + 1) / 2;
		int midY = startY + (endY - startY) / 2;
		context.fill(startX - halfThickness, Math.min(startY, midY), startX + ceilHalfThickness, Math.max(startY, midY), color);
		context.fill(Math.min(startX, endX), midY - halfThickness, Math.max(startX, endX), midY + ceilHalfThickness, color);
		context.fill(endX - halfThickness, Math.min(midY, endY), endX + ceilHalfThickness, Math.max(midY, endY), color);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
