package com.sigmundgranaas.forgero.item.items.testutil;

import com.sigmundgranaas.forgero.type.Type;

public class Types {
    public static final Type TOOL = Type.of("TOOL");
    public static final Type RANDOM = Type.of("RANDOM");
    public static final Type PICKAXE = Type.of("PICKAXE", TOOL);
    public static Type MATERIAL = Type.of("MATERIAL");
    public static Type WOOD = Type.of("WOOD", MATERIAL);
    public static Type SCHEMATIC = Type.of("SCHEMATIC");
    public static Type PICKAXE_SCHEMATIC = Type.of("PICKAXE_SCHEMATIC", SCHEMATIC);
    public static Type HANDLE_SCHEMATIC = Type.of("HANDLE_SCHEMATIC", SCHEMATIC);
    public static Type PART = Type.of("PART");
    public static Type TOOL_PART = Type.of("TOOL_PART", PART);
    public static Type TOOL_PART_HEAD = Type.of("TOOL_PART_HEAD", TOOL_PART);
    public static Type HANDLE = Type.of("HANDLE", TOOL_PART);
}
