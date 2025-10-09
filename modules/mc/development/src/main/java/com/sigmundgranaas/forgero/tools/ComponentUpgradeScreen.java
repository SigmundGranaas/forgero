package com.sigmundgranaas.forgero.tools;

import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.UpgradeSlot;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.math.RotationAxis;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ComponentUpgradeScreen extends Screen {
	private final CustomizableComponent component;
	private final ComponentConverter converter;
	private final List<UpgradeSlot> slots;
	private final ItemStack rootStack;

	private static final int SLOT_SIZE = 16;
	private static final int ROOT_ITEM_SIZE = 64;
	private static final int LINE_THICKNESS = 2;
	private static final int LINE_COLOR = 0xFF808080; // Gray

	public ComponentUpgradeScreen(CustomizableComponent component, ComponentConverter converter) {
		super(Text.translatable("forgero.dev.upgrade_viewer.title"));
		this.component = component;
		this.converter = converter;
		this.slots = component.getUpgradeSlots();
		this.rootStack = converter.toStack(component).orElse(ItemStack.EMPTY);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderBackground(context);
		super.render(context, mouseX, mouseY, delta);

		int centerX = this.width / 2;
		int centerY = this.height / 2;

		renderSlotsAndLines(context, centerX, centerY);
		renderComponents(context, centerX, centerY);
		renderTooltips(context, mouseX, mouseY, centerX, centerY);
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

			// Center the model in the screen
			context.getMatrices().translate(centerX, centerY, 150.0F);

			// Scale it up. The negative Y is to flip the model upright.
			context.getMatrices().scale(1.0F * ROOT_ITEM_SIZE, -1.0F * ROOT_ITEM_SIZE, 1.0F * ROOT_ITEM_SIZE);

			// Apply rotations
			float yRotation = (float) ((System.currentTimeMillis() / 20) % 360);
			context.getMatrices().multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yRotation));
			context.getMatrices().multiply(RotationAxis.POSITIVE_X.rotationDegrees(-20f)); // Static tilt

			// Render the item with proper lighting
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

		// Vertical line from start point
		context.fill(startX - halfThickness, Math.min(startY, midY), startX + ceilHalfThickness, Math.max(startY, midY), color);

		// Horizontal line connecting the two vertical lines
		context.fill(Math.min(startX, endX), midY - halfThickness, Math.max(startX, endX), midY + ceilHalfThickness, color);

		// Vertical line to end point
		context.fill(endX - halfThickness, Math.min(midY, endY), endX + ceilHalfThickness, Math.max(midY, endY), color);
	}


	@Override
	public boolean shouldPause() {
		return false;
	}
}
