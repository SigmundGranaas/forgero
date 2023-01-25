package com.sigmundgranaas.forgero.core.condition;

import com.sigmundgranaas.forgero.core.property.v2.feature.PropertyData;

import java.util.List;
import java.util.Optional;

public class Conditions {
    public static String BROKEN_TYPE_KEY = "BROKEN";
    public static NamedCondition BROKEN = new NamedCondition("broken", List.of(PropertyData.builder().type(BROKEN_TYPE_KEY).build()));

    public static Optional<NamedCondition> of(String name) {
        if (name.equals(BROKEN.name())) {
            return Optional.of(BROKEN);
        }
        return Optional.empty();
    }
}
