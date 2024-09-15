package com.sigmundgranaas.forgero.block.upgradestation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sigmundgranaas.forgero.tooltip.v2.TooltipConfiguration;
import com.sigmundgranaas.forgero.tooltip.v2.section.SlotSectionWriter;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;



public class UpgradeStationScreen extends HandledScreen<UpgradeStationScreenHandler> {
	private static final Identifier TEXTURE = new Identifier("forgero", "textures/gui/container/upgrade_table_ui.png");
	private final int backgroundHeight = 220;
	private final int backgroundWidth = 176;

	private int tickCounter;

	public UpgradeStationScreen(UpgradeStationScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, Text.translatable("block.forgero.upgrade_station"));
		super.backgroundHeight = this.backgroundHeight;
		super.backgroundWidth = this.backgroundWidth;
	}


	public void renderCustomTooltip(DrawContext context, List<Text> lines, int mouseX, int mouseY) {
		for (UpgradeStationScreenHandler.PositionedSlot slot : this.handler.slotPool.stream().filter(Objects::nonNull).map(UpgradeStationScreenHandler.PositionedSlot.class::cast).toList()) {
			if (isPointWithinBounds(slot.xPosition, slot.yPosition, 16, 16, mouseX, mouseY) && !slot.hasStack() && slot.slot != null) {
				lines.addAll(new SlotSectionWriter.SlotWriter(slot.slot, TooltipConfiguration.builder().build()).entries());
				context.drawTooltip(this.textRenderer, lines, mouseX, mouseY);
				renderCycledSlot(slot, lines, mouseX, mouseY, context);
			}
		}
		if (isPointWithinBounds(handler.compositeSlot.x, handler.compositeSlot.y, 16, 16, mouseX, mouseY) && !handler.compositeSlot.hasStack()) {
			lines.add(Text.literal("Place you Forgero tool or weapon here"));
			context.drawTooltip(this.textRenderer, lines, mouseX, mouseY);
			renderCycledTag(new Identifier("forgero:holdable"), mouseX, mouseY, context);
		}
	}


	public void renderCycledSlot(UpgradeStationScreenHandler.PositionedSlot slot, List<Text> tooltip, int mouseX, int mouseY, DrawContext context) {
		if (slot.getSlot() != null) {
			com.sigmundgranaas.forgero.core.state.Slot slot1 = slot.getSlot();

			Identifier id = new Identifier("forgero", slot1.typeName().toLowerCase(Locale.ENGLISH));

			renderCycledTag(id, mouseX, mouseY, context);
		}
	}

	public void renderCycledTag(Identifier id, int mouseX, int mouseY, DrawContext context) {
		TagKey<Item> key = TagKey.of(Registries.ITEM.getKey(), id);
		List<RegistryEntry<Item>> entries = new ArrayList<>();
		// List of items that this slot accepts
		Registries.ITEM.iterateEntries(key).forEach(entries::add);
		List<Item> acceptedItems = entries.stream().map(RegistryEntry::value).toList();

		// Select an item to display based on the current game tick
		try {
			Item itemToShow = acceptedItems.get((this.tickCounter / 40) % acceptedItems.size());
			context.drawItem(itemToShow.getDefaultStack(), mouseX, mouseY);
		} catch (Exception ignored) {
		}
	}

	private void drawDynamicSlot(DrawContext context, Slot slot) {
		int x = slot.x - 1;
		int y = slot.y - 1;

		int centerX = (this.width - this.backgroundWidth) / 2;
		int centerY = (this.height - this.backgroundHeight) / 2;

		context.drawTexture(TEXTURE, centerX + x, centerY + y, 194, 55, 18, 18);
	}

	@Override
	public void render(DrawContext matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);
		this.tickCounter++;

		drawMouseoverTooltip(matrices, mouseX, mouseY);
		if (this.handler.compositeSlot.hasStack()) {
			renderCustomTooltip(matrices, new ArrayList<>(), mouseX, mouseY);
		}
	}


	// Render the vertical line from parent's middle to halfway to child
	private void renderVerticalHalfway(DrawContext context, int startX, int startY, int endX, int endY, int thickness, int color, float opacity) {
		// Calculate the halfway point
		int halfWayY = startY + ((endY - startY) / 2);
		renderLineBetweenSlots(context, startX + x, startY + y, startX + x, halfWayY + y, thickness, color, opacity);
	}

	// Render the horizontal line from halfway of parent to directly above/below child
	private void renderHorizontalToChild(DrawContext context, int startX, int startY, int endX, int endY, int thickness, int color, float opacity) {
		// Calculate the halfway point
		int halfWayY = startY + ((endY - startY) / 2);
		renderLineBetweenSlots(context, startX + x, halfWayY + y, endX + x, halfWayY + y, thickness, color, opacity);
	}


	@Override
	protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
		super.onMouseClick(slot, slotId, button, actionType);
	}

	// Render the vertical line from the end of the horizontal line to the child
	private void renderVerticalToChild(DrawContext context, int startX, int startY, int endX, int endY, int thickness, int color, float opacity) {
		// Calculate the halfway point
		int halfWayY = startY + ((endY - startY) / 2);
		renderLineBetweenSlots(context, endX + x, halfWayY + y, endX + x, endY + y, thickness, color, opacity);
	}

	// Call these functions in renderLinesBetweenSlots
	public void renderLinesBetweenSlots(DrawContext context) {
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
				renderVerticalHalfway(context, parentCenterX, parentCenterY, childCenterX, childCenterY, thickness, color, opacity);

				// Draw the horizontal line from halfway of parent to directly above/below child
				renderHorizontalToChild(context, parentCenterX, parentCenterY, childCenterX, childCenterY, thickness, color, opacity);

				// Draw the vertical line from the end of the horizontal line to the child
				renderVerticalToChild(context, parentCenterX, parentCenterY, childCenterX, childCenterY, thickness, color, opacity);
			}
		}
	}


	private void renderLineBetweenSlots(DrawContext context, int startLineX, int startLineY, int endLineX, int endLineY, int thickness, int color, float opacity) {
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
				context.fill( currentLineX + t, currentLineY, currentLineX + t + 1, currentLineY + 1, rgbaColor);
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

	@Override
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		int centerX = (this.width - this.backgroundWidth) / 2;
		int centerY = (this.height - this.backgroundHeight) / 2;
		context.drawTexture(TEXTURE, centerX, centerY, 0, 0, backgroundWidth, backgroundHeight);

		if (this.handler.compositeSlot.hasStack()) {
			this.renderLinesBetweenSlots(context);
			for (Slot slot : handler.slotPool) {
				if (slot instanceof UpgradeStationScreenHandler.PositionedSlot positioned && slot.isEnabled() && positioned.slot != null) {
					this.drawDynamicSlot(context, slot);
				}
			}
		}
		this.drawDynamicSlot(context, handler.compositeSlot);
	}
}
