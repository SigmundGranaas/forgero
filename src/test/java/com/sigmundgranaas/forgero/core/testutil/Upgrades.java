package com.sigmundgranaas.forgero.core.testutil;

import com.sigmundgranaas.forgero.core.state.Upgrade;

import java.util.List;

import static com.sigmundgranaas.forgero.core.testutil.Properties.ATTACK_DAMAGE_1;
import static com.sigmundgranaas.forgero.core.testutil.Types.*;
import static com.sigmundgranaas.forgero.item.items.testutil.Properties.ATTACK_DAMAGE_10;
import static com.sigmundgranaas.forgero.item.items.testutil.Properties.DURABILITY_1000;

public class Upgrades {
    public static Upgrade REDSTONE_GEM = Upgrade.of("redstone-gem", GEM, List.of(ATTACK_DAMAGE_10));
    public static Upgrade IRON = Upgrade.of("iron", MATERIAL, List.of(ATTACK_DAMAGE_1));
    public static Upgrade OAK_BINDING = Upgrade.of("oak-binding", BINDING, List.of(DURABILITY_1000));
}
