package com.sigmundgranaas.forgero.smithing.minigame;

import java.util.Arrays;

import com.sigmundgranaas.forgero.smithing.block.entity.custom.SmithingAnvilBlockEntity;
import com.sigmundgranaas.forgero.smithing.item.custom.MorphedItem;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureColorProvider;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils;

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

// TODO BLUE, CYAN LIGHT BLUE,
public class MinigameHudOverlay implements HudRenderCallback {

	private static final Identifier THERMOMETER_UP = new Identifier("forgero", "textures/gui/thermometer_up.png");
	private static final Identifier BAR_TEXTURE = new Identifier("forgero", "textures/gui/bar_texture.png");

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

        // Compute a scaled window around current temperature (base: 600 for 1600 max)
        final int BASE_MAX = 1600;
        final int BASE_WINDOW = 700;
        int window = (int)(BASE_WINDOW / (float)BASE_MAX * effectiveMax);
        int half = window / 2;
        int minWindow = Math.max(0, Math.min(temp - half, Math.max(0, effectiveMax - window)));
        int maxWindow = Math.min(effectiveMax, minWindow + window);
        if (maxWindow <= minWindow) return;

        // Horizontal bar placement and dimensions (center top, match texture size)
        int screenW = ctx.getScaledWindowWidth();
        int barWidth = 156; // match texture width
        int barHeight = 26; // match texture height
        int borderSize = 0; // 1px border on all sides
        int barLeft = (screenW - barWidth) / 2;
        int barTop = 10;

        int[] boundaries = TemperatureColorProvider.getStageBoundaries(max);
        // Stage indices based on boundaries array (6 stages)
        int idxCold     = 0;
        int idxWarm     = 1;
        int idxHot      = 2;
        int idxVeryHot  = 3;
        int idxNearMelt = 4;
        int idxMolten   = 5;



        // Only show bar from cold and up
        int minStageBoundary = boundaries[idxCold];
        minWindow = Math.max(minStageBoundary, minWindow);
        if (maxWindow <= minWindow) return;
        float unitsPerPixelX = (float) (maxWindow - minWindow) / (float) (barWidth - 2 * borderSize);

        // Hardcoded stage colors per segment (no blending)
        // Fill the inside of the bar with stage colors
        int[] stageColors = new int[] {
            0xFF000099, // Cold: dark blue
            0xFF3399FF, // Warm: dark cyan
            0xFFCCCC00, // Hot: dark yellow
            0xFF00CC00, // Very Hot: dark green
            0xFFCC6600, // Near Melt: dark orange
            0xFFCC0000  // Molten: dark red
        };
        for (int x = borderSize; x < barWidth - borderSize; x++) {
            int tempValue = Math.round(minWindow + (x - borderSize) * unitsPerPixelX);
            int segIdx = segmentIndex(tempValue, boundaries);
            int color = stageColors[segIdx];
            fill(ctx, barLeft + x, barTop + borderSize, barLeft + x + 1, barTop + barHeight - borderSize, color);
        }

        // Draw the border using the texture (full size)
        ctx.drawTexture(BAR_TEXTURE, barLeft, barTop, 0, 0, barWidth, barHeight, barWidth, barHeight);

        // 1px square border (optional, if you want to keep it)
        int border = 0xFF222222;
        fill(ctx, barLeft, barTop, barLeft + barWidth, barTop + borderSize, border); // top
        fill(ctx, barLeft, barTop + barHeight - borderSize, barLeft + barWidth, barTop + barHeight, border); // bottom
        fill(ctx, barLeft, barTop, barLeft + borderSize, barTop + barHeight, border); // left
        fill(ctx, barLeft + barWidth - borderSize, barTop, barLeft + barWidth, barTop + barHeight, border); // right

        // Stage boundary ticks (straight borders between stages)
        for (int i = 0; i < boundaries.length; i++) {
            int t = boundaries[i];
            if (t < minWindow || t > maxWindow) continue;
            int x = valueToX(t, minWindow, unitsPerPixelX, barLeft + borderSize, barWidth - 2 * borderSize);
            // Clamp x so ticks never overlap the right border
            int rightBorder = barLeft + barWidth - borderSize;
            if (x >= rightBorder) {
                x = rightBorder - 1;
            }
            // Only draw ticks strictly inside the bar, not on the border
            int yStart = barTop + borderSize; // Start below the top pixel
            int yEnd = barTop + barHeight - borderSize;
            fill(ctx, x, yStart, x + 1, yEnd, 0xFFFFFFFF); // fully opaque white

            // Draw temperature value above the tick (larger font, just above the bar)
            String tempText = String.valueOf(t);
            ctx.getMatrices().push();
            float scale = 0.5f; // Boundary tick temperature value: half size
            ctx.getMatrices().translate(x + 1, barTop - 4, 0); // Move text 1px right
            ctx.getMatrices().scale(scale, scale, 1.0f);
            int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(tempText);
            int textX = -textWidth / 2;
            int textY = 0;
            ctx.drawText(MinecraftClient.getInstance().textRenderer, tempText, textX, textY, 0xFFFFFFFF, false);
            ctx.getMatrices().pop();
        }

