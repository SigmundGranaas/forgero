package com.sigmundgranaas.forgero.core.property.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.attribute.computation.ComputationChain;
import com.sigmundgranaas.forgero.core.property.condition.Condition;
import com.sigmundgranaas.forgero.core.attribute.computation.operator.AdditionOperator;
import com.sigmundgranaas.forgero.core.attribute.computation.operator.MultiplicationOperator;
import java.util.List;
import org.junit.jupiter.api.Test;

class ComputationChainTest {

	@Test
	void computesCorrectValueWithSimpleAddition() {
		var attributes = List.of(
				new Attribute(DefaultAttributes.ATTACK_DAMAGE, 5f),
				new Attribute(DefaultAttributes.ATTACK_DAMAGE, 3f)
		);
		var chain = new ComputationChain(attributes);
		assertEquals(8f, chain.compute(0f));
	}

	@Test
	void computesCorrectValueWithMixedOperatorsAndLevels() {
		var attributes = List.of(
				// These should be applied out of order to test sorting by level
				new Attribute(DefaultAttributes.ATTACK_DAMAGE, 1.2f, MultiplicationOperator.getInstance(), 1, Condition.ALWAYS_TRUE), // Second
				new Attribute(DefaultAttributes.ATTACK_DAMAGE, 5f, AdditionOperator.getInstance(), 0, Condition.ALWAYS_TRUE),       // First
				new Attribute(DefaultAttributes.ATTACK_DAMAGE, -1f, AdditionOperator.getInstance(), 2, Condition.ALWAYS_TRUE)      // Third
		);
		var chain = new ComputationChain(attributes);

		// Calculation should be: (0 + 5) * 1.2 - 1 = 6 - 1 = 5
		assertEquals(5f, chain.compute(0f));
	}

	@Test
	void handlesEmptyAttributeList() {
		var chain = new ComputationChain(List.of());
		assertEquals(10f, chain.compute(10f));
	}

	@Test
	void usesBuilderCorrectly() {
		var builder = new ComputationChain.Builder();
		builder.addAttribute(new Attribute(DefaultAttributes.ATTACK_DAMAGE, 10f));
		builder.addAttribute(new Attribute(DefaultAttributes.ATTACK_DAMAGE, 2f, MultiplicationOperator.getInstance(), 1, Condition.ALWAYS_TRUE));
		var chain = builder.build();

		// Calculation: (0 + 10) * 2 = 20
		assertEquals(20f, chain.compute(0f));
	}
}
