package com.sigmundgranaas.forgero.blocks.upgrade;

import com.sigmundgranaas.forgero.blocks.common.screen.ComponentSlot;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Client-side screen for the Upgrade Station.
 * <p>
 * Renders the upgrade station UI with:
 * <ul>
 *   <li>Background texture</li>
 *   <li>Dynamic slot positions based on component tree</li>
 *   <li>Connection lines between parent and child slots</li>
 *   <li>Slot type tooltips</li>
 * </ul>
 */
public class UpgradeStationScreen extends HandledScreen<UpgradeStationScreenHandler> {

	private static final Identifier TEXTURE = new Identifier("forgero", "textures/gui/container/upgrade_table_ui.png");

	/**
	 * Background dimensions.
	 */
	private static final int BACKGROUND_WIDTH = 176;
	private static final int BACKGROUND_HEIGHT = 222;

	/**
	 * Line color for slot connections (ARGB).
	 */
	private static final int LINE_COLOR = 0xFF555555;

	public UpgradeStationScreen(UpgradeStationScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		this.backgroundWidth = BACKGROUND_WIDTH;
		this.backgroundHeight = BACKGROUND_HEIGHT;
		// Adjust label positions
		this.titleY = 6;
		this.playerInventoryTitleY = this.backgroundHeight - 94;
	}

	@Override
	protected void init() {
		super.init();
		// Center the title
		titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
	}

	@Override
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
		int x = (width - backgroundWidth) / 2;
		int y = (height - backgroundHeight) / 2;

		// Draw main background
		context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);

		// Draw slot connections
		drawSlotConnections(context, x, y);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		renderBackground(context);
		super.render(context, mouseX, mouseY, delta);
		drawMouseoverTooltip(context, mouseX, mouseY);
	}

	/**
	 * Draws lines connecting parent slots to child slots.
	 */
	private void drawSlotConnections(DrawContext context, int guiX, int guiY) {
		for (Slot slot : handler.slots) {
			if (slot instanceof ComponentSlot componentSlot && componentSlot.isEnabled()) {
				int slotX = guiX + componentSlot.getDynamicX() + 8; // Center of slot
				int slotY = guiY + componentSlot.getDynamicY() + 8;

				// Draw line to parent (composite slot at top)
				int parentY = guiY + UpgradeStationScreenHandler.COMPOSITE_SLOT_Y + 8;
				int parentX = guiX + UpgradeStationScreenHandler.COMPOSITE_SLOT_X + 8;

				// Only draw if this slot is below the composite slot
				if (slotY > parentY) {
					// Vertical line from parent
					context.fill(parentX, parentY + 8, parentX + 1, slotY - 8, LINE_COLOR);
					// Horizontal line to slot
					int minX = Math.min(parentX, slotX);
					int maxX = Math.max(parentX, slotX);
					context.fill(minX, slotY - 8, maxX + 1, slotY - 7, LINE_COLOR);
					// Vertical line to slot
					context.fill(slotX, slotY - 8, slotX + 1, slotY, LINE_COLOR);
				}
			}
		}
	}

}
