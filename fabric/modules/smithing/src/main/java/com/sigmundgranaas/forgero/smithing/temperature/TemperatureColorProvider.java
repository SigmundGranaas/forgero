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
            {1600, 0xFFFF_FF99},
            {1500, 0xFFFF_FF66},
            {1400, 0xFFFF_CC33},
            {1300, 0xFFFF_9900},
            {1200, 0xFFFF_6600},
            {1100, 0xFFFF_3300},
            {1000, 0xFFFF_0000},
            {900,  0xFFCC_0000},
            {800,  0xFF99_0000},
            {700,  0xFF66_0000},
            {600,  0xFF33_0000},
            {500,  0xFF22_0000},
            {20,   0x00FFFFFF}
        };

        if (maxTemp <= 0) {
            return baseScale[baseScale.length - 1][1];
        }

        int effectiveMax = Math.min(maxTemp, REAL_CAP);
        int[][] scale;
        if (effectiveMax == BASE_MAX) {
            scale = baseScale;
        } else {
            scale = new int[baseScale.length][2];
            for (int i = 0; i < baseScale.length; i++) {
                int origTemp = baseScale[i][0];
                int scaledTemp = Math.round(origTemp / (float) BASE_MAX * effectiveMax);
                scale[i][0] = scaledTemp;
                scale[i][1] = baseScale[i][1];
            }
        }

        if (temperature > scale[0][0]) {
            return scale[0][1];
        }

        for (int i = 0; i < scale.length - 1; i++) {
            int tHigh = scale[i][0];
            int tLow = scale[i + 1][0];
            int cHigh = scale[i][1];
            int cLow = scale[i + 1][1];
            if (tHigh == tLow) continue;
            if (temperature >= tLow && temperature <= tHigh) {
                float t = (temperature - tLow) / (float) (tHigh - tLow);
                t = Math.max(0f, Math.min(1f, t));
                return lerpColor(cLow, cHigh, t);
            }
        }
        return scale[scale.length - 1][1];
    }

    private static int lerpColor(int colorA, int colorB, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int aA = (colorA >> 24) & 0xFF;
        int aR = (colorA >> 16) & 0xFF;
        int aG = (colorA >> 8) & 0xFF;
        int aB = colorA & 0xFF;
        int bA = (colorB >> 24) & 0xFF;
        int bR = (colorB >> 16) & 0xFF;
        int bG = (colorB >> 8) & 0xFF;
        int bB = colorB & 0xFF;
        int a = (int) (aA + (bA - aA) * t);
        int r = (int) (aR + (bR - aR) * t);
        int g = (int) (aG + (bG - aG) * t);
        int b = (int) (aB + (bB - aB) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static boolean isHotEnoughForWork(int temperature, int maxTemp) {
        int minWorkingTemp = maxTemp >= 10000 ? 3125 : (int) (3125 / 10000.0 * maxTemp);
        return temperature >= minWorkingTemp;
    }

    private static final int[] BASE_STAGE_BOUNDS = new int[]{
        0,
        320,
        750,
        1200,
        1500,
        1600
    };

    public static int[] getStageBoundaries(int maxTemp) {
        final int BASE_MAX = 1600;
        final int REAL_CAP = 10000;
        int effectiveMax = Math.min(maxTemp, REAL_CAP);
        if (effectiveMax == BASE_MAX) {
            return BASE_STAGE_BOUNDS;
        }
        int[] scaled = new int[BASE_STAGE_BOUNDS.length];
        for (int i = 0; i < BASE_STAGE_BOUNDS.length; i++) {
            scaled[i] = Math.round(BASE_STAGE_BOUNDS[i] / (float) BASE_MAX * effectiveMax);
        }
        return scaled;
    }

    public static int[] getStageBoundariesWithWorkableRange(int maxTemp, int workableStart, int workableEnd) {
        int[] bounds = getStageBoundaries(maxTemp);
        if (workableStart > 0 && workableEnd > workableStart) {
            bounds[2] = workableStart;
            bounds[3] = workableEnd;
        }
        return bounds;
    }

    private static boolean isInStage(int temperature, int maxTemp, int stageIdx) {
        int[] bounds = getStageBoundaries(maxTemp);
        return temperature >= bounds[stageIdx] && temperature < bounds[stageIdx + 1];
    }

    private static boolean isInStageWithBounds(int temperature, int[] bounds, int stageIdx) {
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
    public static boolean isInBrightHot(int temperature, int maxTemp) {
        return isInStage(temperature, maxTemp, 3);
    }
    public static boolean isInOverheated(int temperature, int maxTemp) {
        return isInStage(temperature, maxTemp, 4);
    }

    public static int getHudColorForTemperature(int temp, int maxTemp, int[] bounds, int idxCold, int idxWarm, int idxHot, int idxBrightHot, int idxOverheated, int stageIdx) {
        final int DARK_BLUE   = 0xFF000099;
        final int DARK_CYAN   = 0xFF3399FF;
        final int DARK_YELLOW = 0xFFCCCC00;
        final int DARK_ORANGE = 0xFFCC6600;
        final int DARK_RED    = 0xFFCC0000;
        if (stageIdx == idxCold)       return DARK_BLUE;
        else if (stageIdx == idxWarm)       return DARK_CYAN;
        else if (stageIdx == idxHot)        return DARK_YELLOW;
        else if (stageIdx == idxBrightHot) return DARK_ORANGE;
        else if (stageIdx == idxOverheated) return DARK_RED;
        else return DARK_YELLOW;
    }
}
