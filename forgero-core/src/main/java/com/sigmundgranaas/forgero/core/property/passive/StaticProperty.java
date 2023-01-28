package com.sigmundgranaas.forgero.core.property.passive;

public record StaticProperty(StaticPassiveType passiveType) implements PassiveProperty, Static {
    @Override
    public PassivePropertyType getPassiveType() {
        return PassivePropertyType.STATIC;
    }

    @Override
    public StaticPassiveType getStaticType() {
        return passiveType;
    }

    @Override
    public String type() {
        return passiveType.toString();
    }
}
