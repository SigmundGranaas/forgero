package com.sigmundgranaas.forgero.testutil;

import com.sigmundgranaas.forgero.core.state.Upgrade;

import java.util.List;

public class Upgrades {
	public static Upgrade REDSTONE_GEM = Upgrade.of("redstone-gem", Types.GEM, List.of(Properties.ANY_ATTACK_DAMAGE_10));
	public static Upgrade IRON = Upgrade.of("iron", Types.METAL, List.of(Properties.ATTACK_DAMAGE_1));

	public static Upgrade LEATHER = Upgrade.of("leather", Types.SECONDARY_MATERIAL, List.of(Properties.ATTACK_DAMAGE_1));
	public static Upgrade BINDING = Upgrade.of(ToolParts.OAK_BINDING);
}
