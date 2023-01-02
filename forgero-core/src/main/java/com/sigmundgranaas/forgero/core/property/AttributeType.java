package com.sigmundgranaas.forgero.core.property;

/**
 * Types for describing different attributes which can be improved by describing them in JSON.
 */
public enum AttributeType {
    ATTACK_SPEED,
    ATTACK_DAMAGE,
    MINING_SPEED,
    MINING_LEVEL,
    RARITY,
    DURABILITY;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
