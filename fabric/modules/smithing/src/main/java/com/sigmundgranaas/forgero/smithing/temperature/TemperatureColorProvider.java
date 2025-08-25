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
				{1600, 0xFFFF99}, // Very bright yellow-orange (upper forging limit)
				{1500, 0xFFFF66}, // Bright yellow
				{1400, 0xFFCC33}, // Yellow-orange
				{1300, 0xFF9900}, // Orange
				{1200, 0xFF6600}, // Deep orange
				{1100, 0xFF3300}, // Bright red-orange
				{1000, 0xFF0000}, // Bright red
				{900,  0xCC0000}, // Red
				{800,  0x990000}, // Dark red
				{700,  0x660000}, // Very dark red
				{600,  0x330000}, // Faint red
				{500,  0x220000}, // Barely glowing red
				{0,    0xCCCCCC}  // Cold metal (neutral grey)
		};



        // If maxTemp >= 1600, use the original scale
        if (maxTemp >= 1600) {
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
            int scaledTemp = (int)(origTemp / 1600.0 * maxTemp);
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




    public static boolean isHotEnoughForWork(int temperature, int maxTemp) {
        int minWorkingTemp = maxTemp >= 1600 ? 500 : (int)(500 / 1600.0 * maxTemp);
        return temperature >= minWorkingTemp;
    }

	public static boolean isInOverheatedStage(int temperature, int maxTemp) {
		return isInStage(temperature, maxTemp, 1500, 1601); // 1500+ is liquid
	}

	public static boolean isInWeldingStage(int temperature, int maxTemp) {
		return isInStage(temperature, maxTemp, 1300, 1500); // 1300–1500
	}

	public static boolean isInForgingStage(int temperature, int maxTemp) {
		return isInStage(temperature, maxTemp, 1100, 1300); // 1100–1300
	}

	public static boolean isInShapingStage(int temperature, int maxTemp) {
		return isInStage(temperature, maxTemp, 900, 1100); // 900–1100
	}

	public static boolean isInCriticalStage(int temperature, int maxTemp) {
		return isInStage(temperature, maxTemp, 700, 900); // 700–900
	}

	public static boolean isInTemperingStage(int temperature, int maxTemp) {
		return isInStage(temperature, maxTemp, 500, 700); // 500–700
	}

	public static boolean isInColdStage(int temperature, int maxTemp) {
		return isInStage(temperature, maxTemp, 0, 500); // <500
	}

	public static boolean isInPerfectStage(int temperature, int maxTemp) {
		return isInStage(temperature, maxTemp, 1150, 1250); // 1100–1300
	}

	private static boolean isInStage(int temperature, int maxTemp, int min, int max) {
		if (maxTemp >= 1600) {
			return temperature >= min && temperature < max;
		} else {
			int scaledMin = (int)(min / 1600.0 * maxTemp);
			int scaledMax = (int)(max / 1600.0 * maxTemp);
			return temperature >= scaledMin && temperature < scaledMax;
		}
	}
}
