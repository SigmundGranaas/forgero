package com.sigmundgranaas.forgero.core.property.compiled;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.component.impl.StaticEquipment;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Proves a dagger's "sneak attack" shape: a dynamic-conditional attribute is compiled as
 * DATA — excluded from the baked stat value, carried in conditionalAttributes for the game
 * layer to evaluate. This is the core-side half of dynamic runtime attributes.
 */
class DynamicAttributeCarriageTest {

	/** A dynamic condition is opaque data to core; only the game layer evaluates it. */
	record SneakingMarker() implements DynamicCondition {
		public OpenIdentifier type() { return new OpenIdentifier("minecraft", "entity"); }
	}

	@Test
	void dynamicConditionalAttributeIsCarriedNotFolded() {
		Attribute base = new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 3f);
		Attribute sneakBonus = new SimpleAttribute(
				DefaultAttributes.ATTACK_DAMAGE, 4f,
				new Condition(List.of(), List.of(new SneakingMarker())));

		StaticEquipment dagger = StaticEquipment.create(
				new OpenIdentifier("forgero", "dagger"),
				Set.of(new OpenIdentifier("forgero", "weapon")),
				Map.of(Attribute.KEY.key(), List.of(base, sneakBonus)));

		var attackDamage = dagger.compiled().attributes().get(DefaultAttributes.ATTACK_DAMAGE);

		// Base value excludes the dynamic bonus (it is not folded at compile time).
		assertEquals(3f, attackDamage.value(), "base attack damage excludes the dynamic sneak bonus");
		// The dynamic bonus is carried as data for the game layer.
		assertEquals(1, attackDamage.conditionalAttributes().size(), "sneak bonus carried as conditional data");
		assertEquals(4f, attackDamage.conditionalAttributes().get(0).value());
	}
}
