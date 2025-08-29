package com.sigmundgranaas.forgero.smithing.temperature;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
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
	private static final Identifier HUD_TEXTURE_DEFAULT = new Identifier("forgero", "textures/gui/thermometer_scaled.png");
	private static final Identifier HUD_TEXTURE_MAX = new Identifier("forgero", "textures/gui/thermometer_scaled.png");
	private static final Identifier HUD_TEXTURE_HEATING = new Identifier("forgero", "textures/gui/thermometer_scaled.png");
	private static final Identifier HUD_TEXTURE_COOLING = new Identifier("forgero", "textures/gui/thermometer_scaled.png");

	public static void onHudRender(DrawContext ctx, float tickDelta) {
		// use tickDelta in a no-op to avoid unused parameter warnings
		if (tickDelta != tickDelta) {
			// no-op
		}

		MinecraftClient client = MinecraftClient.getInstance();
		if (client == null || client.player == null || client.world == null) return;

		HitResult hit = client.crosshairTarget;
		if (!(hit instanceof BlockHitResult)) return;
		BlockHitResult bhr = (BlockHitResult) hit;

		BlockPos pos = bhr.getBlockPos();
		BlockState state = client.world.getBlockState(pos);
		boolean coolingBlock = TemperatureUtils.isBlockFilledWaterCauldron(state);
		boolean heatingBlock = TemperatureUtils.isBlockCampfire(state);
		if (!(coolingBlock || heatingBlock)) return;

		ItemStack targetStack = ItemStack.EMPTY;

		if (coolingBlock) {
			Box box = new Box(pos).expand(0.25);
			for (ItemEntity item : client.world.getEntitiesByClass(ItemEntity.class, box, e -> true)) {
				ItemStack stack = item.getStack();
				if (stack != null && !stack.isEmpty() && TemperatureUtils.hasMaxTemperature(stack) && TemperatureUtils.isItemInFilledWaterCauldron(item, client.world)) {
					targetStack = stack;
					break;
				}
			}
		} else {
			BlockEntity be = client.world.getBlockEntity(pos);
			if (be instanceof CampfireBlockEntity) {
				CampfireBlockEntity camp = (CampfireBlockEntity) be;
				for (ItemStack stack : camp.getItemsBeingCooked()) {
					if (stack != null && !stack.isEmpty() && TemperatureUtils.hasMaxTemperature(stack)) {
						targetStack = stack;
						break;
					}
				}
			}
		}

		if (targetStack.isEmpty()) return;

		int temp = TemperatureUtils.getTemperature(targetStack);
		int max = TemperatureUtils.getMaxTemp(targetStack);

		renderThermometer(ctx, temp, max, heatingBlock, coolingBlock);
	}

	private static void renderThermometer(DrawContext ctx, int temp, int max, boolean heatingBlock, boolean coolingBlock) {
		int x = 10;
		int y = 10;
		int textureWidth = 26;
		int textureHeight = 66;
		int barWidth = 18;
		int barHeight = 60;
		int barX = 4;
		int barY = 4;

		int[] boundaries = TemperatureColorProvider.getStageBoundaries(max);

		Identifier texture = HUD_TEXTURE_DEFAULT;
		if (temp >= max) texture = HUD_TEXTURE_MAX;
		else if (heatingBlock) texture = HUD_TEXTURE_HEATING;
		else if (coolingBlock) texture = HUD_TEXTURE_COOLING;

		// Fill the bar using heat colormap; force opaque alpha so low temps are visible
		float ratio = Math.max(0f, Math.min(1f, temp / (float) max));
		int filled = Math.round(barHeight * ratio);
		for (int row = 0; row < filled; row++) {
			int rowY = y + barY + barHeight - filled + row;
			float rowRatio = (float) (filled - row) / barHeight;
			int rowTemp = Math.round(rowRatio * max);
			int raw = TemperatureColorProvider.getHeatColor(rowTemp, max);
			int color = (0xFF << 24) | (raw & 0x00FFFFFF);
			ctx.fill(x + barX, rowY, x + barX + barWidth, rowY + 1, color);
		}

		// Draw overlay
		ctx.drawTexture(texture, x, y, 0, 0, textureWidth, textureHeight, textureWidth, textureHeight);

		// Draw connectors on the right side
		int connectorX = x + barX + barWidth + 1; // right of bar
		int connectorW = 1;
		for (int i = 1; i < boundaries.length - 1; i++) { // skip first connector (index 0)
			int a = boundaries[i];
			int b = boundaries[i + 1];
			float ra = a / (float) max;
			float rb = b / (float) max;
			int yA = y + barY + (barHeight - 1) - (int) Math.floor(barHeight * ra);
			int yB = y + barY + (barHeight - 1) - (int) Math.floor(barHeight * rb);
			int top = Math.min(yA, yB);
			int bottom = Math.max(yA, yB);
			int stageColor = TemperatureColorProvider.getInterpolatedHudColor((a + b) / 2, max);

			// Draw a continuous connector from top to bottom
			if (top < bottom) {
				ctx.fill(connectorX, top, connectorX + connectorW, bottom + 1, stageColor);
			}
		}

		// Draw main boundary ticks on the left
		int mainW = 3;
		int mainH = 2;
		int offset = 2;
		int firstOffset = -2;
		int tickColor = 0xFF2E2D4B; // #2e2d4b
		for (int i = 1; i < boundaries.length - 1; i++) {
			int b = boundaries[i];
			float r = b / (float) max;
			int tickY = y + barY + (barHeight - 1) - (int) Math.floor(barHeight * r);
			int tickX = x + barX + (i == 1 ? firstOffset : offset);
			ctx.fill(tickX, tickY, tickX + mainW, tickY + mainH, tickColor);
		}
	}
}
