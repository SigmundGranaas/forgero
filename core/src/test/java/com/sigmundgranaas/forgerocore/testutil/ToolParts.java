package com.sigmundgranaas.forgerocore.testutil;

import com.sigmundgranaas.forgerocore.state.Composite;

import static com.sigmundgranaas.forgerocore.testutil.Materials.IRON;
import static com.sigmundgranaas.forgerocore.testutil.Materials.OAK;

public class ToolParts {
    public static Composite HANDLE = Composite.builder()
            .add(OAK)
            .add(Schematics.HANDLE_SCHEMATIC)
            .type(Types.HANDLE)
            .build();

    public static Composite PICKAXE_HEAD = Composite.builder()
            .add(IRON)
            .add(Schematics.PICKAXE_HEAD_SCHEMATIC)
            .type(Types.TOOL_PART_HEAD)
            .build();

    public static Composite OAK_BINDING = Composite.builder()
            .add(OAK)
            .add(Schematics.BINDING_SCHEMATIC)
            .type(Types.BINDING)
            .build();
}
