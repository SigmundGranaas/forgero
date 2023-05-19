package com.sigmundgranaas.forgero.minecraft.common.block.assemblystation;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class AssemblyStationScreen extends HandledScreen<AssemblyStationScreenHandler> {
	private static final Identifier TEXTURE = new Identifier("forgero", "textures/gui/container/assembly_table_ui.png");

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
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {

	}

}
