package com.sigmundgranaas.forgero.item.forgerotool.Modifier;

import net.minecraft.nbt.NbtCompound;

import java.util.Map;

public class MiningSpeedModifier implements ForgeroModifier {
    static final String MINING_SPEED_IDENTIFIER = "MiningSpeed";
    static final String MINING_SPEED_STRENGTH_IDENTIFIER = "ModifierStrength";
    static final String NBT_KEY = ModifierConstants.MINING_SPEED_MODIFIER.getNbtName();
    private final float miningSpeedModifier;
    private final float miningSpeedStrength;


    public MiningSpeedModifier() {
        this.miningSpeedModifier = 1;
        this.miningSpeedStrength = 1;
    }

    public MiningSpeedModifier(float miningSpeedModifier, float miningSpeedStrength) {
        this.miningSpeedModifier = miningSpeedModifier;
        this.miningSpeedStrength = miningSpeedStrength;
    }

    public static MiningSpeedModifier createMiningSpeedModifier(Map<String, Float> attributes) {
        Float miningSpeedModifier = attributes.get(MINING_SPEED_IDENTIFIER);
        Float miningStrengthModifier = attributes.get(MINING_SPEED_STRENGTH_IDENTIFIER);
        if (miningSpeedModifier == null || miningStrengthModifier == null) {
            return new MiningSpeedModifier();
        } else {
            return new MiningSpeedModifier(miningSpeedModifier, miningStrengthModifier);
        }
    }

    public static MiningSpeedModifier createMiningSpeedModifier(NbtCompound nbtModifier) {
        float miningSpeedModifier = nbtModifier.getFloat(MINING_SPEED_IDENTIFIER);
        float miningStrengthModifier = nbtModifier.getFloat(MINING_SPEED_STRENGTH_IDENTIFIER);
        if (miningSpeedModifier == 0.0F || miningStrengthModifier == 0.0F) {
            return new MiningSpeedModifier();
        } else {
            return new MiningSpeedModifier(miningSpeedModifier, miningStrengthModifier);
        }
    }


    @Override
    public NbtCompound createModifierNbt(NbtCompound parentElement) {
        NbtCompound element = new NbtCompound();
        element.putFloat(MINING_SPEED_IDENTIFIER, miningSpeedModifier);
        element.putFloat(MINING_SPEED_STRENGTH_IDENTIFIER, miningSpeedStrength);
        parentElement.put(NBT_KEY, element);
        return parentElement;
    }

    @Override
    public Map<String, Float> getModifierAttributes() {
        return null;
    }

    @Override
    public void addModifierToNbt(NbtCompound element) {

    }

    @Override
    public NbtCompound getModifierNbt() {
        return null;
    }
}
