package com.sigmundgranaas.forgero.smithing.condition;

import java.util.List;
import java.util.Random;

import com.sigmundgranaas.forgero.core.condition.Conditions;
import com.sigmundgranaas.forgero.core.condition.NamedCondition;

public class ConditionLootTables {
    public static final List<NamedCondition> BEST = List.of(
        Conditions.UNBREAKABLE

    );

	public static final List<NamedCondition> GOOD = List.of(
			// Add neutral conditions here, currently using UNBREAKABLE as a placeholder
			Conditions.UNBREAKABLE
	);

    public static final List<NamedCondition> NEUTRAL = List.of(
        Conditions.INSTANCE.of("forgero:carved").orElse(null),
        Conditions.INSTANCE.of("forgero:durable").orElse(null),
        Conditions.INSTANCE.of("forgero:hardened").orElse(null),
        Conditions.INSTANCE.of("forgero:honed").orElse(null),
        Conditions.INSTANCE.of("forgero:light").orElse(null),
        Conditions.INSTANCE.of("forgero:lucky").orElse(null),
        Conditions.INSTANCE.of("forgero:mighty").orElse(null),
        Conditions.INSTANCE.of("forgero:nimble").orElse(null),
        Conditions.INSTANCE.of("forgero:protective").orElse(null),
        Conditions.INSTANCE.of("forgero:quick").orElse(null),
        Conditions.INSTANCE.of("forgero:rapid").orElse(null),
        Conditions.INSTANCE.of("forgero:rare").orElse(null),
        Conditions.INSTANCE.of("forgero:sharp").orElse(null),
        Conditions.INSTANCE.of("forgero:sturdy").orElse(null),
        Conditions.INSTANCE.of("forgero:swift").orElse(null),
        Conditions.INSTANCE.of("forgero:tempered").orElse(null),
        Conditions.INSTANCE.of("forgero:tough").orElse(null),
        Conditions.INSTANCE.of("forgero:trimmed").orElse(null),
        Conditions.INSTANCE.of("forgero:unbreakable").orElse(null)
    ).stream().filter(java.util.Objects::nonNull).toList();

    public static final List<NamedCondition> BAD = List.of(
        Conditions.INSTANCE.of("forgero:pig_affinity").orElse(null)
    ).stream().filter(java.util.Objects::nonNull).toList();

    private static final Random RANDOM = new Random();

    public static NamedCondition getRandomCondition(List<NamedCondition> table) {
        if (table.isEmpty()) return null;
        return table.get(RANDOM.nextInt(table.size()));
    }
}
