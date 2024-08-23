package com.sigmundgranaas.forgero.minecraft.common.block.assemblystation;

import com.mojang.blaze3d.systems.RenderSystem;

import com.sigmundgranaas.forgero.core.Forgero;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import org.jetbrains.annotations.NotNull;

public class AssemblyStationScreen extends HandledScreen<AssemblyStationScreenHandler> {
	private static final @NotNull Identifier TEXTURE = new Identifier(Forgero.NAMESPACE, "textures/gui/container/assembly_table_ui.png");

	@SuppressWarnings("unused")
	public AssemblyStationScreen(AssemblyStationScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, Text.translatable("block.forgero.assembly_station"));
	}

	@Override
	protected void init() {
		super.init();
		// Center the title
		titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
	}

	@Override
	protected void drawBackground(@NotNull DrawContext context, float delta, int mouseX, int mouseY) {
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURE);
		int x = (width - backgroundWidth) / 2;
		int y = (height - backgroundHeight) / 2;
		context.drawTexture(TEXTURE, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);
	}

	@Override
	public void render(@NotNull DrawContext context, int mouseX, int mouseY, float delta) {
		renderBackground(context);
		super.render(context, mouseX, mouseY, delta);
		drawMouseoverTooltip(context, mouseX, mouseY);
	}
}
