package com.sigmundgranaas.forgero.smithingrework.block.entity;

import com.mojang.blaze3d.systems.RenderSystem;

import com.sigmundgranaas.forgero.core.context.Context;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class BloomeryScreen extends HandledScreen<BloomeryScreenHandler> {
	private static final Identifier TEXTURE = new Identifier("textures/gui/container/blast_furnace.png");

	public BloomeryScreen(BloomeryScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
	}

	@Override
	protected void init() {
		super.init();
		// Center the title
		titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
	}

	@Override
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURE);
		int x = (width - backgroundWidth) / 2;
		int y = (height - backgroundHeight) / 2;
		int k;
		context.drawTexture(TEXTURE, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);

		if (((BloomeryScreenHandler)this.handler).isSmelting()) {
			k = ((BloomeryScreenHandler)this.handler).getFuelProgress();
			context.drawTexture(TEXTURE, x + 56, y + 36 + 12 - k, 176, 12 - k, 14, k + 1);
		}
		k = ((BloomeryScreenHandler)this.handler).getCookProgress();
		context.drawTexture(TEXTURE, x + 79, y + 34, 176, 14, k + 1, 16);

	}

	@Override
	public void render(DrawContext matrices, int mouseX, int mouseY, float delta) {
		renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		drawMouseoverTooltip(matrices, mouseX, mouseY);
	}
}
