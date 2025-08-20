package com.sigmundgranaas.forgero.smithing.item.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.item.Items;

public class CrucibleTooltipComponent implements TooltipComponent {
	private static final Identifier SLOT_TEXTURE = new Identifier("textures/gui/container/bundle.png");
	private final ItemStack storedItem;
	private final int count;
	private final Identifier liquidType;

	public CrucibleTooltipComponent(CrucibleTooltipData data) {
		this.storedItem = data.storedItem();
		this.count = data.count();
		this.liquidType = data.liquidType(); // Add this to CrucibleTooltipData
	}

	@Override
	public int getHeight() {
		return 20;
	}

	@Override
	public int getWidth(TextRenderer textRenderer) {
		return 18;
	}

	@Override
	public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
		RenderSystem.setShaderTexture(0, SLOT_TEXTURE);
		context.drawTexture(SLOT_TEXTURE, x, y, 0, 0, 18, 18, 128, 128);

		if (!storedItem.isEmpty()) {
			context.drawItem(storedItem, x + 1, y + 1);
			if (count > 1) {
				String countText = String.valueOf(count);
				context.drawItemInSlot(textRenderer, storedItem, x + 1, y + 1, countText);
			}
		} else if (liquidType != null) {
			// Render animated lava block texture
			SpriteIdentifier lavaSpriteId = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("minecraft", "block/lava_still"));
			Sprite lavaSprite = MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).apply(lavaSpriteId.getTextureId());
			context.drawSprite(x + 1, y + 1, 0, 16, 16, lavaSprite);
		}
	}
}
