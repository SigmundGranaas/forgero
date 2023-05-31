package com.sigmundgranaas.forgero.minecraft.common.block.upgradestation;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class UpgradeStationScreen extends HandledScreen<UpgradeStationScreenHandler> {
	private static final Identifier TEXTURE = new Identifier("forgero", "textures/gui/container/assembly_table_ui.png");

	private final StateService service;
	private final ItemRenderer itemRenderer;

	public UpgradeStationScreen(UpgradeStationScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, Text.translatable("block.forgero.assembly_station"));
		this.itemRenderer = MinecraftClient.getInstance().getItemRenderer();
		this.service = StateService.INSTANCE;
	}

	@Override
	protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURE);
		int x = (width - backgroundWidth) / 2;
		int y = (height - backgroundHeight) / 2;
		drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		drawMouseoverTooltip(matrices, mouseX, mouseY);
	}

	@Override
	protected void init() {
		super.init();
		// Center the title
		titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
	}

}
