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
        public final int workableStart;
        public final int workableEnd;
        public final int max;

        public TemperatureStages(int ambient, int coldEnd, int warmEnd, int hotStart, int hotEnd, int overheatedStart, int workableStart, int workableEnd, int max) {
            this.ambient = ambient;
            this.coldEnd = coldEnd;
            this.warmEnd = warmEnd;
            this.hotStart = hotStart;
            this.hotEnd = hotEnd;
            this.overheatedStart = overheatedStart;
            this.workableStart = workableStart;
            this.workableEnd = workableEnd;
            this.max = max;
        }
    }

    public enum TemperatureStage {
        COLD, WARM, HOT, WORKABLE, OVERHEATED
    }

    public static TemperatureStages calculateStages(int maxTemp, int workableStart, int workableEnd) {
        int effectiveMax = Math.min(Math.max(maxTemp, 1), REAL_CAP);

        if (workableStart <= AMBIENT_TEMP || workableEnd <= workableStart || workableEnd > effectiveMax) {
            workableStart = 0;
            workableEnd = 0;
        }

        if (workableStart <= 0 || workableEnd <= 0) {
            int coldEnd = (int) (effectiveMax * 0.25);
            int warmEnd = (int) (effectiveMax * 0.55);
            int hotEnd = (int) (effectiveMax * 0.85);
            int overheatedStart = hotEnd;

            return new TemperatureStages(AMBIENT_TEMP, coldEnd, warmEnd, warmEnd, hotEnd, overheatedStart, 0, 0, effectiveMax);
        }

        int coldEnd = (int) (effectiveMax * 0.25);
        int warmEnd = (int) (effectiveMax * 0.55);
        int hotStart = (int) (effectiveMax * 0.55);
        int hotEnd = (int) (effectiveMax * 0.85);
        int overheatedStart = (int) (effectiveMax * 0.85);

        return new TemperatureStages(AMBIENT_TEMP, coldEnd, warmEnd, hotStart, hotEnd, overheatedStart, workableStart, workableEnd, effectiveMax);
    }

    public static TemperatureStages calculateStages(ItemStack stack) {
        int maxTemp = TemperatureUtils.getMaxTemp(stack);
        int workableStart = TemperatureUtils.getWorkableTemperatureStart(stack);
        int workableEnd = TemperatureUtils.getWorkableTemperatureEnd(stack);
        return calculateStages(maxTemp, workableStart, workableEnd);
    }

    public static TemperatureStage getStage(int temperature, TemperatureStages stages) {
        if (isWorkable(temperature, stages)) {
            return TemperatureStage.WORKABLE;
        }
        if (temperature < stages.coldEnd) {
            return TemperatureStage.COLD;
        }
        if (temperature < stages.warmEnd) {
            return TemperatureStage.WARM;
        }
        if (temperature < stages.hotEnd) {
            return TemperatureStage.HOT;
        }
        if (temperature >= stages.overheatedStart) {
            return TemperatureStage.OVERHEATED;
        }
        return TemperatureStage.HOT;
    }

    public static int getTemperatureColor(int temperature, TemperatureStages stages) {
        final int COLOR_RED = 0xFFFF0000;
        final int COLOR_ORANGE_RED = 0xFFFF3300;
        final int COLOR_ORANGE = 0xFFFF9900;
        final int COLOR_YELLOW = 0xFFFFCC33;
        final int COLOR_PALE = 0xFFFFFF99;
        final int COLOR_AMBIENT = 0x00FFFFFF;

        if (stages.workableStart > 0 && stages.workableEnd > 0) {
            // Build dynamic scale with orange only in workable range
            int[][] colorScale = {
                {stages.max, COLOR_RED},
                {stages.workableEnd, COLOR_ORANGE_RED},
                {stages.workableStart, COLOR_YELLOW},
                {stages.warmEnd, COLOR_YELLOW},
                {stages.coldEnd, COLOR_PALE},
                {stages.ambient, COLOR_AMBIENT}
            };

            if (temperature >= colorScale[0][0]) {
                return colorScale[0][1];
            }

            if (temperature <= colorScale[colorScale.length - 1][0]) {
                return colorScale[colorScale.length - 1][1];
            }

            if (temperature >= stages.workableStart && temperature <= stages.workableEnd) {
                float workableProgress = (temperature - stages.workableStart) / (float) (stages.workableEnd - stages.workableStart);
                workableProgress = Math.max(0f, Math.min(1f, workableProgress));

                if (workableProgress < 0.5f) {
                    float t = workableProgress * 2;
                    return lerpColor(COLOR_YELLOW, COLOR_ORANGE, t);
                } else {
                    float t = (workableProgress - 0.5f) * 2;
                    return lerpColor(COLOR_ORANGE, COLOR_ORANGE_RED, t);
                }
            }

            for (int i = 0; i < colorScale.length - 1; i++) {
                int tHigh = colorScale[i][0];
                int tLow = colorScale[i + 1][0];
                int cHigh = colorScale[i][1];
                int cLow = colorScale[i + 1][1];

                if (tHigh != tLow && temperature >= tLow && temperature <= tHigh) {
                    float t = (temperature - tLow) / (float) (tHigh - tLow);
                    t = Math.max(0f, Math.min(1f, t));
                    return lerpColor(cLow, cHigh, t);
                }
            }
        } else {
            int[][] colorScale = {
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

            if (temperature >= colorScale[0][0]) {
                return colorScale[0][1];
            }

            if (temperature <= colorScale[colorScale.length - 1][0]) {
                return colorScale[colorScale.length - 1][1];
            }

            for (int i = 0; i < colorScale.length - 1; i++) {
                int tHigh = colorScale[i][0];
                int tLow = colorScale[i + 1][0];
                int cHigh = colorScale[i][1];
                int cLow = colorScale[i + 1][1];

                if (tHigh != tLow && temperature >= tLow && temperature <= tHigh) {
                    float t = (temperature - tLow) / (float) (tHigh - tLow);
                    t = Math.max(0f, Math.min(1f, t));
                    return lerpColor(cLow, cHigh, t);
                }
            }

            return colorScale[colorScale.length - 1][1];
        }

        return COLOR_AMBIENT;
    }

    public static boolean isWorkable(int temperature, TemperatureStages stages) {
        return stages.workableStart > 0 && stages.workableEnd > 0 && temperature >= stages.workableStart && temperature <= stages.workableEnd;
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

        int r = (int) (aR + (bR - aR) * t);
        int g = (int) (aG + (bG - aG) * t);
        int b = (int) (aB + (bB - aB) * t);
        int a = (int) (aA + (bA - aA) * t);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int getHudColor(TemperatureStage stage) {
        return switch (stage) {
			case COLD -> 0xFF2196F3;
			case WARM -> 0xFFFFEB3B;
			case HOT -> 0xFFFF9800;
			case WORKABLE -> 0xFF4CAF50;
			case OVERHEATED -> 0xFFF44336;
        };
    }
}
