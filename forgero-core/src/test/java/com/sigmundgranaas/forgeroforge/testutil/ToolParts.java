package com.sigmundgranaas.forgeroforge.testutil;

import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.slot.EmptySlot;

import java.util.Collections;
import java.util.List;

import static com.sigmundgranaas.forgeroforge.testutil.Materials.IRON;
import static com.sigmundgranaas.forgeroforge.testutil.Materials.OAK;

public class ToolParts {
    public static Composite HANDLE = Composite.builder()
            .addIngredient(OAK)
            .addIngredient(Schematics.HANDLE_SCHEMATIC)
            .addUpgrades(EmptySlot.of(List.of(Types.MATERIAL, Types.GEM), Collections.emptySet()))
            .type(Types.HANDLE)
            .build();

    public static Composite REINFORCED_HANDLE = Composite.builder()
            .addIngredient(OAK)
            .addIngredient(Schematics.REINFORCED_HANDLE_SCHEMATIC)
            .addUpgrades(EmptySlot.of(List.of(Types.MATERIAL, Types.GEM, Types.MATERIAL), Collections.emptySet()))
            .type(Types.HANDLE)
            .build();

    public static Composite PICKAXE_HEAD = Composite.builder()
            .addIngredient(IRON)
            .addIngredient(Schematics.PICKAXE_HEAD_SCHEMATIC)
            .type(Types.TOOL_PART_HEAD)
            .build();

    public static Composite OAK_BINDING = Composite.builder()
            .addIngredient(OAK)
            .addIngredient(Schematics.BINDING_SCHEMATIC)
            .type(Types.TOOL_BINDING)
            .build();
}
