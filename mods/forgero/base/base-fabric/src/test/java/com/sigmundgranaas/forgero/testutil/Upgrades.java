package com.sigmundgranaas.forgero.testutil;

import java.util.List;

import com.sigmundgranaas.forgero.core.state.Upgrade;
import com.sigmundgranaas.forgero.core.type.Type;

public class Upgrades {
	public static Upgrade REDSTONE_GEM = Upgrade.of("redstone-gem", Type.GEM, List.of(Properties.ATTACK_DAMAGE_10));
	public static Upgrade IRON = Upgrade.of("iron", Type.MATERIAL, List.of(Properties.ATTACK_DAMAGE_1));
	public static Upgrade BINDING = Upgrade.of(ToolParts.OAK_BINDING);
}
