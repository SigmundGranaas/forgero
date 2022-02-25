package com.sigmundgranaas.forgero.core.properties;

import com.sigmundgranaas.forgero.core.properties.attribute.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class propertyTest {
    private final Function<Float, Float> EXAMPLE_CALCULATION = (current) -> current + 1;
    private final Predicate<Target> EXAMPLE_CONDITION = (target) -> true;

    @Test
    void testPropertyDamage() {
        PropertyStream toolProperties = createTestProperties();
        Assertions.assertEquals(9f, toolProperties.applyAttribute(createDummyTarget(TargetTypes.ENTITY, Set.of("HUMAN")), AttributeType.ATTACK_DAMAGE));
    }

    private PropertyStream createTestProperties() {
        Attribute exampleCalculation = createDefaultMiningSpeedAttribute();

        Attribute damageAttribute = new AttributeBuilder(AttributeType.ATTACK_DAMAGE)
                .applyCalculation((base) -> base + 1f)
                .applyOrder(CalculationOrder.BASE_MULTIPLICATION)
                .build();

        Attribute baseDamage = new AttributeBuilder(AttributeType.ATTACK_DAMAGE)
                .applyCalculation((base) -> base + 5f)
                .build();

        Attribute damageAttributeConditional = new AttributeBuilder(AttributeType.ATTACK_DAMAGE)
                .applyCalculation((base) -> base + base * 0.5f)
                .applyOrder(CalculationOrder.BASE_MULTIPLICATION)
                .applyCondition((target -> target.getType() == TargetTypes.ENTITY &&
                        target.getTag().isApplicable("HUMAN")))
                .build();

        return Property.stream(List.of(exampleCalculation, baseDamage, damageAttribute, damageAttributeConditional));
    }

    private Target createDummyTarget(TargetTypes type, Set<String> tags) {
        return new Target() {
            @Override
            public TargetTypes getType() {
                return type;
            }

            @Override
            public TargetTag getTag() {
                return new TargetTag(tags);
            }
        };
    }

    private Attribute createDefaultMiningSpeedAttribute() {
        return new AttributeBuilder(AttributeType.MINING_SPEED)
                .applyCalculation(EXAMPLE_CALCULATION)
                .applyOrder(CalculationOrder.BASE_MULTIPLICATION)
                .applyCondition(EXAMPLE_CONDITION)
                .build();
    }


}
