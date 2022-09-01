package com.sigmundgranaas.forgero.core.testutil;

import com.sigmundgranaas.forgero.core.state.Composite;

import static com.sigmundgranaas.forgero.core.testutil.Materials.IRON;
import static com.sigmundgranaas.forgero.core.testutil.Materials.OAK;
import static com.sigmundgranaas.forgero.core.testutil.Schematics.HANDLE_SCHEMATIC;
import static com.sigmundgranaas.forgero.core.testutil.Schematics.PICKAXE_HEAD_SCHEMATIC;

public class ToolParts {
    public static Composite HANDLE = Composite.builder()
            .add(OAK)
            .add(HANDLE_SCHEMATIC)
            .type(Types.HANDLE)
            .build();

    public static Composite PICKAXE_HEAD = Composite.builder()
            .add(IRON)
            .add(PICKAXE_HEAD_SCHEMATIC)
            .type(Types.TOOL_PART_HEAD)
            .build();
}
