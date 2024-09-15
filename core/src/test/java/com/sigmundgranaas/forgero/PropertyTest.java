package com.sigmundgranaas.forgero;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import com.sigmundgranaas.forgero.core.property.Attribute;
import com.sigmundgranaas.forgero.core.property.CalculationOrder;
import com.sigmundgranaas.forgero.core.property.NumericOperation;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.PropertyStream;
import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.property.attribute.AttributeBuilder;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttackDamage;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Durability;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.MiningSpeed;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PropertyTest {
	private final Function<Float, Float> EXAMPLE_CALCULATION = (current) -> current + 1;
	private final Predicate<Target> EXAMPLE_CONDITION = (target) -> true;

	@Test
	void testPropertyDamage() {
		PropertyStream toolProperties = createTestProperties();
		Assertions.assertEquals(30f, toolProperties.applyAttribute(AttackDamage.KEY));
	}

	@Test
	void testPropertyDurability() {
		PropertyStream toolProperties = createTestProperties();
		Assertions.assertEquals(2000, toolProperties.applyAttribute(Durability.KEY));
	}

	private PropertyStream createTestProperties() {
		Attribute exampleCalculation = createDefaultMiningSpeedAttribute();

		Attribute damageAttribute = new AttributeBuilder(AttackDamage.KEY)
				.applyValue(1)
				.applyOperation(NumericOperation.ADDITION)
				.applyOrder(CalculationOrder.BASE_MULTIPLICATION)
				.build();

		Attribute baseDamage = new AttributeBuilder(AttackDamage.KEY)
				.applyValue(5)
				.applyOperation(NumericOperation.ADDITION)
				.applyOrder(CalculationOrder.BASE_MULTIPLICATION)
				.build();

		Attribute damageAttributeConditional = new AttributeBuilder(AttackDamage.KEY)
				.applyValue(5)
				.applyOperation(NumericOperation.MULTIPLICATION)
				.applyOrder(CalculationOrder.BASE_MULTIPLICATION)
				.build();

		Attribute durabilityAttribute = new AttributeBuilder(Durability.KEY)
				.applyValue(1000)
				.applyOperation(NumericOperation.ADDITION)
				.applyOrder(CalculationOrder.BASE)
				.build();

		Attribute durabilityAttributeConditional = new AttributeBuilder(Durability.KEY)
				.applyValue(1000)
				.applyOperation(NumericOperation.ADDITION)
				.applyOrder(CalculationOrder.MIDDLE)
				.build();

		return Property.stream(List.of(exampleCalculation, baseDamage, damageAttribute, damageAttributeConditional, durabilityAttribute, durabilityAttributeConditional));
	}


	private Attribute createDefaultMiningSpeedAttribute() {
		return new AttributeBuilder(MiningSpeed.KEY)
				.applyOrder(CalculationOrder.BASE_MULTIPLICATION)
				.build();
	}
}
