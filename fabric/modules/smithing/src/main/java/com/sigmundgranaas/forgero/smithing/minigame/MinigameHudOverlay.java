package com.sigmundgranaas.forgero.smithing.minigame;

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
        final int window = 300;
        int half = window / 2;
        int minWindow = Math.max(0, Math.min(temp - half, Math.max(0, max - window)));
        int maxWindow = Math.min(max, minWindow + window);
        if (maxWindow <= minWindow) return;

        // Horizontal bar placement and dimensions (center top, slightly larger)
        int screenW = ctx.getScaledWindowWidth();
        int barWidth = 180; // slightly larger than before
        int barHeight = 10;
        int barLeft = (screenW - barWidth) / 2;
        int barTop = 10;

        float unitsPerPixelX = (float) (maxWindow - minWindow) / (float) barWidth;

        // Temperature-colored bar fill (simple rectangle)
        for (int x = 0; x < barWidth; x++) {
            int valueAtX = minWindow + Math.round(x * unitsPerPixelX);
            int rgb = TemperatureColorProvider.getHeatColor(valueAtX, max);
            int argb = 0xFF000000 | rgb;
            fill(ctx, barLeft + x, barTop, barLeft + x + 1, barTop + barHeight, argb);
        }

        // 1px square border
        int border = 0xFF000000;
        fill(ctx, barLeft, barTop, barLeft + barWidth, barTop + 1, border); // top
        fill(ctx, barLeft, barTop + barHeight - 1, barLeft + barWidth, barTop + barHeight, border); // bottom
        fill(ctx, barLeft, barTop, barLeft + 1, barTop + barHeight, border); // left
        fill(ctx, barLeft + barWidth - 1, barTop, barLeft + barWidth, barTop + barHeight, border); // right

        // Minor ticks every 50 degrees rendered INSIDE the bar as shorter vertical lines
        int minorStep = 50;
        for (int t = nearestMultiple(minWindow, minorStep); t <= maxWindow; t += minorStep) {
            if (t % 100 == 0) continue; // skip where major ticks will be drawn
            int x = valueToX(t, minWindow, unitsPerPixelX, barLeft, barWidth);
            int yStart = barTop + 2;                      // shorter inside bar
            int yEnd = barTop + barHeight - 2;
            if (yEnd > yStart) {
                fill(ctx, x, yStart, x + 1, yEnd, 0x88FFFFFF);
            }
        }

        // Major ticks every 100 degrees rendered INSIDE the bar as full-height vertical lines
        int tickStep = 100;
        for (int t = nearestMultiple(minWindow, tickStep); t <= maxWindow; t += tickStep) {
            int x = valueToX(t, minWindow, unitsPerPixelX, barLeft, barWidth);
            int yStart = barTop + 1;                      // inside border
            int yEnd = barTop + barHeight - 1;
            if (yEnd > yStart) {
                fill(ctx, x, yStart, x + 1, yEnd, 0xCCFFFFFF);
            }
        }

        // Degree labels centered ABOVE each 100-degree tick
        var tr = MinecraftClient.getInstance().textRenderer;
        float labelScale = 0.7f;
        int fontHeight = 9; // Minecraft default font height
        int scaledTextHeight = Math.round(fontHeight * labelScale);
        for (int t = nearestMultiple(minWindow, 100); t <= maxWindow; t += 100) {
            int x = valueToX(t, minWindow, unitsPerPixelX, barLeft, barWidth);
            String s = Integer.toString(t);
            int textW = tr.getWidth(s);
            int textX = x - Math.round(textW * labelScale / 2f);
            int textY = barTop - scaledTextHeight - 2; // above the bar
            ctx.getMatrices().push();
            ctx.getMatrices().translate(textX, textY, 0);
            ctx.getMatrices().scale(labelScale, labelScale, 1.0f);
            ctx.drawText(tr, s, 0, 0, 0xFFFFFFFF, false);
            ctx.getMatrices().pop();
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
}
