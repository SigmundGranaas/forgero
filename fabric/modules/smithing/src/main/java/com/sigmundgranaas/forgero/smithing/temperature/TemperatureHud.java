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
	private static final Identifier ARROW_HEATING = new Identifier("forgero", "textures/gui/arrow_heating.png");
	private static final Identifier ARROW_COOLING = new Identifier("forgero", "textures/gui/arrow_cooling.png");
	private static final Identifier ARROW_MAX = new Identifier("forgero", "textures/gui/arrow_max.png");

	private static final int HUD_X = 10;
	private static final int HUD_Y = 10;
	private static final int TEXTURE_W = 26;
	private static final int TEXTURE_H = 66;
	private static final int BAR_W = 18;
	private static final int BAR_H = 60;
	private static final int BAR_X = 4;
	private static final int BAR_Y = 4;
	private static final int ANIMATION_FRAMES = 4;
	private static final int ANIMATION_INTERVAL = 25;
	private static final int MIN_COOLING_TEMP = 20;

	private static int coolingArrowTick = 0;
	private static int coolingArrowFrame = 0;
	private static int heatingArrowTick = 0;
	private static int heatingArrowFrame = 0;

	public static void onHudRender(DrawContext ctx, float tickDelta) {
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
		var world = client.world;

		if (coolingBlock) {
			Box box = new Box(pos).expand(0.25);
			for (ItemEntity item : world.getEntitiesByClass(ItemEntity.class, box, e -> true)) {
				ItemStack stack = item.getStack();
				if (!stack.isEmpty() && TemperatureUtils.hasMaxTemperature(stack) && TemperatureUtils.isItemInFilledWaterCauldron(item, world)) {
					targetStack = stack;
					break;
				}
			}
		} else {
			BlockEntity be = world.getBlockEntity(pos);
			if (be instanceof CampfireBlockEntity camp) {
				for (ItemStack stack : camp.getItemsBeingCooked()) {
					if (!stack.isEmpty() && TemperatureUtils.hasMaxTemperature(stack)) {
						targetStack = stack;
						break;
					}
				}
			}
		}

		if (targetStack.isEmpty()) return;

		int temp = TemperatureUtils.getTemperature(targetStack);
		int max = TemperatureUtils.getMaxTemp(targetStack);
		int safeMax = Math.max(1, max);

		boolean isCooling = coolingBlock && temp > MIN_COOLING_TEMP;
		boolean isHeating = heatingBlock && temp < max;

		updateArrowAnimation();

		renderThermometer(ctx, temp, safeMax, isHeating, isCooling, max);
	}

	private static void updateArrowAnimation() {
		coolingArrowTick++;
		if (coolingArrowTick >= ANIMATION_INTERVAL) {
			coolingArrowFrame = (coolingArrowFrame + 1) % ANIMATION_FRAMES;
			coolingArrowTick = 0;
		}
		heatingArrowTick++;
		if (heatingArrowTick >= ANIMATION_INTERVAL) {
			heatingArrowFrame = (heatingArrowFrame + 1) % ANIMATION_FRAMES;
			heatingArrowTick = 0;
		}
	}

	private static void renderThermometer(DrawContext ctx, int temp, int max, boolean heatingBlock, boolean coolingBlock, int originalMax) {
		int x = HUD_X, y = HUD_Y, textureWidth = TEXTURE_W, textureHeight = TEXTURE_H, barWidth = BAR_W, barHeight = BAR_H, barX = BAR_X, barY = BAR_Y;
		int[] boundaries = TemperatureColorProvider.getStageBoundaries(originalMax);

		Identifier texture = HUD_TEXTURE_DEFAULT;
		Identifier arrowTexture = null;
		if (temp >= originalMax) arrowTexture = ARROW_MAX;
		else if (heatingBlock) arrowTexture = ARROW_HEATING;
		else if (coolingBlock) arrowTexture = ARROW_COOLING;

		float ratio = Math.max(0f, Math.min(1f, temp / (float) max));
		int filled = Math.round(barHeight * ratio);

		for (int row = 0; row < filled; row++) {
			int rowY = y + barY + barHeight - filled + row;
			float rowRatio = (float) (filled - row) / barHeight;
			int rowTemp = Math.round(rowRatio * originalMax);
			int raw = TemperatureColorProvider.getHeatColor(rowTemp, originalMax);
			int color = (0xFF << 24) | (raw & 0x00FFFFFF);
			ctx.fill(x + barX, rowY, x + barX + barWidth, rowY + 1, color);
		}

		ctx.drawTexture(texture, x, y, 0, 0, textureWidth, textureHeight, textureWidth, textureHeight);

		if (arrowTexture != null) {
			int arrowY = y;
			if (arrowTexture == ARROW_COOLING) arrowY += coolingArrowFrame;
			else if (arrowTexture == ARROW_HEATING) arrowY -= heatingArrowFrame;
			ctx.drawTexture(arrowTexture, x, arrowY, 0, 0, 60, 66, 60, 66);
		}

		int connectorW = 1, offset = 2, firstOffset = 2;
		int specialOffset1 = -2, specialOffset2 = 0, specialOffset3 = -2;

		{
			int a = boundaries[0], b = boundaries[1];
			float ra = a / (float) originalMax, rb = b / (float) originalMax;
			int yA = y + barY + (barHeight - 1) - (int) Math.floor(barHeight * ra);
			int yB = y + barY + (barHeight - 1) - (int) Math.floor(barHeight * rb);
			int top = Math.min(yA, yB), bottom = Math.max(yA, yB);
			int stage1Top = top, stage1Bottom = Math.min(top + 7, bottom + 1);
			int stage1Color = TemperatureColorProvider.getHudColorForTemperature((a + b) / 2, originalMax, boundaries, 0, 1, 2, 3, 4, 5, 0);
			ctx.fill(x + barX + specialOffset1, stage1Top, x + barX + specialOffset1 + connectorW, stage1Bottom, stage1Color);
		}

		for (int i = 1; i < boundaries.length - 1; i++) {
			int a = boundaries[i], b = boundaries[i + 1];
			float ra = a / (float) originalMax, rb = b / (float) originalMax;
			int yA = y + barY + (barHeight - 1) - (int) Math.floor(barHeight * ra);
			int yB = y + barY + (barHeight - 1) - (int) Math.floor(barHeight * rb);
			int top = Math.min(yA, yB), bottom = Math.max(yA, yB);

			if (i == boundaries.length - 2) top += 1;

			int connectorX = x + barX + (i == 1 ? firstOffset : offset);
			int stageColor = TemperatureColorProvider.getHudColorForTemperature((a + b) / 2, originalMax, boundaries, 0, 1, 2, 3, 4, 5, i);

			if (top < bottom) {
				if (i == 1) {
					int limitedBottom = Math.min(top + 10, bottom);
					ctx.fill(connectorX, top, connectorX + connectorW, limitedBottom + 1, stageColor);

					int specialTop2 = limitedBottom + 1;
					int specialBottom2 = Math.min(specialTop2 + 2, bottom + 1);
					int specialColor2 = stageColor;
					ctx.fill(x + barX + specialOffset2, specialTop2, x + barX + specialOffset2 + connectorW, specialBottom2, specialColor2);

					int specialTop3 = specialBottom2;
					int specialBottom3 = Math.min(specialTop3 + 3, bottom + 1);
					int specialColor3 = stageColor;
					ctx.fill(x + barX + specialOffset3, specialTop3, x + barX + specialOffset3 + connectorW, specialBottom3, specialColor3);
				} else {
					ctx.fill(connectorX, top, connectorX + connectorW, bottom + 1, stageColor);
				}
			}
		}

		int mainW = 3, mainH = 2, offset2 = 2, firstOffset2 = -2, tickColor = 0xFF2E2D4B;
		for (int i = 1; i < boundaries.length - 1; i++) {
			int b = boundaries[i];
			float r = b / (float) originalMax;
			int tickY = y + barY + (barHeight - 1) - (int) Math.floor(barHeight * r);
			int tickX = x + barX + (i == 1 ? firstOffset2 : offset2);
			ctx.fill(tickX, tickY, tickX + mainW, tickY + mainH, tickColor);
		}
	}
}
