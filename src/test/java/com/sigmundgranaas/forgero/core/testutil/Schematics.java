package com.sigmundgranaas.forgero.core.testutil;

import com.sigmundgranaas.forgero.core.state.Ingredient;

import java.util.List;

import static com.sigmundgranaas.forgero.core.testutil.Properties.ATTACK_DAMAGE_1;
import static com.sigmundgranaas.forgero.core.testutil.Types.PICKAXE_SCHEMATIC;
import static com.sigmundgranaas.forgero.item.items.testutil.Properties.DURABILITY_1000;

public class Schematics {
    public static Ingredient HANDLE_SCHEMATIC = Ingredient.of("handle-schematic", Types.HANDLE_SCHEMATIC, List.of(ATTACK_DAMAGE_1));
    public static Ingredient PICKAXE_HEAD_SCHEMATIC = Ingredient.of("pickaxe_head-schematic", PICKAXE_SCHEMATIC, List.of(ATTACK_DAMAGE_1));
    public static Ingredient BINDING_SCHEMATIC = Ingredient.of("binding-schematic", Types.BINDING_SCHEMATIC, List.of(DURABILITY_1000));
}
