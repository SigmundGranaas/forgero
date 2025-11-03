package com.sigmundgranaas.forgero.smithing.minigame;

import java.util.Arrays;

import com.sigmundgranaas.forgero.smithing.block.entity.custom.SmithingAnvilBlockEntity;
import com.sigmundgranaas.forgero.smithing.item.custom.MorphedItem;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils;
import com.sigmundgranaas.forgero.smithing.temperature.DynamicTemperatureSystem;
import com.sigmundgranaas.forgero.smithing.temperature.DynamicTemperatureSystem.TemperatureStage;
import com.sigmundgranaas.forgero.smithing.temperature.DynamicTemperatureSystem.TemperatureStages;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

// TODO create our own texture for the outside of the bar. Including arrow and ticks.
// TODO Bar animating / Instead of item different bars based on the temperature stage.


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
		double progress = MorphedItem.getMorphProgress(stack);
		if (progress >= 1.0) return;

		int temp = TemperatureUtils.getTemperature(stack);
		int max = Math.max(TemperatureUtils.getMaxTemp(stack), 1);
		int effectiveMax = Math.min(max, 10000);

		// Calculate stages using dynamic system
		TemperatureStages stages = DynamicTemperatureSystem.calculateStages(stack);

		// Find which stage the current temperature is in
		TemperatureStage currentStage = DynamicTemperatureSystem.getStage(temp, stages);

		// Build stage boundaries for snapping
		int[] stageBoundaries = {0, stages.coldEnd, stages.warmEnd, stages.hotStart, stages.hotEnd, stages.overheatedStart, effectiveMax};

		// Calculate window centered on current temperature with context
		int minWindowWidth = 600;
		int halfWindow = minWindowWidth / 2;
		int minWindow = Math.max(0, temp - halfWindow);
		int maxWindow = Math.min(effectiveMax, minWindow + minWindowWidth);

		// If we hit the upper bound, shift back to keep full window width
		if (maxWindow == effectiveMax && maxWindow - minWindow < minWindowWidth) {
			minWindow = Math.max(0, maxWindow - minWindowWidth);
		}

		// Add small margin to the window (5% of width)
		int margin = Math.max(50, (maxWindow - minWindow) / 20);
		minWindow = Math.max(0, minWindow - margin);
		maxWindow = Math.min(effectiveMax, maxWindow + margin);

		if (maxWindow <= minWindow) return;

		// Stage boundary ticks (defined early for use in progress bar)
		int[] stageBoundariesForTicks = {stages.coldEnd, stages.warmEnd, stages.hotStart, stages.hotEnd, stages.overheatedStart};

		// Horizontal bar placement and dimensions (center top, match texture size)
		int screenW = ctx.getScaledWindowWidth();
		int barWidth = 165;
		int barHeight = 29;
		int barLeft = (screenW - barWidth) / 2;
		int barTop = 10;

		// Inner area for temperature bar
		int innerWidth = 140;
		int innerHeight = 7;
		int innerLeft = barLeft + 8;
		int innerTop = barTop + 8;

		float unitsPerPixelX = (float) (maxWindow - minWindow) / (float) (innerWidth);

		// Stage colors matching HUD colors
		int[] stageColors = new int[]{
			DynamicTemperatureSystem.getHudColor(TemperatureStage.COLD),
			DynamicTemperatureSystem.getHudColor(TemperatureStage.WARM),
			DynamicTemperatureSystem.getHudColor(TemperatureStage.HOT),
			DynamicTemperatureSystem.getHudColor(TemperatureStage.WORKABLE),
			DynamicTemperatureSystem.getHudColor(TemperatureStage.OVERHEATED)
		};

		// Fill the inside of the bar with stage colors
		for (int x = 0; x < innerWidth; x++) {
			int tempValue = Math.round(minWindow + x * unitsPerPixelX);
			TemperatureStage stage = DynamicTemperatureSystem.getStage(tempValue, stages);
			int color = DynamicTemperatureSystem.getHudColor(stage);
			fill(ctx, innerLeft + x, innerTop, innerLeft + x + 1, innerTop + innerHeight, color);
		}

		// --- Progress Bar ---
		int progressBarWidth = 134;
		int progressBarHeight = 3;
		int progressBarLeft = barLeft + 11;
		int progressBarTop = barTop + 20;

		// Draw static black background for the bar
		fill(ctx, progressBarLeft, progressBarTop, progressBarLeft + progressBarWidth, progressBarTop + progressBarHeight, 0xFF000000); // black

		// Draw filled portion for progress as discrete segments, colored by hit stage
		int totalSegments = MinigameLogic.TOTAL_MARKERS;
		int hits = Math.min(be.getMinigameLogic().getMarkerHitsCount(), totalSegments);
		float segWidth = progressBarWidth / (float) totalSegments;
		var hitStageIndices = be.getMinigameLogic().getHitStageIndices();

		for (int i = 0; i < hits; i++) {
			int startX = progressBarLeft + Math.round(i * segWidth);
			int endX = progressBarLeft + Math.round((i + 1) * segWidth);

			// Get stage index for this hit
			int stageIdx = 0;
			if (i < hitStageIndices.size()) {
				stageIdx = hitStageIndices.get(i);
			}

			// Map stage index to color
			int color = stageColors[clamp(stageIdx, 0, stageColors.length - 1)];
			fill(ctx, startX, progressBarTop, endX, progressBarTop + progressBarHeight, color);
		}

		// Draw 10 ticks for each segment
		int numSegments = 10;
		for (int i = 1; i < numSegments; i++) {
			int tickX = progressBarLeft + (int) Math.round(i * (progressBarWidth / (float) numSegments));
			fill(ctx, tickX, progressBarTop, tickX + 1, progressBarTop + progressBarHeight, 0xFFad9474); // same tick color as temp bar
		}
		// No border or sides, as those are included in the PNG

		// Draw the border using the texture (full size) AFTER the progress bar so the PNG overlaps
		ctx.drawTexture(BAR_TEXTURE, barLeft, barTop, 0, 0, barWidth, barHeight, barWidth, barHeight);

		// Stage boundary ticks
		for (int boundary : stageBoundariesForTicks) {
			if (boundary < minWindow || boundary > maxWindow) continue;
			if ((boundary == minWindow) || (boundary == maxWindow)) continue;
			int x = valueToX(boundary, minWindow, unitsPerPixelX, innerLeft, innerWidth);
			int rightBorder = innerLeft + innerWidth;
			if (x >= rightBorder) {
				x = rightBorder - 1;
			}
			int yStart = innerTop;
			int yEnd = innerTop + innerHeight;
			fill(ctx, x, yStart, x + 1, yEnd, 0xFFad9474);
		}

		// Two ticks between each boundary (only for workable range for clarity)
		int workableStart = TemperatureUtils.getWorkableTemperatureStart(stack);
		int workableEnd = TemperatureUtils.getWorkableTemperatureEnd(stack);

		int[] boundaries = stageBoundariesForTicks;
		for (int i = 0; i < boundaries.length - 1; i++) {
			int start = boundaries[i];
			int end = boundaries[i + 1];

			// Only draw subdivision ticks for stages within or near the workable range
			boolean inWorkableContext = !(end < workableStart || start > workableEnd);
			if (!inWorkableContext) continue;

			float interval = (float) (end - start);

			// Midpoint tick (always shown for active stages)
			int midValue = Math.round(start + interval / 2.0f);
			if (midValue > minWindow && midValue < maxWindow) {
				int x = valueToX(midValue, minWindow, unitsPerPixelX, innerLeft, innerWidth);
				int rightBorder = innerLeft + innerWidth;
				if (x >= rightBorder) x = rightBorder - 1;
				int yStart = innerTop + 1;
				int yEnd = innerTop + innerHeight - 1;
				fill(ctx, x, yStart, x + 1, yEnd, 0xFFad9474);
			}

			// Quarter ticks (reduced density - skip for very small stages)
			if (interval > 150) {
				int quarterValue = Math.round(start + interval / 4.0f);
				int threeQuarterValue = Math.round(start + 3.0f * interval / 4.0f);
				for (int tickValue : new int[]{quarterValue, threeQuarterValue}) {
					if (tickValue > minWindow && tickValue < maxWindow) {
						int x = valueToX(tickValue, minWindow, unitsPerPixelX, innerLeft, innerWidth);
						int rightBorder = innerLeft + innerWidth;
						if (x >= rightBorder) x = rightBorder - 1;
						int yStart = innerTop + 2;
						int yEnd = innerTop + innerHeight - 2;
						fill(ctx, x, yStart, x + 1, yEnd, 0xFFad9474);
					}
				}
			}
		}

		// Edge indicators: subtle marks if stages exist outside the window
		if (minWindow > 0) {
			// Left edge: stage exists below minimum
			fill(ctx, innerLeft - 1, innerTop + innerHeight - 2, innerLeft, innerTop + innerHeight, 0xFF666666);
		}
		if (maxWindow < effectiveMax) {
			// Right edge: stage exists above maximum
			fill(ctx, innerLeft + innerWidth, innerTop + innerHeight - 2, innerLeft + innerWidth + 1, innerTop + innerHeight, 0xFF666666);
		}

		// Current temperature arrow (now fixed in the middle)
		int tempX = innerLeft + innerWidth / 2;
		int arrowBottomY = innerTop + innerHeight + 1;
		drawDownArrow(ctx, tempX, arrowBottomY, 0xFFFFFFFF);

		// --- Miss-hit crosses (5 boxes of size 3x3) ---
		// Draw only the red "X" for each miss, in the PNG boxes stacked vertically (3x3, 1px gap)
		final int missBaseX = barLeft + 155;
		final int missBaseY = barTop + 5;
		final int boxStep = 4; // 3px box + 1px gap
		final int maxBoxes = 5;
		int misses = be.getMinigameLogic().getMissMarkerHits();
		int toRender = Math.min(Math.max(0, misses), maxBoxes);
		final int crossColor = 0xFFFF0000; // opaque red
		for (int i = 0; i < toRender; i++) {
			int x = missBaseX;
			int y = missBaseY + i * boxStep;
			// diagonal TL -> BR
			fill(ctx, x, y, x + 1, y + 1, crossColor);
			fill(ctx, x + 1, y + 1, x + 2, y + 2, crossColor);
			fill(ctx, x + 2, y + 2, x + 3, y + 3, crossColor);
			// diagonal TR -> BL
			fill(ctx, x + 2, y, x + 3, y + 1, crossColor);
			fill(ctx, x + 1, y + 1, x + 2, y + 2, crossColor); // center (already drawn, idempotent)
			fill(ctx, x, y + 2, x + 1, y + 3, crossColor);
		}
	}



    private SmithingAnvilBlockEntity findNearestActiveAnvil(MinecraftClient mc) {
        // 1) Prefer the anvil the player is looking at
        HitResult hit = mc.crosshairTarget;
        if (hit instanceof BlockHitResult bhr && hit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = bhr.getBlockPos();
            var be = mc.world.getBlockEntity(pos);
            if (be instanceof SmithingAnvilBlockEntity sa && isActiveMinigame(sa)) {
                return sa;
            }
        }

        // 2) Fallback: search a small radius around the player
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
        return MorphedItem.getMorphProgress(stack) < 1.0;
    }

	private int clamp(int v, int lo, int hi) {
		if (v < lo) return lo;
		if (v > hi) return hi;
		return v;
	}

	// Find which segment a temperature value falls into based on boundaries
	private int findSegmentIndex(int value, int[] boundaries) {
		for (int i = 0; i < boundaries.length - 1; i++) {
			if (value >= boundaries[i] && value < boundaries[i + 1]) {
				return i;
			}
		}
		return Math.max(0, boundaries.length - 2);
	}

	// Convert a value in [minWindow, maxWindow] to a X coordinate along the bar (left-to-right)
	private int valueToX(int value, int minWindow, float unitsPerPixelX, int barLeft, int barWidth) {
		float exactPixelOffset = (value - minWindow) / unitsPerPixelX;
		int x = barLeft + Math.round(exactPixelOffset);
		if (x < barLeft) x = barLeft;
		if (x > barLeft + barWidth - 1) x = barLeft + barWidth - 1;
		return x;
	}

	// Draw a small down-pointing arrow; bottomY is the tip's Y
	private void drawDownArrow(DrawContext ctx, int centerX, int bottomY, int color) {
		// Draw the custom arrow texture centered at (centerX, bottomY)
		int arrowWidth = 16; // Adjust to match your texture size
		int arrowHeight = 16; // Adjust to match your texture size
		int x = centerX - arrowWidth / 2;
		int y = bottomY - arrowHeight + 1;
		ctx.drawTexture(THERMOMETER_ARROW, x, y, 0, 0, arrowWidth, arrowHeight, arrowWidth, arrowHeight);
	}

	private void fill(DrawContext ctx, int x1, int y1, int x2, int y2, int argb) {
		ctx.fill(x1, y1, x2, y2, argb);
	}

    // Correctly map a temperature value to the segment index defined by 'boundaries'
    private int segmentIndex(int value, int[] boundaries) {
        int idx = Arrays.binarySearch(boundaries, value);
        int maxSeg = Math.max(0, boundaries.length - 2);
        if (idx >= 0) {
            // Boundary values belong to the right-hand segment, except the last boundary
            return Math.min(idx, maxSeg);
        }
        int insertionPoint = -(idx + 1);
        int seg = Math.max(0, insertionPoint - 1);
        // Ensure segment index never exceeds maxSeg
        if (seg > maxSeg) seg = maxSeg;
        return seg;
    }
}
