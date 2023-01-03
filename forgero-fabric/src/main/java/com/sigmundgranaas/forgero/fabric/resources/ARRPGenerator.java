package com.sigmundgranaas.forgero.fabric.resources;


import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.MutableTypeNode;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.fabric.resources.dynamic.DynamicResourceGenerator;
import lombok.Synchronized;
import net.devtech.arrp.api.RRPCallback;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.devtech.arrp.json.tags.JTag;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.sigmundgranaas.forgero.core.identifier.Common.ELEMENT_SEPARATOR;


public class ARRPGenerator {

    public static final RuntimeResourcePack RESOURCE_PACK = RuntimeResourcePack.create("forgero:dynamic");
    private static final List<DynamicResourceGenerator> generators = new ArrayList<>();

    @Synchronized
    public static void register(DynamicResourceGenerator generator) {
        generators.add(generator);
    }

    @Synchronized
    public static void register(Supplier<DynamicResourceGenerator> supplier) {
        generators.add(supplier.get());
    }

    public static void generate() {
        new ARRPGenerator().generateResources();
        generators.stream()
                .filter(DynamicResourceGenerator::enabled)
                .forEach(generator -> generator.generate(RESOURCE_PACK));
    }


    public void generateResources() {
        generateTags();
        generateTagsFromStateTree();
        createMaterialToolTags();
        RRPCallback.BEFORE_VANILLA.register(a -> a.add(RESOURCE_PACK));
    }

    public void generateTags() {

    }

    public void generateTagsFromStateTree() {
        ForgeroStateRegistry.TREE.nodes().forEach(this::createTagFromType);
    }

    private void createTagFromType(MutableTypeNode node) {
        JTag typeTag = new JTag();

        var states = node.getResources(State.class);
        if (states.size() > 0) {
            states.stream()
                    .filter(state -> ForgeroStateRegistry.STATE_TO_CONTAINER.containsKey(state.identifier()))
                    .map(state -> new Identifier(ForgeroStateRegistry.STATE_TO_CONTAINER.get(state.identifier())))
                    .forEach(typeTag::add);
            RESOURCE_PACK.addTag(new Identifier("forgero", "items/" + node.name().toLowerCase()), typeTag);
        }
    }

    private void createMaterialToolTags() {

        var tools = ForgeroStateRegistry.STATES.find(Type.TOOL_MATERIAL);
        var materials = ForgeroStateRegistry.STATES.find(Type.HOLDABLE);

        Map<String, List<State>> materialMap = materials.stream()
                .collect(Collectors.toMap(supplier -> supplier.get().name(), material -> tools.stream()
                        .filter(tool -> Arrays.stream(tool.get().name().split(ELEMENT_SEPARATOR))
                                .anyMatch(nameElement -> nameElement.equals(material.get().name())))
                        .map(Supplier::get)
                        .toList()));

        for (Map.Entry<String, List<State>> entry : materialMap.entrySet()) {
            String key = entry.getKey();
            List<State> states = entry.getValue();
            JTag materialToolTag = new JTag();
            if (states.size() > 0) {
                states.stream()
                        .map(State::identifier)
                        .map(Identifier::new)
                        .forEach(materialToolTag::add);
                RESOURCE_PACK.addTag(new Identifier(Forgero.NAMESPACE, "items/" + key + "_tool"), materialToolTag);
            }
        }

    }

}
