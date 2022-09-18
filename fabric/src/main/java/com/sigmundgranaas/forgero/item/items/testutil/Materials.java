package com.sigmundgranaas.forgero.item.items.testutil;

import com.sigmundgranaas.forgero.state.Ingredient;

import java.util.List;

import static com.sigmundgranaas.forgero.item.items.testutil.Properties.*;
import static com.sigmundgranaas.forgero.item.items.testutil.Types.MATERIAL;
import static com.sigmundgranaas.forgero.item.items.testutil.Types.WOOD;


public class Materials {
    public static Ingredient OAK = Ingredient.of("wierd_oak", WOOD, List.of(ATTACK_DAMAGE_1));
    public static Ingredient IRON = Ingredient.of("wierd_iron", MATERIAL, List.of(ATTACK_DAMAGE_10, MINING_SPEED_10, DURABILITY_1000));
}
