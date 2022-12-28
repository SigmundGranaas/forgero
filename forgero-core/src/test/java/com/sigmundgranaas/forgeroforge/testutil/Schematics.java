package com.sigmundgranaas.forgeroforge.testutil;

import com.sigmundgranaas.forgero.state.Ingredient;
import com.sigmundgranaas.forgero.state.State;

import java.util.List;

import static com.sigmundgranaas.forgeroforge.testutil.Properties.ATTACK_DAMAGE_1;


public class Schematics {
    public static State HANDLE_SCHEMATIC = Ingredient.of("handle-schematic", Types.HANDLE_SCHEMATIC, List.of(ATTACK_DAMAGE_1));
    public static State REINFORCED_HANDLE_SCHEMATIC = Ingredient.of("reinforced_handle-schematic", Types.HANDLE_SCHEMATIC, List.of(ATTACK_DAMAGE_1));
    public static State PICKAXE_HEAD_SCHEMATIC = Ingredient.of("pickaxe_head-schematic", Types.PICKAXE_SCHEMATIC, List.of(ATTACK_DAMAGE_1));
    public static State BINDING_SCHEMATIC = Ingredient.of("binding-schematic", Types.BINDING_SCHEMATIC, List.of(Properties.DURABILITY_1000));
}
