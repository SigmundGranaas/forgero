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
            {10000, 0xFFFF_FF99}, // Very bright yellow-orange (upper forging limit)
            {9375, 0xFFFF_FF66}, // Bright yellow
            {8750, 0xFFFF_CC33}, // Yellow-orange
            {8125, 0xFFFF_9900}, // Orange
            {7500, 0xFFFF_6600}, // Deep orange
            {6875, 0xFFFF_3300}, // Bright red-orange
            {6250, 0xFFFF_0000}, // Bright red
            {5625,  0xFFCC_0000}, // Red
            {5000,  0xFF99_0000}, // Dark red
            {4375,  0xFF66_0000}, // Very dark red
            {3750,  0xFF33_0000}, // Faint red
            {3125,  0xFF22_0000}, // Barely glowing red
            {20,   0x00FFFFFF}  // Fully transparent
        };

        // If maxTemp >= 10000, use the original scale
        if (maxTemp >= 10000) {
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
            int scaledTemp = (int)(origTemp / 10000.0 * maxTemp);
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

	public static int getTooltipColor(int temperature, int maxTemp) {

		final int[][] baseScale = {
				{10000, 0xFFFF_FF99}, // Very bright yellow-orange (upper forging limit)
				{9375, 0xFFFF_FF66}, // Bright yellow
				{8750, 0xFFFF_CC33}, // Yellow-orange
				{8125, 0xFFFF_9900}, // Orange
				{7500, 0xFFFF_6600}, // Deep orange
				{6875, 0xFFFF_3300}, // Bright red-orange
				{6250, 0xFFFF_0000}, // Bright red
				{5625,  0xFFCC_0000}, // Red
				{5000,  0xFF99_0000}, // Dark red
				{4375,  0xFF66_0000}, // Very dark red
				{3750,  0xFF33_0000}, // Faint red
				{3125,  0xFF22_0000}, // Barely glowing red
				{20,   0x00FFFFFF}  // Fully transparent
		};

		// If maxTemp >= 10000, use the original scale
		if (maxTemp >= 10000) {
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
			int scaledTemp = (int)(origTemp / 10000.0 * maxTemp);
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





    public static boolean isHotEnoughForWork(int temperature, int maxTemp) {
        int minWorkingTemp = maxTemp >= 10000 ? 3125 : (int)(3125 / 10000.0 * maxTemp);
        return temperature >= minWorkingTemp;
    }

	public static boolean isInOverheatedStage(int temperature, int maxTemp) {
		return isInStage(temperature, maxTemp, 9375, 10001); // 9375+ is liquid
	}

	public static boolean isInWeldingStage(int temperature, int maxTemp) {
		return isInStage(temperature, maxTemp, 8125, 9375); // 8125–9375
	}

	public static boolean isInForgingStage(int temperature, int maxTemp) {
		return isInStage(temperature, maxTemp, 6875, 8125); // 6875–8125
	}

	public static boolean isInShapingStage(int temperature, int maxTemp) {
		return isInStage(temperature, maxTemp, 5625, 6875); // 5625–6875
	}

	public static boolean isInCriticalStage(int temperature, int maxTemp) {
		return isInStage(temperature, maxTemp, 4375, 5625); // 4375–5625
	}

	public static boolean isInTemperingStage(int temperature, int maxTemp) {
		return isInStage(temperature, maxTemp, 3125, 4375); // 3125–4375
	}

	public static boolean isInColdStage(int temperature, int maxTemp) {
		return isInStage(temperature, maxTemp, 0, 3125); // <3125
	}

	public static boolean isInPerfectStage(int temperature, int maxTemp) {
		return isInStage(temperature, maxTemp, 8125, 8750); // 8125–9375
	}

	private static boolean isInStage(int temperature, int maxTemp, int min, int max) {
		if (maxTemp >= 10000) {
			return temperature >= min && temperature < max;
		} else {
			int scaledMin = (int)(min / 10000.0 * maxTemp);
			int scaledMax = (int)(max / 10000.0 * maxTemp);
			return temperature >= scaledMin && temperature < scaledMax;
		}
	}

    // New: scaled stage boundaries (Cold|Tempering|Critical|Shaping|Forging|Welding|Overheated)
    // Base boundaries are defined for a 0..10000 range and scaled for lower max temps.
    public static int[] getStageBoundaries(int maxTemp) {
        int[] base = new int[]{20, 3125, 4375, 5625, 6875, 8125, 9375, 10000};
        if (maxTemp >= 10000) {
            return base;
        }
        int[] scaled = new int[base.length];
        for (int i = 0; i < base.length; i++) {
            scaled[i] = (int) (base[i] / 10000.0 * maxTemp);
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
