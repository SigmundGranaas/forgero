package com.sigmundgranaas.forgero.smithing.temperature;

import com.sigmundgranaas.forgero.minecraft.common.item.StateItem;
import com.sigmundgranaas.forgero.smithing.util.ToolPartTypeUtils;

import net.minecraft.registry.Registries;

import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;

public class TemperatureColorProvider {
    public static void register() {
        Registries.ITEM.forEach(item -> {
            if (item instanceof StateItem stateItem) {
                var type = stateItem.defaultState().type();
                if (ToolPartTypeUtils.isToolPartType(type)) {
                    ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
                        int temp = TemperatureUtils.getTemperature(stack);
                        return getHeatColor(temp);
                    }, item);
                }
            }
        });
    }

    // Map temperature to color according to the provided scale
    private static int getHeatColor(int temperature) {
        // Colors are in 0xRRGGBB format
        final int[][] scale = {
            {2000, 0xFFFF00}, // Bright Yellow
            {1900, 0xFFD700}, // Dark Yellow
            {1800, 0xFFC800}, // Orange Yellow
            {1700, 0xFF9900}, // Orange
            {1600, 0xFF5500}, // Orange Red
            {1500, 0xFF2222}, // Bright Red
            {1400, 0xFF0000}, // Red
            {1300, 0xCC0000}, // Medium Red
            {1200, 0x990000}, // Dull Red
            {1100, 0x882222}, // Slight Red
            {1000, 0x555555}, // Very Slightly Red, Mostly Grey
            {800,  0x222222}, // Dark Grey
            {575,  0x222288}, // Blue
            {540,  0x220055}, // Dark Purple
            {520,  0x660088}, // Purple
            {500,  0x442233}, // Brown/Purple
            {480,  0x664422}, // Brown
            {465,  0xBBAA44}, // Dark Straw
            {445,  0xFFFACD}, // Light Straw
            {390,  0xFFF8DC}, // Faint Straw
            {0,    0xCCCCCC}  // Default cold (grey)
        };

        for (int i = 0; i < scale.length - 1; i++) {
            int tHigh = scale[i][0];
            int tLow = scale[i + 1][0];
            int cHigh = scale[i][1];
            int cLow = scale[i + 1][1];
            if (temperature >= tLow && temperature <= tHigh) {
                float t = (temperature - tLow) / (float)(tHigh - tLow);
                return lerpColor(cLow, cHigh, t);
            }
        }
        // Below lowest, return cold color
        return scale[scale.length - 1][1];
    }

    // Linear interpolation between two RGB colors
    private static int lerpColor(int colorA, int colorB, float t) {
        int aR = (colorA >> 16) & 0xFF;
        int aG = (colorA >> 8) & 0xFF;
        int aB = colorA & 0xFF;
        int bR = (colorB >> 16) & 0xFF;
        int bG = (colorB >> 8) & 0xFF;
        int bB = colorB & 0xFF;
        int r = (int)(aR + (bR - aR) * t);
        int g = (int)(aG + (bG - aG) * t);
        int b = (int)(aB + (bB - aB) * t);
        return (r << 16) | (g << 8) | b;
    }
}
