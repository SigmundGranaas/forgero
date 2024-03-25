package com.sigmundgranaas.forgero.testutil;

import static com.sigmundgranaas.forgero.core.property.CalculationOrder.BASE;
import static com.sigmundgranaas.forgero.core.property.NumericOperation.ADDITION;

import com.sigmundgranaas.forgero.core.property.Attribute;
import com.sigmundgranaas.forgero.core.property.attribute.AttributeBuilder;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttackDamage;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Durability;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.MiningSpeed;

public class Properties {
	public static Attribute ATTACK_DAMAGE_1 = new AttributeBuilder(AttackDamage.KEY)
			.applyOrder(BASE)
			.applyOperation(ADDITION)
			.applyValue(1)
			.build();

	public static Attribute ATTACK_DAMAGE_10 = new AttributeBuilder(AttackDamage.KEY)
			.applyOrder(BASE)
			.applyOperation(ADDITION)
			.applyValue(10)
			.build();

	public static Attribute MINING_SPEED_10 = new AttributeBuilder(MiningSpeed.KEY)
			.applyOrder(BASE)
			.applyOperation(ADDITION)
			.applyValue(10)
			.build();
	public static Attribute DURABILITY_1000 = new AttributeBuilder(Durability.KEY)
			.applyOrder(BASE)
			.applyOperation(ADDITION)
			.applyValue(1000)
			.build();
}
