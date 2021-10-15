package com.sigmundgranaas.forgero.item.forgerotool.Modifier;

import net.minecraft.nbt.NbtCompound;

import java.util.HashMap;
import java.util.Map;

public class EmptyModifier implements ForgeroModifier {

    @Override
    public void addModifierToNbt(NbtCompound element) {
        element.putBoolean(ModifierConstants.EMPTY_MODIFIER.getNbtName(), true);
    }

    @Override
    public NbtCompound getModifierNbt() {
        return new NbtCompound();
    }

    @Override
    public NbtCompound createModifierNbt(NbtCompound parentElement) {
        return parentElement;
    }

    @Override
    public Map<String, Float> getModifierAttributes() {
        return new HashMap<String, Float>();
    }
}
