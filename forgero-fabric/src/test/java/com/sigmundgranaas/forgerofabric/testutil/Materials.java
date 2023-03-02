package com.sigmundgranaas.forgerofabric.testutil;

import com.sigmundgranaas.forgero.core.state.Ingredient;

import java.util.List;

import static com.sigmundgranaas.forgerofabric.testutil.Properties.ATTACK_DAMAGE_1;


public class Materials {
	public static Ingredient OAK = Ingredient.of("oak", Types.WOOD, List.of(ATTACK_DAMAGE_1));
	public static Ingredient IRON = Ingredient.of("iron", Types.MATERIAL, List.of(Properties.ATTACK_DAMAGE_10));
}
