package com.sigmundgranaas.forgeroforge.testutil;

import static com.sigmundgranaas.forgeroforge.testutil.Properties.ATTACK_DAMAGE_1;

import java.util.List;

import com.sigmundgranaas.forgero.core.condition.NamedCondition;


public class Conditions {
	public static NamedCondition SHARP = new NamedCondition("sharp", "forgero", List.of(ATTACK_DAMAGE_1));
}
