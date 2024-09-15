package com.sigmundgranaas.forgero.testutil;

import com.sigmundgranaas.forgero.core.type.Type;

public class Types {
	public static final Type TOOL = Type.of("TOOL");
	public static final Type RANDOM = Type.of("RANDOM");
	public static final Type PICKAXE = Type.of("PICKAXE", TOOL);
	public static Type MATERIAL = Type.of("MATERIAL");
	public static Type TOOL_MATERIAL = Type.of("TOOL_MATERIAL", MATERIAL);
	public static Type WOOD = Type.of("WOOD", TOOL_MATERIAL);

	public static Type METAL = Type.of("METAL", TOOL_MATERIAL);

	public static Type SECONDARY_MATERIAL = Type.of("SECONDARY_MATERIAL", MATERIAL);
	public static Type SCHEMATIC = Type.of("SCHEMATIC");
	public static Type PICKAXE_SCHEMATIC = Type.of("PICKAXE_HEAD_SCHEMATIC", SCHEMATIC);
	public static Type SWORD_BLADE_SCHEMATIC = Type.of("SWORD_BLADE_SCHEMATIC", SCHEMATIC);
	public static Type HANDLE_SCHEMATIC = Type.of("HANDLE_SCHEMATIC", SCHEMATIC);
	public static Type BINDING_SCHEMATIC = Type.of("BINDING_SCHEMATIC", SCHEMATIC);
	public static Type PART = Type.of("PART");
	public static Type TOOL_PART = Type.of("TOOL_PART", PART);
	public static Type BINDING = Type.of("BINDING", TOOL_PART);
	public static Type TOOL_BINDING = Type.of("TOOL_BINDING", BINDING);
	public static Type TOOL_PART_HEAD = Type.of("TOOL_PART_HEAD", TOOL_PART);
	public static Type HANDLE = Type.of("HANDLE", TOOL_PART);
	public static Type TRINKET = Type.of("TRINKET");
	public static Type GEM = Type.of("GEM", TRINKET);
}
