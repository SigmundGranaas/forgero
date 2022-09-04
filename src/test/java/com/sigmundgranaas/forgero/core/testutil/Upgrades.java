package com.sigmundgranaas.forgero.core.testutil;

import com.sigmundgranaas.forgero.core.state.Upgrade;

import java.util.List;

import static com.sigmundgranaas.forgero.core.testutil.Properties.ATTACK_DAMAGE_1;
import static com.sigmundgranaas.forgero.core.testutil.ToolParts.OAK_BINDING;
import static com.sigmundgranaas.forgero.core.testutil.Types.GEM;
import static com.sigmundgranaas.forgero.core.testutil.Types.MATERIAL;
import static com.sigmundgranaas.forgero.item.items.testutil.Properties.ATTACK_DAMAGE_10;

public class Upgrades {
    public static Upgrade REDSTONE_GEM = Upgrade.of("redstone-gem", GEM, List.of(ATTACK_DAMAGE_10));
    public static Upgrade IRON = Upgrade.of("iron", MATERIAL, List.of(ATTACK_DAMAGE_1));
    public static Upgrade BINDING = Upgrade.of(OAK_BINDING);
}
