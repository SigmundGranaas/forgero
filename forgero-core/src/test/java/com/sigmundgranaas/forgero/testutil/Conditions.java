package com.sigmundgranaas.forgero.testutil;

import java.util.ArrayList;
import java.util.List;

import com.sigmundgranaas.forgero.core.condition.NamedCondition;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.resource.data.PropertyPojo;
import com.sigmundgranaas.forgero.core.util.match.Matchable;


public class Conditions {
    public static NamedCondition SHARP;
    static {
        List<Property> properties = new ArrayList<>();
        properties.add(new PropertyPojo());
        SHARP = new NamedCondition(
            "sharp",
            "forgero",
            properties,
            Matchable.DEFAULT_TRUE
        );
    }
}
