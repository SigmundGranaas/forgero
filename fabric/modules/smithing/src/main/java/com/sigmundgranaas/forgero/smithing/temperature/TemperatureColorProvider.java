package com.sigmundgranaas.forgero.smithing.temperature;

import net.minecraft.registry.Registries;

import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;

public class TemperatureColorProvider {
    public static void register() {
        Registries.ITEM.forEach(item -> {
            ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
                if (!TemperatureUtils.hasMaxTemperature(stack)) {
                    return 0xFFFFFF; // Default color (white) for items without temperature
                }
                int temp = TemperatureUtils.getTemperature(stack);
                int max = TemperatureUtils.getMaxTemp(stack);
                return getHeatColor(temp, max);
            }, item);
        });
    }

    // Change getHeatColor to public so it can be accessed from other classes
    public static int getHeatColor(int temperature, int maxTemp) {
        // Colors are in 0xRRGGBB format
        final int[][] baseScale = {
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



        // If maxTemp >= 2000, use the original scale
        if (maxTemp >= 2000) {
            for (int i = 0; i < baseScale.length - 1; i++) {
                int tHigh = baseScale[i][0];
                int tLow = baseScale[i + 1][0];
                int cHigh = baseScale[i][1];
                int cLow = baseScale[i + 1][1];
                if (temperature >= tLow && temperature <= tHigh) {
                    float t = (temperature - tLow) / (float)(tHigh - tLow);
                    return lerpColor(cLow, cHigh, t);
                }
            }
            return baseScale[baseScale.length - 1][1];
        }

        // Scale the stops to fit maxTemp
        int[][] scaledScale = new int[baseScale.length][2];
        for (int i = 0; i < baseScale.length; i++) {
            int origTemp = baseScale[i][0];
            int scaledTemp = (int)(origTemp / 2000.0 * maxTemp);
            scaledScale[i][0] = scaledTemp;
            scaledScale[i][1] = baseScale[i][1];
        }
        for (int i = 0; i < scaledScale.length - 1; i++) {
            int tHigh = scaledScale[i][0];
            int tLow = scaledScale[i + 1][0];
            int cHigh = scaledScale[i][1];
            int cLow = scaledScale[i + 1][1];
            if (temperature >= tLow && temperature <= tHigh) {
                float t = (temperature - tLow) / (float)(tHigh - tLow);
                return lerpColor(cLow, cHigh, t);
            }
        }
        return scaledScale[scaledScale.length - 1][1];
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



    // --- Stage checker for orange-red, orange, and orange-yellow combined ---
    public static boolean inFirstStageSmithing(int temperature, int maxTemp) {
        return isInStage(temperature, maxTemp, 1500, 1800);
    }

	public static boolean inSecondStageSmithing(int temperature, int maxTemp) {
		return isInStage(temperature, maxTemp, 1100, 1300);
	}

    // Helper to check if temperature is in a scaled stage
    private static boolean isInStage(int temperature, int maxTemp, int min, int max) {
        if (maxTemp >= 2000) {
            return temperature >= min && temperature < max;
        } else {
            int scaledMin = (int)(min / 2000.0 * maxTemp);
            int scaledMax = (int)(max / 2000.0 * maxTemp);
            return temperature >= scaledMin && temperature < scaledMax;
        }
    }
}
