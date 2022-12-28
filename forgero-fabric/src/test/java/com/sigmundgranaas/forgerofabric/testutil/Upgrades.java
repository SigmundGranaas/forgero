package com.sigmundgranaas.forgerofabric.testutil;

import com.sigmundgranaas.forgero.state.Upgrade;

import java.util.List;

public class Upgrades {
    public static Upgrade REDSTONE_GEM = Upgrade.of("redstone-gem", Types.GEM, List.of(Properties.ATTACK_DAMAGE_10));
    public static Upgrade IRON = Upgrade.of("iron", Types.MATERIAL, List.of(Properties.ATTACK_DAMAGE_1));
    public static Upgrade BINDING = Upgrade.of(ToolParts.OAK_BINDING);
}
