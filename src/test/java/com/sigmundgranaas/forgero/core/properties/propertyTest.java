package com.sigmundgranaas.forgero.core.properties;

import com.sigmundgranaas.forgero.core.properties.attribute.AttributeBuilder;
import com.sigmundgranaas.forgero.core.properties.attribute.SingleTarget;
import com.sigmundgranaas.forgero.core.properties.attribute.Target;
import com.sigmundgranaas.forgero.core.properties.attribute.ToolPartTarget;
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

    @Test
    void testPropertyDurability() {
        PropertyStream toolProperties = createTestProperties();
        Target attributeTarget = new ToolPartTarget(Set.of("HANDLE"));
        Assertions.assertEquals(2000, toolProperties.applyAttribute(attributeTarget, AttributeType.DURABILITY));
    }

    private PropertyStream createTestProperties() {
        Attribute exampleCalculation = createDefaultMiningSpeedAttribute();

        Attribute damageAttribute = new AttributeBuilder(AttributeType.ATTACK_DAMAGE)
                .applyValue(1)
                .applyOperation(NumericOperation.ADDITION)
                .applyOrder(CalculationOrder.BASE_MULTIPLICATION)
                .build();

        Attribute baseDamage = new AttributeBuilder(AttributeType.ATTACK_DAMAGE)
                .applyValue(5)
                .applyOperation(NumericOperation.ADDITION)
                .applyOrder(CalculationOrder.BASE_MULTIPLICATION)
                .build();

        Attribute damageAttributeConditional = new AttributeBuilder(AttributeType.ATTACK_DAMAGE)
                .applyValue(5)
                .applyOperation(NumericOperation.MULTIPLICATION)
                .applyOrder(CalculationOrder.BASE_MULTIPLICATION)
                .applyCondition((Target target) -> target.isApplicable(Set.of("HUMAN"), TargetTypes.ENTITY))
                .build();

        Attribute durabilityAttribute = new AttributeBuilder(AttributeType.DURABILITY)
                .applyValue(1000)
                .applyOperation(NumericOperation.ADDITION)
                .applyOrder(CalculationOrder.BASE)
                .build();

        Attribute durabilityAttributeConditional = new AttributeBuilder(AttributeType.DURABILITY)
                .applyValue(1000)
                .applyOperation(NumericOperation.ADDITION)
                .applyOrder(CalculationOrder.MIDDLE)
                .applyCondition((Target target) -> target.isApplicable(Set.of("HANDLE"), TargetTypes.TOOL_PART_TYPE))
                .build();

        return Property.stream(List.of(exampleCalculation, baseDamage, damageAttribute, damageAttributeConditional, durabilityAttribute, durabilityAttributeConditional));
    }

    private Target createDummyTarget(TargetTypes type, Set<String> tags) {
        return new SingleTarget(type, tags);
    }

    private Attribute createDefaultMiningSpeedAttribute() {
        return new AttributeBuilder(AttributeType.MINING_SPEED)
                .applyOrder(CalculationOrder.BASE_MULTIPLICATION)
                .applyCondition(EXAMPLE_CONDITION)
                .build();
    }
}
