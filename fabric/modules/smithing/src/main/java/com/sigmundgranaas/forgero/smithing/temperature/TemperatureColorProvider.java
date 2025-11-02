package com.sigmundgranaas.forgero.smithing.temperature;

import com.sigmundgranaas.forgero.smithing.temperature2.DynamicTemperatureSystem;
import com.sigmundgranaas.forgero.smithing.temperature2.DynamicTemperatureSystem.TemperatureStages;

import net.minecraft.registry.Registries;

import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;

public class TemperatureColorProvider {
    private static final boolean USE_NEW_SYSTEM = true;

    public static void register() {
        Registries.ITEM.forEach(item -> {
            ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
                if (!TemperatureUtils.hasMaxTemperature(stack)) {
                    return 0xFFFFFF;
                }
                int temp = TemperatureUtils.getTemperature(stack);
                int max = TemperatureUtils.getMaxTemp(stack);

                if (USE_NEW_SYSTEM) {
                    TemperatureStages stages = DynamicTemperatureSystem.calculateStages(stack);
                    return DynamicTemperatureSystem.getTemperatureColor(temp, stages);
                } else {
                    return getHeatColor(temp, max);
                }
            }, item);
        });
    }

    public static int getHeatColor(int temperature, int maxTemp) {
        // Use dynamic temperature system for color calculation
        TemperatureStages stages = DynamicTemperatureSystem.calculateStages(maxTemp, 0, 0);
        return DynamicTemperatureSystem.getTemperatureColor(temperature, stages);
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
        TemperatureStages stages = DynamicTemperatureSystem.calculateStages(maxTemp, 0, 0);
        return new int[]{stages.ambient, stages.coldEnd, stages.warmEnd, stages.hotStart, stages.hotEnd, stages.overheatedStart};
    }

    public static int[] getStageBoundariesWithWorkableRange(int maxTemp, int workableStart, int workableEnd) {
        TemperatureStages stages = DynamicTemperatureSystem.calculateStages(maxTemp, workableStart, workableEnd);
        return new int[]{stages.ambient, stages.coldEnd, stages.warmEnd, stages.hotStart, stages.hotEnd, stages.overheatedStart};
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
        TemperatureStages stages = DynamicTemperatureSystem.calculateStages(maxTemp, 0, 0);
        DynamicTemperatureSystem.TemperatureStage stage = DynamicTemperatureSystem.getStage(temp, stages);
        return DynamicTemperatureSystem.getHudColor(stage);
    }
}
