package com.sigmundgranaas.forgero.testutil;

import com.sigmundgranaas.forgero.property.Attribute;
import com.sigmundgranaas.forgero.property.attribute.AttributeBuilder;

import static com.sigmundgranaas.forgero.property.AttributeType.*;
import static com.sigmundgranaas.forgero.property.CalculationOrder.BASE;
import static com.sigmundgranaas.forgero.property.NumericOperation.ADDITION;

public class Properties {
    public static Attribute ATTACK_DAMAGE_1 = new AttributeBuilder(ATTACK_DAMAGE, category)
            .applyOrder(BASE)
            .applyOperation(ADDITION)
            .applyValue(1)
            .build();

    public static Attribute ATTACK_DAMAGE_10 = new AttributeBuilder(ATTACK_DAMAGE, category)
            .applyOrder(BASE)
            .applyOperation(ADDITION)
            .applyValue(10)
            .build();

    public static Attribute MINING_SPEED_10 = new AttributeBuilder(MINING_SPEED, category)
            .applyOrder(BASE)
            .applyOperation(ADDITION)
            .applyValue(10)
            .build();
    public static Attribute DURABILITY_1000 = new AttributeBuilder(DURABILITY, category)
            .applyOrder(BASE)
            .applyOperation(ADDITION)
            .applyValue(1000)
            .build();
}
