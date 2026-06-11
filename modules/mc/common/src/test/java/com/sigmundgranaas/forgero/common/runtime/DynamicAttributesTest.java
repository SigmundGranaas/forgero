package com.sigmundgranaas.forgero.common.runtime;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Proves the runtime half of dynamic attributes: the +4 sneak bonus applies only when the
 * (game-side) condition passes against context, and never enters core.
 */
class DynamicAttributesTest {

	private static final Key<Boolean> SNEAKING = new Key<>(new OpenIdentifier("test", "sneaking"));

	/** A game-side evaluable condition standing in for "source is sneaking". */
	record SneakingCondition() implements EvaluableCondition {
		public OpenIdentifier type() { return new OpenIdentifier("test", "sneaking"); }
		public boolean test(DynamicContext context) { return context.get(SNEAKING).orElse(false); }
	}

	private Attribute sneakBonus() {
		return new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 4f,
				new Condition(List.of(), List.of(new SneakingCondition())));
	}

	@Test
	void bonusAppliesWhenSneaking() {
		DynamicContext ctx = new DynamicContext.Builder().put(SNEAKING, true).build();
		assertEquals(4f, DynamicAttributes.apply(List.of(sneakBonus()), ctx));
	}

	@Test
	void noBonusWhenNotSneaking() {
		DynamicContext ctx = new DynamicContext.Builder().put(SNEAKING, false).build();
		assertEquals(0f, DynamicAttributes.apply(List.of(sneakBonus()), ctx));
	}
}
