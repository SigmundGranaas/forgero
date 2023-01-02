package com.sigmundgranaas.forgero.core.state;

import com.sigmundgranaas.forgero.core.property.attribute.AttributeBuilder;
import com.sigmundgranaas.forgero.core.property.Attribute;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.type.Type;
import lombok.Builder;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@Builder(toBuilder = true)
public class LeveledState implements State {
    private final String name;
    private final String nameSpace;
    private final Type type;
    @Builder.Default
    private List<Property> properties = Collections.emptyList();
    @Builder.Default
    private int level = 1;

    @Override
    public String name() {
        return name;
    }

    @Override
    public String nameSpace() {
        return nameSpace;
    }

    @Override
    public Type type() {
        return type;
    }

    public int level() {
        return level;
    }

    @Override
    public @NotNull List<Property> applyProperty(Target target) {
        var otherProperties = properties.stream().filter(property -> !(property instanceof Attribute)).toList();
        var attributes = properties
                .stream().filter(property -> property instanceof Attribute)
                .map(Attribute.class::cast)
                .map(attribute -> AttributeBuilder
                        .createAttributeBuilderFromAttribute(attribute)
                        .applyLevel(level())
                        .build())
                .toList();


        return Stream.of(otherProperties, attributes)
                .flatMap(List::stream)
                .filter(prop -> prop.applyCondition(target))
                .map(Property.class::cast)
                .toList();
    }

    public LeveledState levelUp() {
        return toBuilder()
                .level(level + 1)
                .name(name)
                .nameSpace(nameSpace)
                .type(type)
                .build();
    }

    public LeveledState setLevel(int level) {
        return toBuilder()
                .level(level)
                .name(name)
                .nameSpace(nameSpace)
                .type(type)
                .build();
    }
}
