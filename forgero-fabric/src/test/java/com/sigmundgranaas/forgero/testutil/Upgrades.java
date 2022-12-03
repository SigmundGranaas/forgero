package com.sigmundgranaas.forgero.testutil;

import com.sigmundgranaas.forgero.state.Upgrade;

import java.util.List;

import static com.sigmundgranaas.forgero.testutil.Properties.ATTACK_DAMAGE_1;

public class Upgrades {
    public static Upgrade REDSTONE_GEM = Upgrade.of("redstone-gem", Types.GEM, List.of(Properties.ATTACK_DAMAGE_10));
    public static Upgrade IRON = Upgrade.of("iron", Types.MATERIAL, List.of(ATTACK_DAMAGE_1));
    public static Upgrade BINDING = Upgrade.of(ToolParts.OAK_BINDING);
}
