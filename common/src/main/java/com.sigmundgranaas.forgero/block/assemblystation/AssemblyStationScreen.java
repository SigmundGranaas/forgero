package com.sigmundgranaas.forgero.block.assemblystation;

import com.mojang.blaze3d.systems.RenderSystem;

import com.sigmundgranaas.forgero.block.upgradestation.UpgradeStationScreenHandler;
import com.sigmundgranaas.forgero.tooltip.v2.TooltipConfiguration;
import com.sigmundgranaas.forgero.tooltip.v2.section.SlotSectionWriter;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURE);
		int x = (width - backgroundWidth) / 2;
		int y = (height - backgroundHeight) / 2;
		context.drawTexture(TEXTURE, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		renderBackground(context);
		super.render(context, mouseX, mouseY, delta);
		drawMouseoverTooltip(context, mouseX, mouseY);
		renderCustomTooltip(context, new ArrayList<>(), mouseX, mouseY);

	}

	public void renderCustomTooltip(DrawContext context, List<Text> lines, int mouseX, int mouseY) {
		if (isPointWithinBounds(handler.getSlot(0).x, handler.getSlot(0).y, 16, 16, mouseX, mouseY) && handler.getSlot(0).getStack().isEmpty()) {
			if(this.handler.getCursorStack().isDamaged()){
				lines.add(Text.literal("Damaged tools cannot be disassembled."));
			}else{
				lines.add(Text.literal("Disassemble tools and parts here."));
			}
			context.drawTooltip(this.textRenderer, lines, mouseX, mouseY);
		}
	}
}
