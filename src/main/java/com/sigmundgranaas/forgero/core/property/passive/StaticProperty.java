package com.sigmundgranaas.forgero.core.property.passive;

public record StaticProperty(StaticPassiveType type) implements PassiveProperty, Static {
    @Override
    public PassivePropertyType getPassiveType() {
        return PassivePropertyType.STATIC;
    }

    @Override
    public StaticPassiveType getStaticType() {
        return type;
    }
}
