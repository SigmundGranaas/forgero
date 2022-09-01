package com.sigmundgranaas.forgero.core.testutil;

import com.sigmundgranaas.forgero.core.property.Attribute;
import com.sigmundgranaas.forgero.core.property.attribute.AttributeBuilder;

import static com.sigmundgranaas.forgero.core.property.AttributeType.ATTACK_DAMAGE;
import static com.sigmundgranaas.forgero.core.property.CalculationOrder.BASE;
import static com.sigmundgranaas.forgero.core.property.NumericOperation.ADDITION;

public class Properties {
    public static Attribute ATTACK_DAMAGE_1 = new AttributeBuilder(ATTACK_DAMAGE)
            .applyOrder(BASE)
            .applyOperation(ADDITION)
            .applyValue(1)
            .build();
}
