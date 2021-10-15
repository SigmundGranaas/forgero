package com.sigmundgranaas.forgero.item.forgerotool.Modifier;

import net.minecraft.nbt.NbtCompound;

import java.util.Map;

public interface ForgeroModifier {

    static ForgeroModifier createForgeroModifier(ModifierConstants modifierType, Map<String, Float> attributes) {
        return switch (modifierType) {
            case MINING_SPEED_MODIFIER -> MiningSpeedModifier.createMiningSpeedModifier(attributes);
            //TODO Create more modifiers
            case DURABILITY_MODIFIER -> MiningSpeedModifier.createMiningSpeedModifier(attributes);
            default -> new EmptyModifier();
        };
    }

    void addModifierToNbt(NbtCompound element);

    NbtCompound getModifierNbt();

    NbtCompound createModifierNbt(NbtCompound parentElement);

    Map<String, Float> getModifierAttributes();
}
