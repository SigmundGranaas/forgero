package com.sigmundgranaas.forgero.core.testutil;

import com.sigmundgranaas.forgero.core.state.Composite;

import static com.sigmundgranaas.forgero.core.testutil.Materials.IRON;
import static com.sigmundgranaas.forgero.core.testutil.Materials.OAK;
import static com.sigmundgranaas.forgero.core.testutil.Schematics.*;

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

    public static Composite OAK_BINDING = Composite.builder()
            .add(OAK)
            .add(BINDING_SCHEMATIC)
            .type(Types.BINDING)
            .build();
}
