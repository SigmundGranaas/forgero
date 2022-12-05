package com.sigmundgranaas.forgero.resources;


import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.resources.dynamic.DynamicResourceGenerator;
import com.sigmundgranaas.forgero.resources.external.Patchouli;
import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgero.type.MutableTypeNode;
import com.sigmundgranaas.forgero.type.Type;
import lombok.Synchronized;
import net.devtech.arrp.api.RRPCallback;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.devtech.arrp.json.lang.JLang;
import net.devtech.arrp.json.models.JModel;
import net.devtech.arrp.json.models.JTextures;
import net.devtech.arrp.json.recipe.JIngredient;
import net.devtech.arrp.json.recipe.JIngredients;
import net.devtech.arrp.json.recipe.JResult;
import net.devtech.arrp.json.recipe.JShapelessRecipe;
import net.devtech.arrp.json.tags.JTag;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.sigmundgranaas.forgero.identifier.Common.ELEMENT_SEPARATOR;
import static com.sigmundgranaas.forgero.item.Items.EMPTY_REPAIR_KIT;

import java.util.ArrayList;
import java.util.List;


public class ARRPGenerator {

    public static final RuntimeResourcePack RESOURCE_PACK = RuntimeResourcePack.create("forgero:dynamic");
    private static final List<DynamicResourceGenerator> generators = new ArrayList<>();

    @Synchronized
    public static void register(DynamicResourceGenerator generator) {
        generators.add(generator);
    }

    public void generate() {
        generateResources();
        generators.stream().filter(DynamicResourceGenerator::enabled).forEach(generator -> generator.generate(RESOURCE_PACK));
    }


    public void generateResources() {
        generateTags();
        generateTagsFromStateTree();
        createMaterialToolTags();
        createRepairKitsRecipes();
        createRepairKitModel();
        createRepairKitLang();
        RRPCallback.BEFORE_VANILLA.register(a -> a.add(RESOURCE_PACK));

        if (FabricLoader.getInstance().isModLoaded("patchouli")) {
            new Patchouli().registerResources(RESOURCE_PACK);
        }
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
        var node = ForgeroStateRegistry.TREE.find(Type.TOOL_MATERIAL);
        var toolNode = ForgeroStateRegistry.TREE.find(Type.HOLDABLE);
        if (node.isPresent() && toolNode.isPresent()) {
            var tools = toolNode.get().getResources(State.class);
            var materials = node.get().getResources(State.class);

            var materialMap = materials.stream().collect(Collectors.toMap(State::name, material -> tools.stream().filter(tool -> Arrays.stream(tool.name().split(ELEMENT_SEPARATOR)).anyMatch(nameElement -> nameElement.equals(material.name()))).toList()));

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

    private void createRepairKitsRecipes() {
        var materials = ForgeroStateRegistry.TREE.find(Type.TOOL_MATERIAL)
                .map(node -> node.getResources(State.class))
                .orElse(ImmutableList.<State>builder().build());
        for (State material : materials) {
            if (ForgeroStateRegistry.STATE_TO_CONTAINER.containsKey(material.identifier())) {
                var ingredients = JIngredients.ingredients();
                ingredients.add(JIngredient.ingredient().item(ForgeroStateRegistry.STATE_TO_CONTAINER.get(material.identifier())));
                ingredients.add(JIngredient.ingredient().item(EMPTY_REPAIR_KIT));
                var recipe = JShapelessRecipe.shapeless(ingredients, JResult.result(new Identifier(Forgero.NAMESPACE, material.name() + "_repair_kit").toString()));
                RESOURCE_PACK.addRecipe(new Identifier(Forgero.NAMESPACE, material.name() + "_repair_kit"), recipe);
            }
        }
    }

    private void createRepairKitModel() {
        var materials = ForgeroStateRegistry.TREE.find(Type.TOOL_MATERIAL)
                .map(node -> node.getResources(State.class))
                .orElse(ImmutableList.<State>builder().build());
        for (State material : materials) {
            if (ForgeroStateRegistry.STATE_TO_CONTAINER.containsKey(material.identifier())) {
                var model = new JModel();
                model.parent("item/generated");
                model.textures(new JTextures().layer0("forgero:item/base_repair_kit"));
                RESOURCE_PACK.addModel(model, new Identifier(Forgero.NAMESPACE, "item/" + material.name() + "_repair_kit"));
            }
        }
    }

    private void createRepairKitLang() {
        var materials = ForgeroStateRegistry.TREE.find(Type.TOOL_MATERIAL)
                .map(node -> node.getResources(State.class))
                .orElse(ImmutableList.<State>builder().build());
        var lang = new JLang();
        for (State material : materials) {
            if (ForgeroStateRegistry.STATE_TO_CONTAINER.containsKey(material.identifier())) {
                var name = material.name().substring(0, 1).toUpperCase() + material.name().substring(1);
                lang.item(new Identifier(Forgero.NAMESPACE, material.name() + "_repair_kit"), String.format("%s Repair kit", name));
            }
        }
        RESOURCE_PACK.addLang(new Identifier(Forgero.NAMESPACE, "en_us"), lang);
    }
}
