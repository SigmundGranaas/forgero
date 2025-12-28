package com.sigmundgranaas.forgero.blocks.assembly;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Client-side screen for the Assembly Station.
 * <p>
 * Renders the disassembly UI with:
 * <ul>
 *   <li>Input slot for the item to disassemble</li>
 *   <li>3x3 grid of result slots showing parts</li>
 *   <li>Arrow indicating disassembly direction</li>
 * </ul>
 */
public class AssemblyStationScreen extends HandledScreen<AssemblyStationScreenHandler> {

	private static final Identifier TEXTURE = new Identifier("forgero", "textures/gui/container/assembly_table_ui.png");

	private static final int BACKGROUND_WIDTH = 176;
	private static final int BACKGROUND_HEIGHT = 166;

	public AssemblyStationScreen(AssemblyStationScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		this.backgroundWidth = BACKGROUND_WIDTH;
		this.backgroundHeight = BACKGROUND_HEIGHT;
	}

	@Override
	protected void init() {
		super.init();
		titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
	}

	@Override
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
		int x = (width - backgroundWidth) / 2;
		int y = (height - backgroundHeight) / 2;
		context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		renderBackground(context);
		super.render(context, mouseX, mouseY, delta);
		drawMouseoverTooltip(context, mouseX, mouseY);
	}
}
