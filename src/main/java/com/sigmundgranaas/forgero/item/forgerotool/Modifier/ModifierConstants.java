package com.sigmundgranaas.forgero.item.forgerotool.Modifier;

public enum ModifierConstants {
    MINING_SPEED_MODIFIER("MiningSpeedModifier"),
    ATTACK_SPEED_MODIFIER("AttackSpeedModifier"),
    DURABILITY_MODIFIER("DurabilityModifier"),
    ATTACK_DAMAGE_MODIFIER("DurabilityModifier"),
    EMPTY_MODIFIER("EmptyModifier");


    private final String NbtName;

    ModifierConstants(String modifier) {
        this.NbtName = modifier;
    }

    public String getNbtName() {
        return NbtName;
    }
}
