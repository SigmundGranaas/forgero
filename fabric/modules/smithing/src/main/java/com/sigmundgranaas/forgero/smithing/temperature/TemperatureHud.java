package com.sigmundgranaas.forgero.smithing.temperature;

import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class TemperatureHud {
	// Call TemperatureHud.register() from your client initializer.
	private static boolean registered = false;
	private static final Identifier HUD_TEXTURE_DEFAULT = new Identifier("forgero", "textures/gui/thermometer_scaled.png");
	private static final Identifier HUD_TEXTURE_MAX = new Identifier("forgero", "textures/gui/thermometer_scaled.png");
	private static final Identifier HUD_TEXTURE_HEATING = new Identifier("forgero", "textures/gui/thermometer_scaled.png");
	private static final Identifier HUD_TEXTURE_COOLING = new Identifier("forgero", "textures/gui/thermometer_scaled.png");

	public static void onHudRender(DrawContext ctx, float tickDelta) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client == null || client.player == null || client.world == null) return;

		HitResult hit = client.crosshairTarget;
		if (!(hit instanceof BlockHitResult bhr)) return;

		BlockPos pos = bhr.getBlockPos();
		var state = client.world.getBlockState(pos);
		boolean coolingBlock = TemperatureUtils.isBlockFilledWaterCauldron(state);
		boolean heatingBlock = TemperatureUtils.isBlockCampfire(state);
		if (!(coolingBlock || heatingBlock)) return;

		ItemStack targetStack = ItemStack.EMPTY;

		if (coolingBlock) {
			// For cauldrons, search for ItemEntity objects as before
			Box box = new Box(pos).expand(0.25);
			var allItems = client.world.getEntitiesByClass(ItemEntity.class, box, e -> true);
			System.out.println("Found " + allItems.size() + " total items in cauldron box");

			for (ItemEntity item : allItems) {
				ItemStack stack = item.getStack();
				if (isValidTemperatureItem(stack) && TemperatureUtils.isItemInFilledWaterCauldron(item, client.world)) {
					targetStack = stack;
					break;
				}
			}
		} else if (heatingBlock) {
			// For campfires, check the block entity inventory
			var blockEntity = client.world.getBlockEntity(pos);
			if (blockEntity instanceof CampfireBlockEntity campfire) {
				System.out.println("Found campfire block entity");
				for (ItemStack stack : campfire.getItemsBeingCooked()) {
					if (isValidTemperatureItem(stack)) {
						targetStack = stack;
						break;
					}
				}
			} else {
				System.out.println("Block entity is not a campfire: " + blockEntity);
			}
		}

		if (targetStack.isEmpty()) {
			return;
		}
		int temp = TemperatureUtils.getTemperature(targetStack);
		int max = TemperatureUtils.getMaxTemp(targetStack);

		// Always render thermometer if item is present
		renderThermometer(ctx, temp, max, heatingBlock, coolingBlock);
	}

	private static boolean isValidTemperatureItem(ItemStack stack) {
		if (stack.isEmpty()) return false;
		return TemperatureUtils.hasMaxTemperature(stack) && TemperatureUtils.getMaxTemp(stack) > 0;
	}

	private static void renderThermometer(DrawContext ctx, int temp, int max, boolean heatingBlock, boolean coolingBlock) {
		int x = 10;
		int y = 10;
		int textureWidth = 26;
		int textureHeight = 66;
		// Bar region: 18x60, centered in thermometer region (26x66)
		int barWidth = 18;
		int barHeight = 60;
		int barX = 4; // (26 - 18) / 2
		int barY = 4; // (66 - 60) / 2 + 1 pixel adjustment for alignment

		// Get stage boundaries from TemperatureColorProvider
		int[] boundaries = TemperatureColorProvider.getStageBoundaries(max);

		// Stage colors (copied from MinigameHudOverlay)
		int[] stageColors = {
			0xFF000099, // Cold: dark blue
			0xFF3399FF, // Warm: dark cyan
			0xFFCCCC00, // Hot: dark yellow
			0xFF00CC00, // Very Hot: dark green
			0xFFCC6600, // Near Melt: dark orange
			0xFFCC0000  // Molten: dark red
		};

		// Select texture based on state
		Identifier texture = HUD_TEXTURE_DEFAULT;
		if (temp >= max) {
			texture = HUD_TEXTURE_MAX;
		} else if (heatingBlock) {
			texture = HUD_TEXTURE_HEATING;
		} else if (coolingBlock) {
			texture = HUD_TEXTURE_COOLING;
		}

		// Calculate fill amount
		float ratio = Math.max(0f, Math.min(1f, temp / (float) max));
		int filled = Math.round(barHeight * ratio);
		if (filled > 0) {
			for (int i = 0; i < filled; i++) {
				int rowY = y + barY + barHeight - filled + i;
				float rowRatio = (float)(filled - i) / barHeight;
				int rowTemp = Math.round(rowRatio * max);
				int stageIdx = 0;
				for (int b = 0; b < boundaries.length - 1; b++) {
					if ((b == boundaries.length - 2 && rowTemp >= boundaries[b] && rowTemp <= boundaries[b + 1]) ||
						(rowTemp >= boundaries[b] && rowTemp < boundaries[b + 1])) {
						stageIdx = b;
						break;
					}
				}
				int fillColor = stageColors[Math.min(stageIdx, stageColors.length - 1)];
				ctx.fill(x + barX, rowY, x + barX + barWidth, rowY + 1, fillColor);
			}
		}
		// Draw the full HUD texture on top of the bar
		ctx.drawTexture(texture, x, y, 0, 0, textureWidth, textureHeight, textureWidth, textureHeight);

		// Draw main ticks for each stage boundary ON TOP of everything
		int tickHeight = 2; // Height of each tick (2 pixels thick)
		int tickWidth = 3; // Quarter length
		int tickOffset = 2; // Default offset to the left of the thermometer
		int firstTickOffset = -2; // Custom offset for the first stage tick
		for (int i = 1; i < boundaries.length - 1; i++) { // Skip first and last
			int boundary = boundaries[i];
			float stageRatio = boundary / (float) max;
			int tickY = y + barY + (barHeight - 1) - (int)Math.floor(barHeight * stageRatio);
			int tickX = x + barX + (i == 1 ? firstTickOffset : tickOffset); // Use custom offset for first tick
			int tickColor = 0xFF2E2D4B; // #2e2d4b, ARGB format
			ctx.fill(tickX, tickY, tickX + tickWidth, tickY + tickHeight, tickColor);
		}
	}
}
