package com.sigmundgranaas.forgero.smithing.temperature;

import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class TemperatureHud {
	// Call TemperatureHud.register() from your client initializer.
	private static boolean registered = false;

	public static void register() {
		if (registered) return;
		HudRenderCallback.EVENT.register(TemperatureHud::onHudRender);
		registered = true;
		System.out.println("TemperatureHud registered!"); // Debug
	}

	private static void onHudRender(DrawContext ctx, float tickDelta) {
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
		renderThermometer(ctx, temp, max);
	}

	private static boolean isValidTemperatureItem(ItemStack stack) {
		if (stack.isEmpty()) return false;
		return TemperatureUtils.hasMaxTemperature(stack) && TemperatureUtils.getMaxTemp(stack) > 0;
	}

	private static void renderThermometer(DrawContext ctx, int temp, int max) {
		// Position and size (top-left corner)
		int x = 10;
		int y = 10;
		int w = 12;
		int h = 60;

		// Colors - make more visible
		int frame = 0xFF000000; // solid black border
		int bg = 0x88000000;    // semi-transparent black background
		int fillColor = TemperatureColorProvider.getHeatColor(temp, max);

		float ratio = Math.max(0f, Math.min(1f, temp / (float) max));
		int filled = Math.round(h * ratio);

		System.out.println("Rendering at x=" + x + ", y=" + y + ", ratio=" + ratio + ", filled=" + filled); // Debug

		// Frame (border)
		ctx.fill(x - 2, y - 2, x + w + 2, y + h + 2, frame);
		// Background
		ctx.fill(x, y, x + w, y + h, bg);
		// Fill (bottom-up)
		if (filled > 0) {
			ctx.fill(x, y + (h - filled), x + w, y + h, fillColor);
		}
	}
}
