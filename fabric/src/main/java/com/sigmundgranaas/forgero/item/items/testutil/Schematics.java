package com.sigmundgranaas.forgero.item.items.testutil;

import com.sigmundgranaas.forgero.state.Ingredient;
import com.sigmundgranaas.forgero.type.Type;

import java.util.List;

import static com.sigmundgranaas.forgero.item.items.testutil.Properties.ATTACK_DAMAGE_1;
import static com.sigmundgranaas.forgero.item.items.testutil.Types.PICKAXE_SCHEMATIC;


public class Schematics {
    public static Ingredient HANDLE_SCHEMATIC = Ingredient.of("handle-schematic", Types.HANDLE_SCHEMATIC, List.of(ATTACK_DAMAGE_1));
    public static Ingredient PICKAXE_HEAD_SCHEMATIC = Ingredient.of("pickaxe_head-schematic", PICKAXE_SCHEMATIC, List.of(ATTACK_DAMAGE_1));
    public static Ingredient BINDING_SCHEMATIC = Ingredient.of("binding-schematic", Type.SCHEMATIC, List.of(ATTACK_DAMAGE_1));
}
