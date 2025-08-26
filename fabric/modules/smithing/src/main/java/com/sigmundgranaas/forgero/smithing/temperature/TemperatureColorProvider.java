package com.sigmundgranaas.forgero.smithing.temperature;

import net.minecraft.registry.Registries;

import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;

public class TemperatureColorProvider {
    public static void register() {
        Registries.ITEM.forEach(item -> {
            ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
                if (!TemperatureUtils.hasMaxTemperature(stack)) {
                    return 0xFFFFFF;
                }
                int temp = TemperatureUtils.getTemperature(stack);
                int max = TemperatureUtils.getMaxTemp(stack);
                return getHeatColor(temp, max);
            }, item);
        });
    }

    public static int getHeatColor(int temperature, int maxTemp) {

        final int[][] baseScale = {
            {1600, 0xFFFF_FF99}, // Very bright yellow-orange (upper forging limit)
            {1500, 0xFFFF_FF66}, // Bright yellow
            {1400, 0xFFFF_CC33}, // Yellow-orange
            {1300, 0xFFFF_9900}, // Orange
            {1200, 0xFFFF_6600}, // Deep orange
            {1100, 0xFFFF_3300}, // Bright red-orange
            {1000, 0xFFFF_0000}, // Bright red
            {900,  0xFFCC_0000}, // Red
            {800,  0xFF99_0000}, // Dark red
            {700,  0xFF66_0000}, // Very dark red
            {600,  0xFF33_0000}, // Faint red
            {500,  0xFF22_0000}, // Barely glowing red
            {20,   0x00FFFFFF}  // Fully transparent
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

    // Linear interpolation between two ARGB colors
    private static int lerpColor(int colorA, int colorB, float t) {
        int aA = (colorA >> 24) & 0xFF;
        int aR = (colorA >> 16) & 0xFF;
        int aG = (colorA >> 8) & 0xFF;
        int aB = colorA & 0xFF;
        int bA = (colorB >> 24) & 0xFF;
        int bR = (colorB >> 16) & 0xFF;
        int bG = (colorB >> 8) & 0xFF;
        int bB = colorB & 0xFF;
        int a = (int)(aA + (bA - aA) * t);
        int r = (int)(aR + (bR - aR) * t);
        int g = (int)(aG + (bG - aG) * t);
        int b = (int)(aB + (bB - aB) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
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

    // New: scaled stage boundaries (Cold|Tempering|Critical|Shaping|Forging|Welding|Overheated)
    // Base boundaries are defined for a 0..1600 range and scaled for lower max temps.
    public static int[] getStageBoundaries(int maxTemp) {
        int[] base = new int[]{20, 500, 700, 900, 1100, 1300, 1500, 1600};
        if (maxTemp >= 1600) {
            return base;
        }
        int[] scaled = new int[base.length];
        for (int i = 0; i < base.length; i++) {
            scaled[i] = (int) (base[i] / 1600.0 * maxTemp);
        }
        // Deduplicate after scaling to avoid overlapping ticks
        java.util.ArrayList<Integer> uniq = new java.util.ArrayList<>(scaled.length);
        int prev = Integer.MIN_VALUE;
        for (int v : scaled) {
            if (uniq.isEmpty() || v != prev) {
                uniq.add(v);
                prev = v;
            }
        }
        int[] out = new int[uniq.size()];
        for (int i = 0; i < uniq.size(); i++) out[i] = uniq.get(i);
        return out;
    }
}
