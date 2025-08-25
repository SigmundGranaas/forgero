package com.sigmundgranaas.forgero.smithing.condition;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sigmundgranaas.forgero.core.condition.Conditions;
import com.sigmundgranaas.forgero.core.condition.NamedCondition;

import net.minecraft.nbt.NbtCompound;

public class NbtConditionLootRegistry {
	private static final Map<String, List<NamedCondition>> NBT_LOOT_MAP = new HashMap<>();
	private static final Map<String, NamedCondition> NBT_CONDITION_MAP = new HashMap<>();

	public static final List<NamedCondition> NEUTRAL = List.of(
			//com.sigmundgranaas.forgero.core.condition.Conditions.INSTANCE.of("forgero:engraved").orElse(null),
			//com.sigmundgranaas.forgero.core.condition.Conditions.INSTANCE.of("forgero:reinforced").orElse(null),
			//com.sigmundgranaas.forgero.core.condition.Conditions.INSTANCE.of("forgero:hardened").orElse(null),
			//com.sigmundgranaas.forgero.core.condition.Conditions.INSTANCE.of("forgero:honed").orElse(null),
			//com.sigmundgranaas.forgero.core.condition.Conditions.INSTANCE.of("forgero:lightweight").orElse(null),
			//com.sigmundgranaas.forgero.core.condition.Conditions.INSTANCE.of("forgero:lucky").orElse(null),
			//com.sigmundgranaas.forgero.core.condition.Conditions.INSTANCE.of("forgero:mighty").orElse(null),
			//com.sigmundgranaas.forgero.core.condition.Conditions.INSTANCE.of("forgero:nimble").orElse(null),
			//com.sigmundgranaas.forgero.core.condition.Conditions.INSTANCE.of("forgero:guarded").orElse(null),
			//com.sigmundgranaas.forgero.core.condition.Conditions.INSTANCE.of("forgero:quick").orElse(null),
			//com.sigmundgranaas.forgero.core.condition.Conditions.INSTANCE.of("forgero:rapid").orElse(null),
			//com.sigmundgranaas.forgero.core.condition.Conditions.INSTANCE.of("forgero:rare").orElse(null),
			//com.sigmundgranaas.forgero.core.condition.Conditions.INSTANCE.of("forgero:sharp").orElse(null),
			//com.sigmundgranaas.forgero.core.condition.Conditions.INSTANCE.of("forgero:sturdy").orElse(null),
			//com.sigmundgranaas.forgero.core.condition.Conditions.INSTANCE.of("forgero:swift").orElse(null),
			//com.sigmundgranaas.forgero.core.condition.Conditions.INSTANCE.of("forgero:tempered").orElse(null),
			com.sigmundgranaas.forgero.core.condition.Conditions.INSTANCE.of("forgero:resilient").orElse(null)
			//com.sigmundgranaas.forgero.core.condition.Conditions.INSTANCE.of("forgero:trimmed").orElse(null),
			//com.sigmundgranaas.forgero.core.condition.Conditions.INSTANCE.of("forgero:unbreakable").orElse(null)
	).stream().filter(java.util.Objects::nonNull).toList();

	static {
		NBT_CONDITION_MAP.put("started_in_nether", Conditions.INSTANCE.of("forgero:netherborn").orElse(null));
	}

	public static List<NamedCondition> getLootTable(NbtCompound nbt) {
		for (String key : NBT_LOOT_MAP.keySet()) {
			if (nbt.contains(key)) {
				return NBT_LOOT_MAP.get(key);
			}
		}
		return Collections.emptyList();
	}

	public static NamedCondition getCondition(NbtCompound nbt) {
		for (String key : NBT_CONDITION_MAP.keySet()) {
			if (nbt.contains(key)) {
				return NBT_CONDITION_MAP.get(key);
			}
		}
		return null;
	}

	public static void register(String nbtKey, List<NamedCondition> lootTable) {
		NBT_LOOT_MAP.put(nbtKey, lootTable);
	}
}
