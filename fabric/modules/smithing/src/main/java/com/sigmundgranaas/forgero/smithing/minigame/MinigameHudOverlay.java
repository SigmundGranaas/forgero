package com.sigmundgranaas.forgero.smithing.minigame;

import com.sigmundgranaas.forgero.smithing.block.entity.custom.SmithingAnvilBlockEntity;
import com.sigmundgranaas.forgero.smithing.item.custom.MorphedItem;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureProfile;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureProfile.TemperatureBand;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureRules;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureRules.TemperatureStage;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureRules.TemperatureStages;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureState;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;


public class MinigameHudOverlay implements HudRenderCallback {

	private static final Identifier BAR_TEXTURE = new Identifier("forgero", "textures/gui/bar_texture_new.png");
	private static final Identifier THERMOMETER_ARROW = new Identifier("forgero", "textures/gui/thermometer_arrow.png");

	@Override
	public void onHudRender(DrawContext ctx, float tickDelta) {
		var mc = MinecraftClient.getInstance();
		if (mc == null || mc.player == null || mc.world == null || mc.options.hudHidden) return;

		SmithingAnvilBlockEntity be = findNearestActiveAnvil(mc);
		if (be == null) return;

		ItemStack stack = be.getInventory().getStack(0);
		if (stack.isEmpty() || !(stack.getItem() instanceof MorphedItem)) return;
		if (MorphedItem.isRuined(stack)) return;
		double progress = MorphedItem.getMorphProgress(stack);
		if (MorphedItem.needsQuench(stack) || progress >= 1.0) {
			if (hasStrikeFeedback(be)) {
				drawStrikeFeedback(ctx, mc, be, ctx.getScaledWindowWidth() / 2, 42);
			}
			return;
		}

		int temp = TemperatureState.currentTemperature(stack);
		TemperatureProfile profile = TemperatureProfile.from(stack);
		TemperatureStages stages = TemperatureRules.stages(profile);
		int minWindow = hudWindowStart(stages);
		int maxWindow = hudWindowEnd(stages);

		if (maxWindow <= minWindow) return;

		int screenW = ctx.getScaledWindowWidth();
		int barWidth = 165;
		int barHeight = 29;
		int barLeft = (screenW - barWidth) / 2;
		int barTop = 10;

		int innerWidth = 140;
		int innerHeight = 7;
		int innerLeft = barLeft + 8;
		int innerTop = barTop + 8;

		float unitsPerPixelX = (float) (maxWindow - minWindow) / (float) (innerWidth);

		int[] stageColors = new int[]{
			TemperatureRules.hudColor(TemperatureStage.COLD),
			TemperatureRules.hudColor(TemperatureStage.WARM),
			TemperatureRules.hudColor(TemperatureStage.HOT),
			TemperatureRules.hudColor(TemperatureStage.WORKABLE),
			TemperatureRules.hudColor(TemperatureStage.OVERHEATED)
		};

		for (TemperatureBand band : stages.bands()) {
			drawTemperatureBand(ctx, band, minWindow, maxWindow, unitsPerPixelX, innerLeft, innerTop, innerWidth, innerHeight);
		}

		int progressBarWidth = 134;
		int progressBarHeight = 3;
		int progressBarLeft = barLeft + 11;
		int progressBarTop = barTop + 20;

		fill(ctx, progressBarLeft, progressBarTop, progressBarLeft + progressBarWidth, progressBarTop + progressBarHeight, 0xFF000000);

		int totalSegments = MinigameLogic.getRequiredHits(stack);
		int hits = Math.min(be.getMinigameLogic().getMarkerHitsCount(), totalSegments);
		float segWidth = progressBarWidth / (float) totalSegments;
		var hitStageIndices = be.getMinigameLogic().getHitStageIndices();

		for (int i = 0; i < hits; i++) {
			int startX = progressBarLeft + Math.round(i * segWidth);
			int endX = progressBarLeft + Math.round((i + 1) * segWidth);

			int stageIdx = 0;
			if (i < hitStageIndices.size()) {
				stageIdx = hitStageIndices.get(i);
			}

			int color = stageColors[clamp(stageIdx, 0, stageColors.length - 1)];
			fill(ctx, startX, progressBarTop, endX, progressBarTop + progressBarHeight, color);
		}

		int numSegments = totalSegments;
		for (int i = 1; i < numSegments; i++) {
			int tickX = progressBarLeft + (int) Math.round(i * (progressBarWidth / (float) numSegments));
			fill(ctx, tickX, progressBarTop, tickX + 1, progressBarTop + progressBarHeight, 0xFFad9474);
		}

		ctx.drawTexture(BAR_TEXTURE, barLeft, barTop, 0, 0, barWidth, barHeight, barWidth, barHeight);

		for (int boundary : stages.boundaries()) {
			if (boundary < minWindow || boundary > maxWindow) continue;
			int x = valueToX(boundary, minWindow, unitsPerPixelX, innerLeft, innerWidth);
			int rightBorder = innerLeft + innerWidth;
			if (x >= rightBorder) {
				x = rightBorder - 1;
			}
			int yStart = innerTop + 1;
			int yEnd = innerTop + innerHeight - 1;
			fill(ctx, x, yStart, x + 1, yEnd, 0xFF2e2728);
		}

		int tempArrowX = valueToX(temp, minWindow, unitsPerPixelX, innerLeft, innerWidth);
		int leftEdge = innerLeft;
		int rightEdge = innerLeft + innerWidth - 1;
		if (temp < minWindow) {
			tempArrowX = leftEdge;
		} else if (temp > maxWindow) {
			tempArrowX = rightEdge;
		}
		int arrowBottomY = innerTop + innerHeight + 1;
		drawDownArrow(ctx, tempArrowX, arrowBottomY, 0xFFFFFFFF);
		drawStrikeFeedback(ctx, mc, be, screenW / 2, barTop + barHeight + 3);

		final int missBaseX = barLeft + 155;
		final int missBaseY = barTop + 5;
		final int boxStep = 4;
		final int maxBoxes = 5;
		int misses = be.getMinigameLogic().getMissMarkerHits();
		int toRender = Math.min(Math.max(0, misses), maxBoxes);
		final int crossColor = 0xFFFF0000;
		for (int i = 0; i < toRender; i++) {
			int x = missBaseX;
			int y = missBaseY + i * boxStep;
			fill(ctx, x, y, x + 3, y + 3, 0xFF000000);
			fill(ctx, x, y, x + 1, y + 1, crossColor);
			fill(ctx, x + 1, y + 1, x + 2, y + 2, crossColor);
			fill(ctx, x + 2, y + 2, x + 3, y + 3, crossColor);
			fill(ctx, x + 2, y, x + 3, y + 1, crossColor);
			fill(ctx, x + 1, y + 1, x + 2, y + 2, crossColor);
			fill(ctx, x, y + 2, x + 1, y + 3, crossColor);
		}
	}

