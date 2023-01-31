package com.sigmundgranaas.forgero.core.soul.level;

import com.sigmundgranaas.forgero.core.property.CalculationOrder;
import com.sigmundgranaas.forgero.core.property.NumericOperation;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.attribute.AttributeBuilder;
import com.sigmundgranaas.forgero.core.soul.PropertyLevelProvider;

import java.util.List;

public class LinearAddition implements PropertyLevelProvider {
    private final String key;

    private final float value;


    public LinearAddition(String key) {
        this.key = key;
        this.value = 1f;
    }

    public LinearAddition(String key, float value) {
        this.key = key;
        this.value = value;
    }

    public static PropertyLevelProvider of(String key) {
        return new LinearAddition(key);
    }

    public static PropertyLevelProvider of(String key, float value) {
        return new LinearAddition(key, value);
    }

    @Override
    public List<Property> apply(Integer level) {
        Property prop = new AttributeBuilder(key)
                .applyOperation(NumericOperation.ADDITION)
                .applyValue(value)
                .applyLevel(level)
                .applyOrder(CalculationOrder.MIDDLE)
                .build();
        return List.of(prop);
    }
}
