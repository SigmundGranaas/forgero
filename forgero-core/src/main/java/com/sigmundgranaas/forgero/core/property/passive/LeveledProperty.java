package com.sigmundgranaas.forgero.core.property.passive;

public record LeveledProperty(LeveledPassiveType passiveType) implements PassiveProperty {
    @Override
    public PassivePropertyType getPassiveType() {
        return PassivePropertyType.LEVELED;
    }

    @Override
    public String type() {
        return passiveType.toString();
    }
}
