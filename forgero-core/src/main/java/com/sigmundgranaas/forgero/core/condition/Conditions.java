package com.sigmundgranaas.forgero.core.condition;

import com.sigmundgranaas.forgero.core.property.AttributeType;
import com.sigmundgranaas.forgero.core.property.CalculationOrder;
import com.sigmundgranaas.forgero.core.property.NumericOperation;
import com.sigmundgranaas.forgero.core.property.attribute.AttributeBuilder;
import com.sigmundgranaas.forgero.core.property.attribute.Category;
import com.sigmundgranaas.forgero.core.property.v2.feature.PropertyData;

import java.util.List;
import java.util.Optional;

public class Conditions {
    public static String BROKEN_TYPE_KEY = "BROKEN";

    public static String RARE_TYPE_KEY = "RARE";
    public static NamedCondition BROKEN = new NamedCondition("broken", List.of(PropertyData.builder().type(BROKEN_TYPE_KEY).build()));

    public static NamedCondition RARE = new NamedCondition("rare", List.of(new AttributeBuilder(AttributeType.RARITY).applyValue(10).applyCategory(Category.ALL).applyOperation(NumericOperation.ADDITION).applyOrder(CalculationOrder.END).build()));

    public static Optional<NamedCondition> of(String name) {
        if (name.equals(BROKEN.name())) {
            return Optional.of(BROKEN);
        } else if (name.equals(RARE.name())) {
            return Optional.of(RARE);
        }
        return Optional.empty();
    }
}
