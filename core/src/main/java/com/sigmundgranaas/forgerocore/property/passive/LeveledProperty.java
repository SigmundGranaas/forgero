package com.sigmundgranaas.forgerocore.property.passive;

public record LeveledProperty(LeveledPassiveType type) implements PassiveProperty {
    @Override
    public PassivePropertyType getPassiveType() {
        return PassivePropertyType.LEVELED;
    }
}
