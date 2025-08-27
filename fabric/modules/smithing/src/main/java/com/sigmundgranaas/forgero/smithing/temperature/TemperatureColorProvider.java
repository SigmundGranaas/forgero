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
        final int BASE_MAX = 1600;
        final int REAL_CAP = 10000;
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

        int effectiveMax = Math.min(maxTemp, REAL_CAP);
        // If effectiveMax == BASE_MAX, use the original scale
        if (effectiveMax == BASE_MAX) {
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

        // Scale the stops to fit effectiveMax
        int[][] scaledScale = new int[baseScale.length][2];
        for (int i = 0; i < baseScale.length; i++) {
            int origTemp = baseScale[i][0];
            int scaledTemp = (int)(origTemp / (float)BASE_MAX * effectiveMax);
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
        int minWorkingTemp = maxTemp >= 10000 ? 3125 : (int)(3125 / 10000.0 * maxTemp);
        return temperature >= minWorkingTemp;
    }

	public static boolean isInPerfectStage(int temperature, int maxTemp) {
		int[] bounds = getStageBoundaries(maxTemp);
		// Use the second-to-last stage as the 'perfect' stage (Near Melt)
		// If you want a different range, adjust the indices accordingly
		int min = bounds[bounds.length - 3]; // Near Melt start
		int max = bounds[bounds.length - 2]; // Near Melt end
		return temperature >= min && temperature < max;
	}

	// BASELINE GROUP TEMPS

	// Stage boundaries for base max 1600
    private static final int[] BASE_STAGE_BOUNDS = new int[]{
        0,    // Cold start
        500,  // Cold end, Warm start
        700, // Warm end, Hot start
        900, // Hot end, Very Hot start
        1100, // Very Hot end, Extreme start
        1400, // Extreme end, Near Melt start
        1500, // Near Melt end, Molten start
        1600  // Molten end
    };

    // Returns scaled stage boundaries for any maxTemp
    public static int[] getStageBoundaries(int maxTemp) {
        final int BASE_MAX = 1600;
        final int REAL_CAP = 10000;
        int effectiveMax = Math.min(maxTemp, REAL_CAP);
        if (effectiveMax == BASE_MAX) {
            return BASE_STAGE_BOUNDS;
        }
        int[] scaled = new int[BASE_STAGE_BOUNDS.length];
        for (int i = 0; i < BASE_STAGE_BOUNDS.length; i++) {
            scaled[i] = (int) (BASE_STAGE_BOUNDS[i] / (float)BASE_MAX * effectiveMax);
        }
        return scaled;
    }

    // Helper to get scaled boundaries for a stage
    private static boolean isInStage(int temperature, int maxTemp, int stageIdx) {
        int[] bounds = getStageBoundaries(maxTemp);
        return temperature >= bounds[stageIdx] && temperature < bounds[stageIdx + 1];
    }

    public static boolean isInCold(int temperature, int maxTemp) {
        return isInStage(temperature, maxTemp, 0);
    }
    public static boolean isInWarm(int temperature, int maxTemp) {
        return isInStage(temperature, maxTemp, 1);
    }
    public static boolean isInHot(int temperature, int maxTemp) {
        return isInStage(temperature, maxTemp, 2);
    }
    public static boolean isInVeryHot(int temperature, int maxTemp) {
        return isInStage(temperature, maxTemp, 3);
    }
    public static boolean isInExtreme(int temperature, int maxTemp) {
        return isInStage(temperature, maxTemp, 4);
    }
    public static boolean isInNearMelt(int temperature, int maxTemp) {
        return isInStage(temperature, maxTemp, 5);
    }
    public static boolean isInMolten(int temperature, int maxTemp) {
        return isInStage(temperature, maxTemp, 6);
    }


    /**
     * Returns HUD color for the given temperature and stage indices.
     */
    public static int getHudColorForTemperature(int temp, int maxTemp, int[] bounds, int idxCold, int idxCool, int idxMild, int idxWarm, int idxExtreme, int idxVeryHot, int idxMolten, int stageIdx) {
        final int BLUE     = 0xFF0000FF; // Cold
        final int CYAN     = 0xFF00FFFF; // Cool/Ambient
        final int TEAL     = 0xFF00FF80; // Mild/Warm
        final int YELLOW   = 0xFFFFFF00; // Warm/Hot
        final int GREEN    = 0xFF00FF00; // Extreme/Unusual
        final int ORANGE   = 0xFFFFA500; // Very Hot/Near Molten
        final int RED      = 0xFFFF0000; // Molten
        if (stageIdx == idxCold)    return BLUE;
        else if (stageIdx == idxCool)    return CYAN;
        else if (stageIdx == idxMild)    return TEAL;
        else if (stageIdx == idxWarm)    return YELLOW;
        else if (stageIdx == idxExtreme) return GREEN;
        else if (stageIdx == idxVeryHot) return ORANGE;
        else if (stageIdx == idxMolten)  return RED;
        else return YELLOW; // fallback
    }
}
