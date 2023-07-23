package com.sigmundgranaas.forgero.core.type;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.util.SchematicMatcher;
import com.sigmundgranaas.forgero.core.util.TypeMatcher;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

public interface Type extends Matchable {
	Type HOLDABLE = new SimpleType("HOLDABLE", Optional.empty(), new TypeMatcher());

	Type TOOL = new SimpleType("TOOL", Optional.of(HOLDABLE), new TypeMatcher());
	Type PICKAXE = new SimpleType("PICKAXE", Optional.of(TOOL), new TypeMatcher());
	Type AXE = new SimpleType("AXE", Optional.of(TOOL), new TypeMatcher());
	Type WEAPON = new SimpleType("WEAPON", Optional.empty(), new TypeMatcher());
	Type SWORD = new SimpleType("SWORD", Optional.of(WEAPON), new TypeMatcher());

	Type PART = new SimpleType("PART", Optional.empty(), new TypeMatcher());
	Type BINDING = new SimpleType("BINDING", Optional.of(PART), new TypeMatcher());
	Type SWORD_GUARD = new SimpleType("SWORD_GUARD", Optional.of(PART), new TypeMatcher());
	Type TOOL_PART_HEAD = new SimpleType("TOOL_PART_HEAD", Optional.of(PART), new TypeMatcher());
	Type AXE_HEAD = new SimpleType("AXE_HEAD", Optional.of(TOOL_PART_HEAD), new TypeMatcher());
	Type HANDLE = new SimpleType("HANDLE", Optional.empty(), new TypeMatcher());

	Type BLADE = new SimpleType("BLADE", Optional.empty(), new TypeMatcher());
	Type SWORD_BLADE = new SimpleType("SWORD_BLADE", Optional.of(BLADE), new TypeMatcher());

	Type SCHEMATIC = new SimpleType("SCHEMATIC", Optional.empty(), new SchematicMatcher());
	Type SWORD_BLADE_SCHEMATIC = new SimpleType("SWORD_BLADE_SCHEMATIC", Optional.of(SCHEMATIC), new SchematicMatcher());
	Type TOOL_PART_HEAD_SCHEMATIC = new SimpleType("SWORD_PART_HEAD_SCHEMATIC", Optional.of(SCHEMATIC), new SchematicMatcher());

	Type TRINKET = new SimpleType("TRINKET", Optional.empty(), new SchematicMatcher());
	Type GEM = new SimpleType("GEM", Optional.of(TRINKET), new SchematicMatcher());
	Type UNDEFINED = new SimpleType("UNDEFINED", Optional.empty(), new TypeMatcher());
	Type MATERIAL = new SimpleType("MATERIAL", Optional.empty(), new TypeMatcher());
	Type TOOL_MATERIAL = new SimpleType("TOOL_MATERIAL", Optional.of(MATERIAL), new TypeMatcher());
	Type WOOD = new SimpleType("WOOD", Optional.of(TOOL_MATERIAL), new TypeMatcher());
	Type OAK = new SimpleType("OAK", Optional.of(WOOD), new TypeMatcher());
	Type METAL = new SimpleType("METAL", Optional.of(TOOL_MATERIAL), new TypeMatcher());
	Type STONE = new SimpleType("STONE", Optional.of(TOOL_MATERIAL), new TypeMatcher());
	Type SECONDARY_MATERIAL = new SimpleType("SECONDARY_MATERIAL", Optional.of(MATERIAL), new TypeMatcher());
	Type STRING = new SimpleType("STRING", Optional.of(SECONDARY_MATERIAL), new TypeMatcher());
	Type BOW_LIMB = new SimpleType("BOW_LIMB", Optional.of(PART), new TypeMatcher());
	Type BOW = new SimpleType("BOW", Optional.of(WEAPON), new TypeMatcher());
	Type ARROW = new SimpleType("ARROW", Optional.empty(), new TypeMatcher());
	Type ARROW_HEAD = new SimpleType("ARROW_HEAD", Optional.of(PART), new TypeMatcher());


	static Type of(String name) {
		if (ForgeroStateRegistry.TREE != null) {
			var type = ForgeroStateRegistry.TREE.find(name);
			if (type.isPresent()) {
				return type.get().type();
			}
		}

		var type = new SimpleType(name.toUpperCase(), Optional.empty(), new TypeMatcher());
		if (type.test(SCHEMATIC, MatchContext.of())) {
			return new SimpleType(name.toUpperCase(), Optional.empty(), new SchematicMatcher());
		}
		return type;
	}

	static Type of(String name, Type parent) {
		var type = new SimpleType(name.toUpperCase(), Optional.of(parent), new TypeMatcher());
		if (type.test(SCHEMATIC, MatchContext.of())) {
			return new SimpleType(name.toUpperCase(), Optional.of(parent), new SchematicMatcher());
		}
		return type;
	}

	String typeName();

	default Optional<Type> parent() {
		return Optional.empty();
	}


}
