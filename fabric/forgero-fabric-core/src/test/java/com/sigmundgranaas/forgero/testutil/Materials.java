package com.sigmundgranaas.forgero.testutil;

import static com.sigmundgranaas.forgero.testutil.Properties.ATTACK_DAMAGE_1;

import java.util.List;

import com.sigmundgranaas.forgero.core.state.Ingredient;
import com.sigmundgranaas.forgero.core.type.Type;


public class Materials {
	public static Ingredient OAK = Ingredient.of("oak", Type.WOOD, List.of(ATTACK_DAMAGE_1));
	public static Ingredient IRON = Ingredient.of("iron", "c", Type.MATERIAL, List.of(Properties.ATTACK_DAMAGE_10));
}
