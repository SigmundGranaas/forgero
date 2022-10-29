package com.sigmundgranaas.forgero.property.attribute;

import com.sigmundgranaas.forgero.property.PropertyContainer;

public class AttackDamage extends AbstractAttribute {
    public AttackDamage(PropertyContainer container) {
        super(container);
    }

    public static AttackDamage of(PropertyContainer container) {
        return new AttackDamage(container);
    }
}
