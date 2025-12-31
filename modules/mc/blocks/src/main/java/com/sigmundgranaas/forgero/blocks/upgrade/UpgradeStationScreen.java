package com.sigmundgranaas.forgero.blocks.upgrade;

import com.sigmundgranaas.forgero.blocks.common.screen.ComponentSlot;
import com.sigmundgranaas.forgero.blocks.common.screen.DisplaySlot;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Client-side screen for the Upgrade Station.
 * <p>
 * Renders the upgrade station UI with:
 * <ul>
 *   <li>Background texture</li>
 *   <li>Dynamic slot positions based on component tree</li>
 *   <li>Connection lines between parent and child slots</li>
 *   <li>Different slot textures for mutable vs immutable slots</li>
 *   <li>Slot type tooltips with cycling item previews</li>
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
	private static final int LINE_COLOR = 0x80FFFFFF;  // Semi-transparent white

	/**
	 * Thickness of connection lines.
	 */
	private static final int LINE_THICKNESS = 2;

	/**
	 * Tick counter for cycling tooltip items.
	 */
	private int tickCounter = 0;

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
		int guiX = (width - backgroundWidth) / 2;
		int guiY = (height - backgroundHeight) / 2;

		// Draw main background
		context.drawTexture(TEXTURE, guiX, guiY, 0, 0, backgroundWidth, backgroundHeight);

		// Only draw slot UI if there's a component in the composite slot
		if (handler.getCurrentComponent() != null) {
			// Draw relationship lines first (behind slots)
			drawSlotConnections(context, guiX, guiY);

			// Draw dynamic slot backgrounds
			drawDynamicSlotBackgrounds(context, guiX, guiY);
		}
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		renderBackground(context);
		super.render(context, mouseX, mouseY, delta);
		drawMouseoverTooltip(context, mouseX, mouseY);

		// Increment tick counter for cycling items
		tickCounter++;

		// Draw custom tooltips for empty upgrade slots
		if (handler.getCurrentComponent() != null) {
			renderCustomTooltips(context, mouseX, mouseY);
		} else {
			// Draw tooltip for empty composite slot
			renderEmptyCompositeTooltip(context, mouseX, mouseY);
		}
	}

	/**
	 * Draws slot backgrounds for all dynamic slots.
	 * Display slots get a read-only texture, upgrade slots get an editable texture.
	 */
	private void drawDynamicSlotBackgrounds(DrawContext context, int guiX, int guiY) {
		// Draw display slots (structure parts - read-only)
		for (DisplaySlot slot : handler.getSlotPool().getActiveDisplaySlots()) {
			if (slot.isEnabled()) {
				drawSlotTexture(context, guiX, guiY, slot, true);
			}
		}

		// Draw upgrade slots (editable)
		for (ComponentSlot slot : handler.getSlotPool().getActiveSlots()) {
			if (slot.isEnabled()) {
				drawSlotTexture(context, guiX, guiY, slot, false);
			}
		}
	}

	/**
	 * Draws the slot background texture.
	 *
	 * @param context     Draw context
	 * @param guiX        GUI base X
	 * @param guiY        GUI base Y
	 * @param slot        The slot to draw
	 * @param readOnly    Whether this is a read-only display slot
	 */
	private void drawSlotTexture(DrawContext context, int guiX, int guiY, Slot slot, boolean readOnly) {
		int slotX = guiX + slot.x - 1;
		int slotY = guiY + slot.y - 1;

		// Texture atlas coordinates for slot backgrounds
		// Read-only slots: lighter/grayed appearance
		// Editable slots: normal slot appearance
		int textureX = readOnly ? 212 : 194;  // Different X offset for read-only
		int textureY = 55;

		context.drawTexture(TEXTURE, slotX, slotY, textureX, textureY, 18, 18);
	}

	/**
	 * Draws lines connecting parent slots to child slots.
	 * Uses a vertical-horizontal-vertical pattern for clean hierarchy visualization.
	 */
	private void drawSlotConnections(DrawContext context, int guiX, int guiY) {
		// Draw lines for display slots
		for (DisplaySlot slot : handler.getSlotPool().getActiveDisplaySlots()) {
			if (slot.isEnabled() && slot.getParentSlot() != null) {
				drawConnectionLine(context, guiX, guiY, slot.getParentSlot(), slot);
			}
		}

		// Draw lines for upgrade slots
		for (ComponentSlot slot : handler.getSlotPool().getActiveSlots()) {
			if (slot.isEnabled() && slot.getParentSlot() != null) {
				drawConnectionLine(context, guiX, guiY, slot.getParentSlot(), slot);
			}
		}
	}

	/**
	 * Draws a connection line from parent to child slot.
	 * Uses vertical-horizontal-vertical pattern:
	 * 1. Vertical from parent center to halfway point
	 * 2. Horizontal from halfway point to child X
	 * 3. Vertical from halfway point to child center
	 */
	private void drawConnectionLine(DrawContext context, int guiX, int guiY, Slot parent, Slot child) {
		// Calculate parent center
		int parentCenterX = guiX + parent.x + 8;
		int parentCenterY = guiY + parent.y + 8;

		// Calculate child center
		int childCenterX;
		int childCenterY;

		if (child instanceof ComponentSlot componentSlot) {
			childCenterX = guiX + componentSlot.getDynamicX() + 8;
			childCenterY = guiY + componentSlot.getDynamicY() + 8;
		} else if (child instanceof DisplaySlot displaySlot) {
			childCenterX = guiX + displaySlot.getDynamicX() + 8;
			childCenterY = guiY + displaySlot.getDynamicY() + 8;
		} else {
			childCenterX = guiX + child.x + 8;
			childCenterY = guiY + child.y + 8;
		}

		// Calculate halfway point
		int halfwayY = parentCenterY + (childCenterY - parentCenterY) / 2;

		// Draw vertical line from parent to halfway
		drawVerticalLine(context, parentCenterX, parentCenterY + 8, halfwayY);

		// Draw horizontal line from parent X to child X at halfway
		drawHorizontalLine(context, parentCenterX, childCenterX, halfwayY);

		// Draw vertical line from halfway to child
		drawVerticalLine(context, childCenterX, halfwayY, childCenterY - 8);
	}

	/**
	 * Draws a vertical line.
	 */
	private void drawVerticalLine(DrawContext context, int x, int startY, int endY) {
		int minY = Math.min(startY, endY);
		int maxY = Math.max(startY, endY);

		for (int t = 0; t < LINE_THICKNESS; t++) {
			context.fill(x + t, minY, x + t + 1, maxY, LINE_COLOR);
		}
	}

	/**
	 * Draws a horizontal line.
	 */
	private void drawHorizontalLine(DrawContext context, int startX, int endX, int y) {
		int minX = Math.min(startX, endX);
		int maxX = Math.max(startX, endX);

		for (int t = 0; t < LINE_THICKNESS; t++) {
			context.fill(minX, y + t, maxX, y + t + 1, LINE_COLOR);
		}
	}

	/**
	 * Renders custom tooltips for empty upgrade slots showing what items can be placed.
	 */
	private void renderCustomTooltips(DrawContext context, int mouseX, int mouseY) {
		int guiX = (width - backgroundWidth) / 2;
		int guiY = (height - backgroundHeight) / 2;

		// Check upgrade slots
		for (ComponentSlot slot : handler.getSlotPool().getActiveSlots()) {
			if (!slot.isEnabled()) continue;

			int slotX = slot.getDynamicX();
			int slotY = slot.getDynamicY();

			if (isPointWithinBounds(slotX, slotY, 16, 16, mouseX - guiX, mouseY - guiY) && !slot.hasStack()) {
				List<Text> tooltip = new ArrayList<>();
				tooltip.add(Text.literal(slot.getAcceptsDescription()));

				// Render cycling item preview
				if (slot.getSlotType() != null) {
					renderCyclingItem(context, slot.getSlotType(), mouseX, mouseY);
				}

				context.drawTooltip(this.textRenderer, tooltip, mouseX, mouseY);
				break;
			}
		}
	}

	/**
	 * Renders a cycling item from the slot's accepted type tag.
	 */
	private void renderCyclingItem(DrawContext context, com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier slotType, int mouseX, int mouseY) {
		try {
			Identifier tagId = new Identifier(slotType.namespace(), slotType.path().toLowerCase(Locale.ENGLISH));
			renderCyclingItemFromTag(context, tagId, mouseX, mouseY);
		} catch (Exception ignored) {
			// Tag doesn't exist or other error - skip rendering
		}
	}

	/**
	 * Renders a cycling item from an item tag.
	 */
	private void renderCyclingItemFromTag(DrawContext context, Identifier tagId, int mouseX, int mouseY) {
		try {
			TagKey<Item> key = TagKey.of(Registries.ITEM.getKey(), tagId);

			List<RegistryEntry<Item>> entries = new ArrayList<>();
			Registries.ITEM.iterateEntries(key).forEach(entries::add);

			if (!entries.isEmpty()) {
				List<Item> acceptedItems = entries.stream().map(RegistryEntry::value).toList();
				Item itemToShow = acceptedItems.get((tickCounter / 40) % acceptedItems.size());
				ItemStack stack = itemToShow.getDefaultStack();

				// Draw item slightly offset from cursor
				context.drawItem(stack, mouseX + 16, mouseY);
			}
		} catch (Exception ignored) {
			// Tag doesn't exist or other error - skip rendering
		}
	}

	/**
	 * Renders tooltip for empty composite slot.
	 */
	private void renderEmptyCompositeTooltip(DrawContext context, int mouseX, int mouseY) {
		int guiX = (width - backgroundWidth) / 2;
		int guiY = (height - backgroundHeight) / 2;

		Slot compositeSlot = handler.getCompositeSlot();
		int slotX = compositeSlot.x;
		int slotY = compositeSlot.y;

		if (isPointWithinBounds(slotX, slotY, 16, 16, mouseX - guiX, mouseY - guiY) && !compositeSlot.hasStack()) {
			List<Text> tooltip = new ArrayList<>();
			tooltip.add(Text.literal("Place you Forgero tool or weapon here"));

			// Render cycling item preview from forgero:holdable tag
			renderCyclingItemFromTag(context, new Identifier("forgero", "holdable"), mouseX, mouseY);

			context.drawTooltip(this.textRenderer, tooltip, mouseX, mouseY);
		}
	}
}
