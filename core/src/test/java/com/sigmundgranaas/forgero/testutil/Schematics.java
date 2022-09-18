package com.sigmundgranaas.forgero.testutil;

import com.sigmundgranaas.forgero.state.Ingredient;

import java.util.List;

import static com.sigmundgranaas.forgero.testutil.Properties.ATTACK_DAMAGE_1;


public class Schematics {
    public static Ingredient HANDLE_SCHEMATIC = Ingredient.of("handle-schematic", Types.HANDLE_SCHEMATIC, List.of(ATTACK_DAMAGE_1));
    public static Ingredient PICKAXE_HEAD_SCHEMATIC = Ingredient.of("pickaxe_head-schematic", Types.PICKAXE_SCHEMATIC, List.of(ATTACK_DAMAGE_1));
    public static Ingredient BINDING_SCHEMATIC = Ingredient.of("binding-schematic", Types.BINDING_SCHEMATIC, List.of(Properties.DURABILITY_1000));
}
