package com.sigmundgranaas.forgero.testutil;

import com.sigmundgranaas.forgero.state.Composite;

import static com.sigmundgranaas.forgero.testutil.Materials.IRON;
import static com.sigmundgranaas.forgero.testutil.Materials.OAK;

public class ToolParts {
    public static Composite HANDLE = Composite.builder()
            .addIngredient(OAK)
            .addIngredient(Schematics.HANDLE_SCHEMATIC)
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
            .type(Types.BINDING)
            .build();
}
