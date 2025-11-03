package com.sigmundgranaas.forgero.smithing.temperature;

import com.sigmundgranaas.forgero.smithing.temperature.DynamicTemperatureSystem.TemperatureStages;

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
                TemperatureStages stages = DynamicTemperatureSystem.calculateStages(stack);
                return DynamicTemperatureSystem.getTemperatureColor(temp, stages);
            }, item);
        });
    }

    public static int[] getStageBoundaries(int maxTemp) {
        TemperatureStages stages = DynamicTemperatureSystem.calculateStages(maxTemp, 0, 0);
        return new int[]{stages.ambient, stages.coldEnd, stages.warmEnd, stages.hotStart, stages.hotEnd, stages.overheatedStart};
    }

    public static int[] getStageBoundariesWithWorkableRange(int maxTemp, int workableStart, int workableEnd) {
        TemperatureStages stages = DynamicTemperatureSystem.calculateStages(maxTemp, workableStart, workableEnd);
        return new int[]{stages.ambient, stages.coldEnd, stages.warmEnd, stages.hotStart, stages.hotEnd, stages.overheatedStart, stages.workableStart, stages.workableEnd};
    }

    public static boolean isInCold(int temperature, int maxTemp) {
        TemperatureStages stages = DynamicTemperatureSystem.calculateStages(maxTemp, 0, 0);
        return DynamicTemperatureSystem.getStage(temperature, stages) == DynamicTemperatureSystem.TemperatureStage.COLD;
    }

    public static boolean isInWarm(int temperature, int maxTemp) {
        TemperatureStages stages = DynamicTemperatureSystem.calculateStages(maxTemp, 0, 0);
        return DynamicTemperatureSystem.getStage(temperature, stages) == DynamicTemperatureSystem.TemperatureStage.WARM;
    }

    public static boolean isInHot(int temperature, int maxTemp) {
        TemperatureStages stages = DynamicTemperatureSystem.calculateStages(maxTemp, 0, 0);
        return DynamicTemperatureSystem.getStage(temperature, stages) == DynamicTemperatureSystem.TemperatureStage.HOT;
    }

    public static boolean isInWorkable(int temperature, int maxTemp, int workableStart, int workableEnd) {
        TemperatureStages stages = DynamicTemperatureSystem.calculateStages(maxTemp, workableStart, workableEnd);
        return DynamicTemperatureSystem.getStage(temperature, stages) == DynamicTemperatureSystem.TemperatureStage.WORKABLE;
    }

    public static boolean isInBrightHot(int temperature, int maxTemp) {
        TemperatureStages stages = DynamicTemperatureSystem.calculateStages(maxTemp, 0, 0);
        return DynamicTemperatureSystem.getStage(temperature, stages) == DynamicTemperatureSystem.TemperatureStage.HOT;
    }

    public static boolean isInOverheated(int temperature, int maxTemp) {
        TemperatureStages stages = DynamicTemperatureSystem.calculateStages(maxTemp, 0, 0);
        return DynamicTemperatureSystem.getStage(temperature, stages) == DynamicTemperatureSystem.TemperatureStage.OVERHEATED;
    }

    public static boolean isHotEnoughForWork(int temperature, int maxTemp, int workableStart, int workableEnd) {
        TemperatureStages stages = DynamicTemperatureSystem.calculateStages(maxTemp, workableStart, workableEnd);
        return DynamicTemperatureSystem.isWorkable(temperature, stages);
    }
}
