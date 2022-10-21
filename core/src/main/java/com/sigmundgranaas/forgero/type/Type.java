package com.sigmundgranaas.forgero.type;

import com.sigmundgranaas.forgero.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.util.SchematicMatcher;
import com.sigmundgranaas.forgero.util.TypeMatcher;
import com.sigmundgranaas.forgero.util.match.Context;
import com.sigmundgranaas.forgero.util.match.Matchable;

import java.util.Optional;

public interface Type extends Matchable {
    Type HANDLE = new SimpleType("HANDLE", Optional.empty(), new TypeMatcher());
    Type TOOL = new SimpleType("TOOL", Optional.empty(), new TypeMatcher());
    Type TOOL_PART_HEAD = new SimpleType("TOOL_PART_HEAD", Optional.empty(), new TypeMatcher());

    Type SWORD_BLADE = new SimpleType("SWORD_BLADE", Optional.empty(), new TypeMatcher());
    Type SCHEMATIC = new SimpleType("SCHEMATIC", Optional.empty(), new SchematicMatcher());
    Type UNDEFINED = new SimpleType("UNDEFINED", Optional.empty(), new TypeMatcher());
    Type MATERIAL = new SimpleType("MATERIAL", Optional.empty(), new TypeMatcher());

    static Type of(String name) {
        if (ForgeroStateRegistry.TREE != null) {
            var type = ForgeroStateRegistry.TREE.find(name);
            if (type.isPresent()) {
                return type.get().type();
            }
        }

        var type = new SimpleType(name.toUpperCase(), Optional.empty(), new TypeMatcher());
        if (type.test(SCHEMATIC, Context.of())) {
            return new SimpleType(name.toUpperCase(), Optional.empty(), new SchematicMatcher());
        }
        return type;
    }

    static Type of(String name, Type parent) {
        var type = new SimpleType(name.toUpperCase(), Optional.of(parent), new TypeMatcher());
        if (type.test(SCHEMATIC, Context.of())) {
            return new SimpleType(name.toUpperCase(), Optional.of(parent), new SchematicMatcher());
        }
        return type;
    }

    String typeName();

    default Optional<Type> parent() {
        return Optional.empty();
    }


}
