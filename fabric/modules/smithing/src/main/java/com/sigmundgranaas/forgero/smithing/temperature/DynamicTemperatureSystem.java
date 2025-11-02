package com.sigmundgranaas.forgero.smithing.temperature;

import net.minecraft.item.ItemStack;

public class DynamicTemperatureSystem {
    private static final int AMBIENT_TEMP = 20;
    private static final int REAL_CAP = 10000;

    public static class TemperatureStages {
        public final int ambient;
        public final int coldEnd;
        public final int warmEnd;
        public final int hotStart;
        public final int hotEnd;
        public final int overheatedStart;
        public final int max;

        public TemperatureStages(int ambient, int coldEnd, int warmEnd, int hotStart, int hotEnd, int overheatedStart, int max) {
            this.ambient = ambient;
            this.coldEnd = coldEnd;
            this.warmEnd = warmEnd;
            this.hotStart = hotStart;
            this.hotEnd = hotEnd;
            this.overheatedStart = overheatedStart;
            this.max = max;
        }
    }

    public enum TemperatureStage {
        COLD, WARM, HOT, OVERHEATED
    }

    /**
     * Calculate temperature stages dynamically based on max temp and workable range.
     */
    public static TemperatureStages calculateStages(int maxTemp, int workableStart, int workableEnd) {
        int effectiveMax = Math.min(Math.max(maxTemp, 1), REAL_CAP);

        // Validate workable range
        if (workableStart <= AMBIENT_TEMP || workableEnd <= workableStart || workableEnd > effectiveMax) {
            workableStart = 0;
            workableEnd = 0;
        }

        if (workableStart <= 0 || workableEnd <= 0) {
            // No custom workable range; use proportional stages
            int coldEnd = (int) (effectiveMax * 0.2);
            int warmEnd = (int) (effectiveMax * 0.5);
            int hotEnd = (int) (effectiveMax * 0.85);
            int overheatedStart = hotEnd;

            return new TemperatureStages(AMBIENT_TEMP, coldEnd, warmEnd, warmEnd, hotEnd, overheatedStart, effectiveMax);
        }

        // Custom workable range provided
        int range = workableEnd - workableStart;
        int coldEnd = Math.max(AMBIENT_TEMP + 1, workableStart - (int) (range * 0.5));
        int warmEnd = workableStart;
        int hotStart = workableStart;
        int hotEnd = workableEnd;
        int overheatedStart = workableEnd;

        return new TemperatureStages(AMBIENT_TEMP, coldEnd, warmEnd, hotStart, hotEnd, overheatedStart, effectiveMax);
    }

    /**
     * Calculate stages from an ItemStack using stored values.
     */
    public static TemperatureStages calculateStages(ItemStack stack) {
        int maxTemp = TemperatureUtils.getMaxTemp(stack);
        int workableStart = TemperatureUtils.getWorkableTemperatureStart(stack);
        int workableEnd = TemperatureUtils.getWorkableTemperatureEnd(stack);
        return calculateStages(maxTemp, workableStart, workableEnd);
    }

    /**
     * Determine which stage the current temperature falls into.
     */
    public static TemperatureStage getStage(int temperature, TemperatureStages stages) {
        if (temperature < stages.warmEnd) {
            return temperature < stages.coldEnd ? TemperatureStage.COLD : TemperatureStage.WARM;
        }
        if (temperature < stages.overheatedStart) {
            return TemperatureStage.HOT;
        }
        return TemperatureStage.OVERHEATED;
    }

    /**
     * Get color for a temperature with smooth interpolation using legacy color scale.
     */
    public static int getTemperatureColor(int temperature, TemperatureStages stages) {
        final int[][] colorScale = {
            {1600, 0xFFFFFF99},
            {1500, 0xFFFFFF66},
            {1400, 0xFFFFCC33},
            {1300, 0xFFFF9900},
            {1200, 0xFFFF6600},
            {1100, 0xFFFF3300},
            {1000, 0xFFFF0000},
            {900,  0xFFCC0000},
            {800,  0xFF990000},
            {700,  0xFF660000},
            {600,  0xFF330000},
            {500,  0xFF220000},
            {20,   0x00FFFFFF}
        };

        final int BASE_MAX = 1600;
        int effectiveMax = stages.max;

        int mappedMax = Math.round(colorScale[0][0] / (float) BASE_MAX * effectiveMax);
        if (temperature >= mappedMax) {
            return colorScale[0][1];
        }

        int mappedMin = Math.round(colorScale[colorScale.length - 1][0] / (float) BASE_MAX * effectiveMax);
        if (temperature <= mappedMin) {
            return colorScale[colorScale.length - 1][1];
        }

        for (int i = 0; i < colorScale.length - 1; i++) {
            int tHigh = Math.round(colorScale[i][0] / (float) BASE_MAX * effectiveMax);
            int tLow = Math.round(colorScale[i + 1][0] / (float) BASE_MAX * effectiveMax);
            int cHigh = colorScale[i][1];
            int cLow = colorScale[i + 1][1];

            if (tHigh == tLow) continue;
            if (temperature >= tLow && temperature <= tHigh) {
                float t = (temperature - tLow) / (float) (tHigh - tLow);
                t = Math.max(0f, Math.min(1f, t));
                return lerpColor(cLow, cHigh, t);
            }
        }

        return colorScale[colorScale.length - 1][1];
    }

    /**
     * Check if temperature is in workable range.
     */
    public static boolean isWorkable(int temperature, TemperatureStages stages) {
        return temperature >= stages.hotStart && temperature <= stages.hotEnd;
    }

    /**
     * Linearly interpolate between two colors.
     */
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

        int r = (int) (aR + (bR - aR) * t);
        int g = (int) (aG + (bG - aG) * t);
        int b = (int) (aB + (bB - aB) * t);
        int a = (int) (aA + (bA - aA) * t);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /**
     * Get HUD color based on temperature stage.
     */
    public static int getHudColor(TemperatureStage stage) {
        return switch (stage) {
            case COLD -> 0xFF000099;       // Dark blue
            case WARM -> 0xFFCCCC00;       // Dark yellow
            case HOT -> 0xFFCC6600;        // Dark orange
            case OVERHEATED -> 0xFFCC0000; // Dark red
        };
    }
}
