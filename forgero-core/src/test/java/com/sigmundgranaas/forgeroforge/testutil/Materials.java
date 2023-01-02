package com.sigmundgranaas.forgeroforge.testutil;

import com.sigmundgranaas.forgero.core.state.Ingredient;
import com.sigmundgranaas.forgero.core.state.State;

import java.util.List;

import static com.sigmundgranaas.forgeroforge.testutil.Properties.ATTACK_DAMAGE_1;


public class Materials {
    public static State OAK = Ingredient.of("oak", Types.WOOD, List.of(ATTACK_DAMAGE_1));
    public static State IRON = Ingredient.of("iron", Types.METAL, List.of(Properties.ATTACK_DAMAGE_10));
}
