package com.sigmundgranaas.forgero.core.properties;

import com.sigmundgranaas.forgero.core.properties.attribute.Attribute;
import com.sigmundgranaas.forgero.core.properties.attribute.CalculationOrder;
import com.sigmundgranaas.forgero.core.properties.attribute.Target;
import com.sigmundgranaas.forgero.core.properties.attribute.TargetTag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class propertyTest {
    @Test
    void testProperty() {
        PropertyStream toolProperties = createTestProperties();
        Assertions.assertEquals(3f, toolProperties.applyAttribute(createDummyTarget(), AttributeType.MINING_SPEED));
    }

    private Target createDummyTarget() {
        return new Target() {
            @Override
            public TargetTypes getType() {
                return TargetTypes.BLOCK;
            }

            @Override
            public TargetTag getTag() {
                return new TargetTag(Set.of("STONE", "IRON"));
            }
        };
    }

    private PropertyStream createTestProperties() {
        Property prop1 = new Attribute() {
            @Override
            public PropertyTypes getType() {
                return PropertyTypes.ATTRIBUTES;
            }

            @Override
            public CalculationOrder getOrder() {
                return CalculationOrder.BASE;
            }

            @Override
            public AttributeType getAttributeType() {
                return AttributeType.MINING_SPEED;
            }

            @Override
            public Function<Float, Float> getCalculation() {
                return (value) -> value + 1;
            }
        };

        Property prop2 = new Attribute() {
            @Override
            public PropertyTypes getType() {
                return PropertyTypes.ATTRIBUTES;
            }

            @Override
            public CalculationOrder getOrder() {
                return CalculationOrder.END;
            }

            @Override
            public Predicate<Target> getCondition() {
                return (target ->
                        target.getType() == TargetTypes.BLOCK
                                && target.getTag().isApplicable("STONE")
                );
            }

            @Override
            public AttributeType getAttributeType() {
                return AttributeType.MINING_SPEED;
            }

            @Override
            public Function<Float, Float> getCalculation() {
                return (value) -> value * 1.5f;
            }
        };
        return Property.stream(List.of(prop1, prop1, prop2));
    }
}
