package com.sigmundgranaas.forgerocore.testutil;

import com.sigmundgranaas.forgerocore.property.Attribute;
import com.sigmundgranaas.forgerocore.property.attribute.AttributeBuilder;

import static com.sigmundgranaas.forgerocore.property.AttributeType.*;
import static com.sigmundgranaas.forgerocore.property.CalculationOrder.BASE;
import static com.sigmundgranaas.forgerocore.property.NumericOperation.ADDITION;

public class Properties {
    public static Attribute ATTACK_DAMAGE_1 = new AttributeBuilder(ATTACK_DAMAGE)
            .applyOrder(BASE)
            .applyOperation(ADDITION)
            .applyValue(1)
            .build();

    public static Attribute ATTACK_DAMAGE_10 = new AttributeBuilder(ATTACK_DAMAGE)
            .applyOrder(BASE)
            .applyOperation(ADDITION)
            .applyValue(10)
            .build();

    public static Attribute MINING_SPEED_10 = new AttributeBuilder(MINING_SPEED)
            .applyOrder(BASE)
            .applyOperation(ADDITION)
            .applyValue(10)
            .build();
    public static Attribute DURABILITY_1000 = new AttributeBuilder(DURABILITY)
            .applyOrder(BASE)
            .applyOperation(ADDITION)
            .applyValue(1000)
            .build();
}
