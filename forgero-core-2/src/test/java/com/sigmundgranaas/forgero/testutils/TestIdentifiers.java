package com.sigmundgranaas.forgero.testutils;

import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

public class TestIdentifiers {
	private static final IdentifierFactory FACTORY = new IdentifierFactory.Builder().defaultNamespace("forgero").build();

	public static IdentifierFactory identifierFactory() {
		return FACTORY;
	}

	public static final OpenIdentifier DURABILITY_IDENTIFIER = FACTORY.of("durability");
	public static final OpenIdentifier ATTACK_DAMAGE_IDENTIFIER = FACTORY.of("attack_damage");
	public static final OpenIdentifier PICKAXE_HEAD_IDENTIFIER = FACTORY.of("pickaxe_head");
	public static final OpenIdentifier IRON_PICKAXE_HEAD_DURABILITY = FACTORY.of("iron-pickaxe_head-durability");
}
