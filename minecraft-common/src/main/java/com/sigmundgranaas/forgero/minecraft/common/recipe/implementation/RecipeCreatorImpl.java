package com.sigmundgranaas.forgero.minecraft.common.recipe.implementation;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.DataResource;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.RecipeData;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.minecraft.common.recipe.RecipeCreator;
import com.sigmundgranaas.forgero.minecraft.common.recipe.RecipeGenerator;
import com.sigmundgranaas.forgero.minecraft.common.recipe.RecipeLoader;
import com.sigmundgranaas.forgero.minecraft.common.recipe.RecipeWrapper;
import com.sigmundgranaas.forgero.minecraft.common.recipe.customrecipe.RecipeTypes;
import com.sigmundgranaas.forgero.minecraft.common.recipe.implementation.generator.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RecipeCreatorImpl implements RecipeCreator {

    private static RecipeCreator INSTANCE;
    private final TemplateGenerator templateGenerator;
    private final RecipeDataHelper helper;
    private final RecipeDataMapper mapper;

    public RecipeCreatorImpl(Map<RecipeTypes, JsonObject> recipeTemplates) {
        this.templateGenerator = new TemplateGenerator(recipeTemplates);
        this.helper = new RecipeDataHelper();
        this.mapper = new RecipeDataMapper(this.helper);
    }

    public static RecipeCreator getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RecipeCreatorImpl(RecipeLoader.INSTANCE.loadRecipeTemplates()
            );
        }
        return INSTANCE;
    }

    @Override
    public List<RecipeWrapper> createRecipes() {
        List<RecipeGenerator> generators = new ArrayList<>();
        generators.addAll(compositeRecipeGenerators());
        generators.addAll(guideBookGenerators());
        generators.addAll(repairKitToolRecipeGenerators());
        generators.addAll(constructUpgradeRecipes());

        return generators.parallelStream()
                .filter(RecipeGenerator::isValid)
                .map(RecipeGenerator::generate)
                .toList();
    }

    private List<RecipeGenerator> guideBookGenerators() {
        return Stream.of("bindings", "heads", "handles")
                .map(tag -> "forgero:" + tag)
                .map(tag -> new GuideBookGenerator(templateGenerator, tag))
                .collect(Collectors.toList());
    }

    private List<RecipeGenerator> constructUpgradeRecipes() {
        return ForgeroStateRegistry.CONSTRUCTS.stream()
                .map(this::upgradeRecipes)
                .flatMap(List::stream)
                .toList();
    }

    private List<RecipeGenerator> compositeRecipeGenerators() {
        var optimiser = new CompositeRecipeOptimiser();
        var recipes = ForgeroStateRegistry.RECIPES.stream()
                .map(mapper).collect(Collectors.toList());
        var optimized = optimiser.process(recipes);

        return optimized.stream()
                .map(this::dataToGenerator)
                .flatMap(Optional::stream)
                .toList();

    }

    private List<RecipeGenerator> repairKitToolRecipeGenerators() {
        var materials = ForgeroStateRegistry.TREE.find(Type.TOOL_MATERIAL)
                .map(node -> node.getResources(State.class))
                .orElse(ImmutableList.<State>builder().build());
        var recipes = new ArrayList<RecipeGenerator>();
        for (State material : materials) {
            recipes.add(new RepairKitRecipeGenerator(material, templateGenerator));
        }
        return recipes;
    }

    private Optional<RecipeGenerator> dataToGenerator(RecipeData data) {
        RecipeTypes type = RecipeTypes.of(data.type());
        if (type == RecipeTypes.SCHEMATIC_PART_CRAFTING) {
            return Optional.of(new SchematicPartGenerator(helper, data, templateGenerator));
        } else if (type == RecipeTypes.STATE_CRAFTING_RECIPE) {
            return Optional.of(new ToolRecipeCreator(data, helper, templateGenerator));
        }
        return Optional.empty();
    }

    private List<RecipeGenerator> upgradeRecipes(DataResource res) {
        if (res.construct().isPresent()) {
            return res.construct().get().slots().stream()
                    .map(slot -> new SlotUpgradeGenerator(helper, templateGenerator, slot, ForgeroStateRegistry.ID_MAPPER.get(res.identifier())))
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }
}