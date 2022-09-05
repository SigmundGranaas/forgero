package com.sigmundgranaas.forgerocore.type;

import com.sigmundgranaas.forgerocore.util.Matchable;
import com.sigmundgranaas.forgerocore.util.SchematicMatcher;
import com.sigmundgranaas.forgerocore.util.TypeMatcher;

import java.util.Optional;

public interface Type extends Matchable {
    static Type of(String name) {
        var type = new SimpleType(name.toUpperCase(), Optional.empty(), new TypeMatcher());
        if (type.test(SCHEMATIC)) {
            return new SimpleType(name.toUpperCase(), Optional.empty(), new SchematicMatcher());
        }
        return type;
    }

    static Type of(String name, Type parent) {
        var type = new SimpleType(name.toUpperCase(), Optional.of(parent), new TypeMatcher());
        if (type.test(SCHEMATIC)) {
            return new SimpleType(name.toUpperCase(), Optional.of(parent), new SchematicMatcher());
        }
        return type;
    }

    String typeName();

    default Optional<Type> parent() {
        return Optional.empty();
    }

    Type UNDEFINED = Type.of("UNDEFINED");
    Type MATERIAL = Type.of("MATERIAL");
    Type SCHEMATIC = Type.of("SCHEMATIC");
    Type TOOL_PART_HEAD = Type.of("TOOL_PART_HEAD");
    Type HANDLE = Type.of("HANDLE");
    Type TOOL = Type.of("TOOL");

}
