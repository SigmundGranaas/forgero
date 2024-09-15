package com.sigmundgranaas.forgero.testutil;

import java.util.List;

import com.sigmundgranaas.forgero.core.condition.NamedCondition;


public class Conditions {
	public static NamedCondition SHARP = new NamedCondition("sharp", "forgero", List.of(Properties.ATTACK_DAMAGE_1));
}