        // Draw two ticks between each stage: one normal, one shorter
        for (int i = 0; i < boundaries.length - 1; i++) {
            // Skip inbetween ticks for the molten stage (last segment)
            if (i == boundaries.length - 2) continue;
            int start = boundaries[i];
            int end = boundaries[i + 1];
            float interval = (float)(end - start);

            // Midpoint tick (normal small tick)
            int midValue = Math.round(start + interval / 2.0f);
            if (midValue > minWindow && midValue < maxWindow) {
                int x = valueToX(midValue, minWindow, unitsPerPixelX, barLeft + borderSize, barWidth - 2 * borderSize);
                int rightBorder = barLeft + barWidth - borderSize;
                if (x >= rightBorder) x = rightBorder - 1;
                int yStart = barTop + borderSize + 1;
                int yEnd = barTop + barHeight - borderSize - 1;
                fill(ctx, x, yStart, x + 1, yEnd, 0xCCFFFFFF); // less see-through (midpoint)

                // Draw temperature value above the tick (smaller font for midpoint)
                String tempText = String.valueOf(midValue);
                ctx.getMatrices().push();
                float scale = 0.33f; // Midpoint tick temperature value: smaller than boundary
                ctx.getMatrices().translate(x + 1, barTop - 3, 0); // Move text 1px right, just above bar
                ctx.getMatrices().scale(scale, scale, 1.0f);
                int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(tempText);
                int textX = -textWidth / 2;
                int textY = 0;
                ctx.drawText(MinecraftClient.getInstance().textRenderer, tempText, textX, textY, 0xFFFFFFFF, false);
                ctx.getMatrices().pop();
            }

            // Quarter ticks (shorter)
            int quarterValue = Math.round(start + interval / 4.0f);
            int threeQuarterValue = Math.round(start + 3.0f * interval / 4.0f);
            for (int tickValue : new int[]{quarterValue, threeQuarterValue}) {
                if (tickValue > minWindow && tickValue < maxWindow) {
                    int x = valueToX(tickValue, minWindow, unitsPerPixelX, barLeft + borderSize, barWidth - 2 * borderSize);
                    int rightBorder = barLeft + barWidth - borderSize;
                    if (x >= rightBorder) x = rightBorder - 1;
                    int yStart = barTop + borderSize + 2; // shorter tick
                    int yEnd = barTop + barHeight - borderSize - 2;
                    fill(ctx, x, yStart, x + 1, yEnd, 0x88FFFFFF); // more see-through (quarter)
                }
            }
        }

        // Current temperature arrow just below the bar, pointing down
        int tempX = valueToX(temp, minWindow, unitsPerPixelX, barLeft + borderSize, barWidth - 2 * borderSize);
        int rightBorder = barLeft + barWidth - borderSize;
        if (tempX >= rightBorder) {
            tempX = rightBorder - 1;
        }
        int arrowBottomY = barTop + barHeight + 1;
        drawDownArrow(ctx, tempX, arrowBottomY, 0xFFFFFFFF);
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

    // Convert a value in [minWindow, maxWindow] to a X coordinate along the bar (left-to-right)
    private int valueToX(int value, int minWindow, float unitsPerPixelX, int barLeft, int barWidth) {
        float exactPixelOffset = (value - minWindow) / unitsPerPixelX;
        int x = barLeft + Math.round(exactPixelOffset);
        if (x < barLeft) x = barLeft;
        if (x > barLeft + barWidth - 1) x = barLeft + barWidth - 1;
        return x;
    }

    private int nearestMultiple(int n, int step) {
        if (step <= 0) return n;
        int r = n % step;
        return r == 0 ? n : (n - r + step);
    }

    // Draw a small down-pointing arrow; bottomY is the tip's Y
    private void drawDownArrow(DrawContext ctx, int centerX, int bottomY, int color) {
        int half = 3; // half-width
        for (int i = 0; i <= 4; i++) {
            int extent = Math.max(0, half - i + 1);
            int x1 = centerX - extent;
            int x2 = centerX + extent + 1;
            fill(ctx, x1, bottomY - i, x2, bottomY - i + 1, color);
        }
    }

    private void fill(DrawContext ctx, int x1, int y1, int x2, int y2, int argb) {
        ctx.fill(x1, y1, x2, y2, argb);
    }

    // Correctly map a temperature value to the segment index defined by 'boundaries'
    private int segmentIndex(int value, int[] boundaries) {
        int idx = Arrays.binarySearch(boundaries, value);
        if (idx >= 0) {
            // Boundary values belong to the right-hand segment, except the last boundary
            return Math.min(idx, boundaries.length - 2);
        }
        int insertionPoint = -(idx + 1);
        return Math.max(0, insertionPoint - 1);
    }


    // Scale a base (0..10000) temperature to current max
    private int scaleToMax(int base, int maxTemp) {
        if (maxTemp >= 10000) return base;
        return Math.round(base / 10000f * maxTemp);
    }
}
