package com.sigmundgranaas.forgero.smithing.minigame;

import java.util.Arrays;

import com.sigmundgranaas.forgero.smithing.block.entity.custom.SmithingAnvilBlockEntity;
import com.sigmundgranaas.forgero.smithing.item.custom.MorphedItem;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureColorProvider;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

// TODO create our own texture for the outside of the bar. Including arrow and ticks.
public class MinigameHudOverlay implements HudRenderCallback {
    // Small, always-on client registration if class is loaded.
    static {
        try {
            HudRenderCallback.EVENT.register(new MinigameHudOverlay());
        } catch (Throwable ignored) {
        }
    }
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

        // Compute a 300-degree sliding window around current temperature.
        final int window = 600;
        int half = window / 2;
        int minWindow = Math.max(0, Math.min(temp - half, Math.max(0, max - window)));
        int maxWindow = Math.min(max, minWindow + window);
        if (maxWindow <= minWindow) return;

        // Horizontal bar placement and dimensions (center top, slightly larger)
        int screenW = ctx.getScaledWindowWidth();
        int barWidth = 140; // reduced width for a less wide bar
        int barHeight = 10;
        int barLeft = (screenW - barWidth) / 2;
        int barTop = 10;

        int[] boundaries = TemperatureColorProvider.getStageBoundaries(max);
        // Precompute segment indices for each stage using scaled midpoints
        int idxTempering  = segmentIndex(scaleToMax(600,  max), boundaries);
        int idxCritical   = segmentIndex(scaleToMax(800,  max), boundaries);
        int idxShaping    = segmentIndex(scaleToMax(1000, max), boundaries);
        int idxForging    = segmentIndex(scaleToMax(1200, max), boundaries);
        int idxWelding    = segmentIndex(scaleToMax(1400, max), boundaries);
        int idxOverheated = segmentIndex(scaleToMax(1550, max), boundaries);

        // Only show bar from tempering and up
        int minStageBoundary = boundaries[idxTempering];
        minWindow = Math.max(minStageBoundary, minWindow);
        if (maxWindow <= minWindow) return;
        float unitsPerPixelX = (float) (maxWindow - minWindow) / (float) barWidth;

        // Hardcoded stage colors per segment (no blending)
        for (int x = 0; x < barWidth; x++) {
            int valueAtX = minWindow + Math.round(x * unitsPerPixelX);
            int segIdx = segmentIndex(valueAtX, boundaries);
            int argb = colorForSegment(segIdx, idxTempering, idxCritical, idxShaping, idxForging, idxWelding, idxOverheated);
            fill(ctx, barLeft + x, barTop, barLeft + x + 1, barTop + barHeight, argb);
        }

        // 1px square border
        int border = 0xFF000000;
        fill(ctx, barLeft, barTop, barLeft + barWidth, barTop + 1, border); // top
        fill(ctx, barLeft, barTop + barHeight - 1, barLeft + barWidth, barTop + barHeight, border); // bottom
        fill(ctx, barLeft, barTop, barLeft + 1, barTop + barHeight, border); // left
        fill(ctx, barLeft + barWidth - 1, barTop, barLeft + barWidth, barTop + barHeight, border); // right

        // Stage boundary ticks (straight borders between stages)
        for (int t : boundaries) {
            if (t < minWindow || t > maxWindow) continue;
            int x = valueToX(t, minWindow, unitsPerPixelX, barLeft, barWidth);
            // Only draw ticks strictly inside the bar, not on the border
            if (x > barLeft && x < barLeft + barWidth - 1) {
                int yStart = barTop + 1;
                int yEnd = barTop + barHeight - 1;
                fill(ctx, x, yStart, x + 1, yEnd, 0xFFFFFFFF); // fully opaque white
            }
        }

        // Current temperature arrow just below the bar, pointing down
        int tempX = valueToX(temp, minWindow, unitsPerPixelX, barLeft, barWidth);
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

    // Convert a value in [minWindow, minWindow+range] to a X coordinate along the bar (left-to-right)
    private int valueToX(int value, int minWindow, float unitsPerPixelX, int barLeft, int barWidth) {
        float clamped = Math.max(minWindow, value);
        int rel = Math.round((clamped - minWindow) / unitsPerPixelX);
        int x = barLeft + rel;
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

    // Map segment index to hardcoded colors based on stage indices
    private int colorForSegment(int segIdx,
                                int idxTempering, int idxCritical,
                                int idxShaping, int idxForging, int idxWelding, int idxOverheated) {
        final int BLUE   = 0xFF0077FF; // tempering (and cold)
        final int YELLOW = 0xFFFFCC00; // shaping and welding
        final int GREEN  = 0xFF00CC00; // forging
        final int RED    = 0xFFCC0000; // critical and overheated

        if (segIdx == idxForging) return GREEN;
        if (segIdx == idxShaping || segIdx == idxWelding) return YELLOW;
        if (segIdx == idxCritical || segIdx == idxOverheated) return RED;
        if (segIdx == idxTempering) return BLUE;
        // Fallback
        return YELLOW;
    }

    // Scale a base (0..1600) temperature to current max
    private int scaleToMax(int base, int maxTemp) {
        if (maxTemp >= 1600) return base;
        return Math.round(base / 1600f * maxTemp);
    }
}
