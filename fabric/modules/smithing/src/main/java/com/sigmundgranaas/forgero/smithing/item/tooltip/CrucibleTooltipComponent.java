package com.sigmundgranaas.forgero.smithing.item.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class CrucibleTooltipComponent implements TooltipComponent {
	private static final Identifier SLOT_TEXTURE = new Identifier("textures/gui/container/bundle.png");
	private final ItemStack storedItem;
	private final int count;

	public CrucibleTooltipComponent(CrucibleTooltipData data) {
		this.storedItem = data.storedItem();
		this.count = data.count();
	}

	@Override
	public int getHeight() {
		return 20; // Height of one slot
	}

	@Override
	public int getWidth(TextRenderer textRenderer) {
		return 18; // Width of one slot
	}

	@Override
	public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
		// Always draw slot background (gray box)
		RenderSystem.setShaderTexture(0, SLOT_TEXTURE);
		context.drawTexture(SLOT_TEXTURE, x, y, 0, 0, 18, 18, 128, 128);

		// Only draw item if not empty
		if (!storedItem.isEmpty()) {
			// Render the item
			context.drawItem(storedItem, x + 1, y + 1);

			// Render count if > 1
			if (count > 1) {
				String countText = String.valueOf(count);
				context.drawItemInSlot(textRenderer, storedItem, x + 1, y + 1, countText);
			}
		}
	}
}
