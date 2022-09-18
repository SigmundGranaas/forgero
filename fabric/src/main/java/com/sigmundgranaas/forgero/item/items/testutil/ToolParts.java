package com.sigmundgranaas.forgero.item.items.testutil;

import com.sigmundgranaas.forgero.state.Composite;

import static com.sigmundgranaas.forgero.item.items.testutil.Materials.IRON;
import static com.sigmundgranaas.forgero.item.items.testutil.Materials.OAK;
import static com.sigmundgranaas.forgero.item.items.testutil.Schematics.HANDLE_SCHEMATIC;
import static com.sigmundgranaas.forgero.item.items.testutil.Schematics.PICKAXE_HEAD_SCHEMATIC;

public class ToolParts {
    public static Composite HANDLE = Composite.builder()
            .addIngredient(OAK)
            .addIngredient(HANDLE_SCHEMATIC)
            .type(Types.HANDLE)
            .build();

    public static Composite PICKAXE_HEAD = Composite.builder()
            .addIngredient(IRON)
            .addIngredient(PICKAXE_HEAD_SCHEMATIC)
            .type(Types.TOOL_PART_HEAD)
            .build();
}
