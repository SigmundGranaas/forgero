package com.sigmundgranaas.forgero.core.testutil;

import com.sigmundgranaas.forgero.core.state.Ingredient;

import java.util.List;

import static com.sigmundgranaas.forgero.core.testutil.Properties.ATTACK_DAMAGE_1;
import static com.sigmundgranaas.forgero.core.testutil.Types.MATERIAL;
import static com.sigmundgranaas.forgero.core.testutil.Types.WOOD;
import static com.sigmundgranaas.forgero.item.items.testutil.Properties.ATTACK_DAMAGE_10;

public class Materials {
    public static Ingredient OAK = Ingredient.of("oak", WOOD, List.of(ATTACK_DAMAGE_1));
    public static Ingredient IRON = Ingredient.of("iron", MATERIAL, List.of(ATTACK_DAMAGE_10));
}
