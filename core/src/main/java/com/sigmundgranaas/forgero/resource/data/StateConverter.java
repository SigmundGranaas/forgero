package com.sigmundgranaas.forgero.resource.data;

import com.sigmundgranaas.forgero.resource.data.factory.PropertyBuilder;
import com.sigmundgranaas.forgero.resource.data.v2.data.ConstructData;
import com.sigmundgranaas.forgero.resource.data.v2.data.DataResource;
import com.sigmundgranaas.forgero.resource.data.v2.data.IngredientData;
import com.sigmundgranaas.forgero.state.Composite;
import com.sigmundgranaas.forgero.state.Slot;
import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgero.state.slot.EmptySlot;
import com.sigmundgranaas.forgero.type.MutableTypeNode;
import com.sigmundgranaas.forgero.type.Type;
import com.sigmundgranaas.forgero.type.TypeTree;

import java.util.*;
import java.util.stream.IntStream;

public class StateConverter implements DataConverter<State> {
    private final HashMap<String, State> states = new HashMap<>();
    private final TypeTree tree;
    private final HashMap<String, String> nameMapping = new HashMap<>();

    public StateConverter(TypeTree tree) {
        this.tree = tree;
    }

    @Override
    public Optional<State> convert(DataResource resource) {
        if (resource.construct().isEmpty()) {
            return createState(resource);
        } else {
            return createComposite(resource);
        }
    }

    public HashMap<String, State> states() {
        return states;
    }

    public HashMap<String, String> nameMapper() {
        return nameMapping;
    }

    private Optional<State> createComposite(DataResource resource) {
        if (resource.construct().isPresent()) {
            var builder = Composite.builder(createSlots(resource.construct().get()));
            builder.type(tree.type(resource.type()));
            builder.nameSpace(resource.nameSpace());
            resource.construct()
                    .map(ConstructData::components)
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(IngredientData::id)
                    .map(nameMapping::get)
                    .map(states::get)
                    .filter(Objects::nonNull)
                    .forEach(builder::addIngredient);
            var state = builder.build();
            states.put(state.identifier(), state);
            nameMapping.put(resource.identifier(), state.identifier());
            return Optional.of(state);
        }
        return Optional.empty();
    }

    private List<? extends Slot> createSlots(ConstructData data) {
        return IntStream.range(0, data.slots().size())
                .mapToObj(index -> new EmptySlot(index, tree.find(data.slots().get(index).type()).map(MutableTypeNode::type).orElse(Type.of(data.slots().get(index).type())), data.slots().get(index).description(), Set.copyOf(data.slots().get(index).category()))).toList();
    }

    private Optional<State> createState(DataResource resource) {
        var state = State.of(resource.name(),
                resource.nameSpace(),
                tree.type(resource.type()),
                resource.properties().map(PropertyBuilder::createPropertyListFromPOJO).orElse(Collections.emptyList()));
        states.put(state.identifier(), state);
        nameMapping.put(resource.identifier(), state.identifier());
        return Optional.of(state);
    }
}
