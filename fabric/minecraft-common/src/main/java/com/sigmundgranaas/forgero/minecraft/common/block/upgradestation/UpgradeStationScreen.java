package com.sigmundgranaas.forgero.minecraft.common.block.upgradestation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.TooltipConfiguration;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.section.SlotSectionWriter;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;


public class UpgradeStationScreen extends HandledScreen<UpgradeStationScreenHandler> {
	private static final Identifier TEXTURE = new Identifier("forgero", "textures/gui/container/upgrade_table_ui.png");
	private static final Identifier textureId = new Identifier("minecraft", "textures/gui/advancements/widgets.png");
	private final ItemRenderer itemRenderer;
	private final int backgroundHeight = 220;
	private final int backgroundWidth = 176;

	private int tickCounter;

	public UpgradeStationScreen(UpgradeStationScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, Text.translatable("block.forgero.upgrade_station"));
		this.itemRenderer = MinecraftClient.getInstance().getItemRenderer();
		super.backgroundHeight = this.backgroundHeight;
		super.backgroundWidth = this.backgroundWidth;
	}


	public void renderCustomTooltip(MatrixStack matrices, List<Text> lines, int mouseX, int mouseY) {
		for (UpgradeStationScreenHandler.PositionedSlot slot : this.handler.slotPool.stream().filter(Objects::nonNull).map(UpgradeStationScreenHandler.PositionedSlot.class::cast).toList()) {
			if (isPointWithinBounds(slot.xPosition, slot.yPosition, 16, 16, mouseX, mouseY) && !slot.hasStack() && slot.slot != null) {
				lines.addAll(new SlotSectionWriter.SlotWriter(slot.slot, TooltipConfiguration.builder().build()).entries());
				super.renderTooltip(matrices, lines, mouseX, mouseY);
				renderCycledSlot(slot, lines, mouseX, mouseY);
			}
		}
		if (isPointWithinBounds(handler.compositeSlot.x, handler.compositeSlot.y, 16, 16, mouseX, mouseY) && !handler.compositeSlot.hasStack()) {
			lines.add(Text.literal("Place you Forgero tool or weapon here"));
			super.renderTooltip(matrices, lines, mouseX, mouseY);
			renderCycledTag(new Identifier("forgero:holdable"), mouseX, mouseY);
		}
	}


	public void renderCycledSlot(UpgradeStationScreenHandler.PositionedSlot slot, List<Text> tooltip, int mouseX, int mouseY) {
		if (slot.getSlot() != null) {
			com.sigmundgranaas.forgero.core.state.Slot slot1 = slot.getSlot();

			Identifier id = new Identifier("forgero", slot1.typeName().toLowerCase(Locale.ENGLISH));

			renderCycledTag(id, mouseX, mouseY);
		}
	}

	public void renderCycledTag(Identifier id, int mouseX, int mouseY) {
		TagKey<Item> key = TagKey.of(Registry.ITEM_KEY, id);
		List<RegistryEntry<Item>> entries = new ArrayList<>();
		// List of items that this slot accepts
		Registry.ITEM.iterateEntries(key).forEach(entries::add);
		List<Item> acceptedItems = entries.stream().map(RegistryEntry::value).toList();

		// Select an item to display based on the current game tick
		Item itemToShow = acceptedItems.get((this.tickCounter / 40) % acceptedItems.size());

		this.itemRenderer.renderInGuiWithOverrides(itemToShow.getDefaultStack(), mouseX, mouseY);
	}


	@Override
	protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURE);
		int centerX = (this.width - this.backgroundWidth) / 2;
		int centerY = (this.height - this.backgroundHeight) / 2;
		drawTexture(matrices, centerX, centerY, 0, 0, backgroundWidth, backgroundHeight);


		if (this.handler.compositeSlot.hasStack()) {
			this.renderLinesBetweenSlots(matrices);
			for (Slot slot : handler.slotPool) {
				// Render your slots here
				if (slot instanceof UpgradeStationScreenHandler.PositionedSlot positioned && slot.isEnabled() && positioned.slot != null) {
					this.drawSlot(matrices, slot);
				}
			}
		}
		this.drawSlot(matrices, handler.compositeSlot);
	}

	private void drawSlot(MatrixStack matrices, Slot slot) {
		// You'll want to render your slot here. I'll use default slot rendering as an example,
		// but you might have your own custom slot rendering.

		int x = slot.x - 1;
		int y = slot.y - 1;

		int centerX = (this.width - this.backgroundWidth) / 2;
		int centerY = (this.height - this.backgroundHeight) / 2;
		RenderSystem.setShaderTexture(0, TEXTURE);

		this.drawTexture(matrices, centerX + x, centerY + y, 194, 55, 18, 18);
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		this.tickCounter++;

		// Add this line to your render method

		drawMouseoverTooltip(matrices, mouseX, mouseY);
		if (this.handler.compositeSlot.hasStack()) {
			renderCustomTooltip(matrices, new ArrayList<>(), mouseX, mouseY);
		}
	}


	// Render the vertical line from parent's middle to halfway to child
	private void renderVerticalHalfway(MatrixStack matrices, int startX, int startY, int endX, int endY, int thickness, int color, float opacity) {
		// Calculate the halfway point
		int halfWayY = startY + ((endY - startY) / 2);
		renderLineBetweenSlots(matrices, startX + x, startY + y, startX + x, halfWayY + y, thickness, color, opacity);
	}

	// Render the horizontal line from halfway of parent to directly above/below child
	private void renderHorizontalToChild(MatrixStack matrices, int startX, int startY, int endX, int endY, int thickness, int color, float opacity) {
		// Calculate the halfway point
		int halfWayY = startY + ((endY - startY) / 2);
		renderLineBetweenSlots(matrices, startX + x, halfWayY + y, endX + x, halfWayY + y, thickness, color, opacity);
	}


	@Override
	protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
		super.onMouseClick(slot, slotId, button, actionType);
	}

	// Render the vertical line from the end of the horizontal line to the child
	private void renderVerticalToChild(MatrixStack matrices, int startX, int startY, int endX, int endY, int thickness, int color, float opacity) {
		// Calculate the halfway point
		int halfWayY = startY + ((endY - startY) / 2);
		renderLineBetweenSlots(matrices, endX + x, halfWayY + y, endX + x, endY + y, thickness, color, opacity);
	}

	// Call these functions in renderLinesBetweenSlots
	public void renderLinesBetweenSlots(MatrixStack matrices) {
		for (Slot slot : this.handler.slotPool) {
			if (!(slot instanceof UpgradeStationScreenHandler.PositionedSlot positionedSlot) || !slot.isEnabled()) {
				continue;
			}
			if (positionedSlot.parent != null && positionedSlot.parent.isEnabled()) {
				Slot parent = positionedSlot.parent;

				int parentCenterX = parent.x + 16 / 2;
				int parentCenterY = parent.y + 16 / 2;

				if (parent instanceof UpgradeStationScreenHandler.PositionedSlot positionedSlot1) {
					parentCenterX = positionedSlot1.xPosition + 16 / 2;
					parentCenterY = positionedSlot1.yPosition + 16 / 2;
				}
				int childCenterX = positionedSlot.xPosition + 16 / 2;
				int childCenterY = positionedSlot.yPosition + 16 / 2;

				int thickness = 2;
				int color = 0xFFFFFF;
				float opacity = 0.5f;

				// Draw the vertical line from parent's middle to halfway to child
				renderVerticalHalfway(matrices, parentCenterX, parentCenterY, childCenterX, childCenterY, thickness, color, opacity);

				// Draw the horizontal line from halfway of parent to directly above/below child
				renderHorizontalToChild(matrices, parentCenterX, parentCenterY, childCenterX, childCenterY, thickness, color, opacity);

				// Draw the vertical line from the end of the horizontal line to the child
				renderVerticalToChild(matrices, parentCenterX, parentCenterY, childCenterX, childCenterY, thickness, color, opacity);
			}
		}
	}


	// This method draws a line from start point (startLineX, startLineY) to end point (endLineX, endLineY)
	private void renderLineBetweenSlots(MatrixStack matrices, int startLineX, int startLineY, int endLineX, int endLineY, int thickness, int color, float opacity) {
		// Calculate the line parameters
		float deltaX = endLineX - startLineX;
		float deltaY = endLineY - startLineY;
		double lineLength = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
		int rgbaColor = (color & 0x00FFFFFF) | ((int) (255 * opacity) << 24); // Applying the opacity

		// Render the line with the specified thickness
		for (int t = 0; t < thickness; t++) {
			for (int i = 0; i < lineLength; i++) {
				float ratio = (float) (i / lineLength);
				int currentLineX = (int) (startLineX + ratio * deltaX);
				int currentLineY = (int) (startLineY + ratio * deltaY);

				// Draw a dot on the line, offset by the thickness
				fill(matrices, currentLineX + t, currentLineY, currentLineX + t + 1, currentLineY + 1, rgbaColor);
			}
		}
	}

	@Override
	protected void init() {
		super.init();
		// Center the title
		titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
		titleY = 5;

		playerInventoryTitleY = 127;
	}

}