	private void drawStrikeFeedback(
			DrawContext ctx,
			MinecraftClient mc,
			SmithingAnvilBlockEntity be,
			int centerX,
			int y
	) {
		MinigameLogic.StrikeFeedback feedback = be.getStrikeFeedbackQuality();

		if (!hasStrikeFeedback(be)) {
			return;
		}

		ctx.drawCenteredTextWithShadow(
				mc.textRenderer,
				strikeFeedbackText(feedback),
				centerX,
				y,
				strikeFeedbackColor(feedback)
		);
	}

	private Text strikeFeedbackText(MinigameLogic.StrikeFeedback feedback) {
		return switch (feedback) {
			case MISS -> Text.translatable("message.forgero.smithing.strike.miss");
			case PERFECT -> Text.translatable("message.forgero.smithing.strike.clean");
			case GOOD -> Text.translatable("message.forgero.smithing.strike.solid");
			case POOR -> Text.translatable("message.forgero.smithing.strike.glancing");
		};
	}

	private int strikeFeedbackColor(MinigameLogic.StrikeFeedback feedback) {
		return switch (feedback) {
			case MISS -> 0xFFFF5555;
			case PERFECT -> 0xFF55FF55;
			case GOOD -> 0xFFFFFFFF;
			case POOR -> 0xFFAAAAAA;
		};
	}

	private boolean hasStrikeFeedback(SmithingAnvilBlockEntity be) {
		return be.getStrikeFeedbackQuality() != null && be.getStrikeFeedbackTicks() > 0;
	}

    private SmithingAnvilBlockEntity findNearestActiveAnvil(MinecraftClient mc) {
        HitResult hit = mc.crosshairTarget;
        if (hit instanceof BlockHitResult bhr && hit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = bhr.getBlockPos();
            var be = mc.world.getBlockEntity(pos);
            if (be instanceof SmithingAnvilBlockEntity sa && isActiveMinigame(sa)) {
                return sa;
            }
        }

        Vec3d p = mc.player.getPos();
        int radius = 5;
        int yRadius = 2;
        SmithingAnvilBlockEntity nearest = null;
        double bestDistSq = Double.MAX_VALUE;

        BlockPos playerPos = BlockPos.ofFloored(p);
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -yRadius; dy <= yRadius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = playerPos.add(dx, dy, dz);
                    var be = mc.world.getBlockEntity(pos);
                    if (be instanceof SmithingAnvilBlockEntity sa && isActiveMinigame(sa)) {
                        double distSq = pos.toCenterPos().squaredDistanceTo(p);
                        if (distSq < bestDistSq) {
                            bestDistSq = distSq;
                            nearest = sa;
                        }
                    }
                }
            }
        }
        return nearest;
    }

    private boolean isActiveMinigame(SmithingAnvilBlockEntity be) {
        ItemStack stack = be.getInventory().getStack(0);
        if (stack.isEmpty() || !(stack.getItem() instanceof MorphedItem)) return false;
        if (MorphedItem.isRuined(stack)) return false;
        return (MorphedItem.getMorphProgress(stack) < 1.0 && !MorphedItem.needsQuench(stack))
				|| hasStrikeFeedback(be);
    }

	private int clamp(int v, int lo, int hi) {
		if (v < lo) return lo;
		if (v > hi) return hi;
		return v;
	}

	private int hudWindowStart(TemperatureStages stages) {
		if (!stages.hasWorkableRange()) {
			return TemperatureState.MIN_TEMPERATURE;
		}

		return Math.max(TemperatureState.MIN_TEMPERATURE, stages.workableStart - hudWindowMargin(stages));
	}

	private int hudWindowEnd(TemperatureStages stages) {
		return Math.max(stages.max, hudWindowStart(stages) + 1);
	}

	private int hudWindowMargin(TemperatureStages stages) {
		int workableWidth = Math.max(1, stages.workableEnd - stages.workableStart);
		return Math.max(80, workableWidth / 2);
	}

	private void drawTemperatureBand(
			DrawContext ctx,
			TemperatureBand band,
			int minWindow,
			int maxWindow,
			float unitsPerPixelX,
			int innerLeft,
			int innerTop,
			int innerWidth,
			int innerHeight
	) {
		if (band.end() < minWindow || band.start() > maxWindow) {
			return;
		}

		int startX = valueToX(Math.max(band.start(), minWindow), minWindow, unitsPerPixelX, innerLeft, innerWidth);
		int endX = valueToX(Math.min(band.end(), maxWindow), minWindow, unitsPerPixelX, innerLeft, innerWidth) + 1;
		endX = Math.min(innerLeft + innerWidth, Math.max(startX + 1, endX));
		fill(ctx, startX, innerTop, endX, innerTop + innerHeight, TemperatureRules.hudColor(band.stage()));
	}

	private int valueToX(int value, int minWindow, float unitsPerPixelX, int barLeft, int barWidth) {
		float exactPixelOffset = (value - minWindow) / unitsPerPixelX;
		int x = barLeft + Math.round(exactPixelOffset);
		if (x < barLeft) x = barLeft;
		if (x > barLeft + barWidth - 1) x = barLeft + barWidth - 1;
		return x;
	}

	private void drawDownArrow(DrawContext ctx, int centerX, int bottomY, int color) {
		int arrowWidth = 16;
		int arrowHeight = 16;
		int x = centerX - arrowWidth / 2;
		int y = bottomY - arrowHeight + 2;
		ctx.drawTexture(THERMOMETER_ARROW, x, y, 0, 0, arrowWidth, arrowHeight, arrowWidth, arrowHeight);
	}

	private void fill(DrawContext ctx, int x1, int y1, int x2, int y2, int argb) {
		ctx.fill(x1, y1, x2, y2, argb);
	}

}
