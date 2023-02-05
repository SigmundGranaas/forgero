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

public class RecipeCreatorImpl implements RecipeCreator {

    private static RecipeCreator INSTANCE;

    private final List<RecipeGenerator> generators;
    private final TemplateGenerator templateGenerator;
    private final RecipeDataHelper helper;
    private final RecipeDataMapper mapper;

    public RecipeCreatorImpl(Map<RecipeTypes, JsonObject> recipeTemplates) {
        this.templateGenerator = new TemplateGenerator(recipeTemplates);
        this.helper = new RecipeDataHelper();
        this.mapper = new RecipeDataMapper(this.helper);
        this.generators = new ArrayList<>();
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
        generators.addAll(compositeRecipeGenerators());
        generators.addAll(repairKitToolRecipeGenerators());
        generators.addAll(constructUpgradeRecipes());
        generators.addAll(basicWoodenPartRecipes());
        generators.addAll(basicStonePartUpgrade());
        return generators.stream()
                .filter(RecipeGenerator::isValid)
                .map(RecipeGenerator::generate)
                .toList();
    }

    @Override
    public void registerGenerator(List<RecipeGenerator> generators) {
        this.generators.addAll(generators);
    }

    @Override
    public TemplateGenerator templates() {
        return templateGenerator;
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

        return optimized.parallelStream()
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

    private List<RecipeGenerator> basicWoodenPartRecipes() {
        var materials = ForgeroStateRegistry.TREE.find(Type.WOOD)
                .map(node -> node.getResources(State.class))
                .orElse(ImmutableList.<State>builder().build());
        var recipes = new ArrayList<RecipeGenerator>();
        for (State material : materials) {
            recipes.add(new BasicWoodenToolRecipeGenerator(material, "pickaxe_head", RecipeTypes.BASIC_PICKAXE_HEAD, templateGenerator));
            recipes.add(new BasicWoodenToolRecipeGenerator(material, "axe_head", RecipeTypes.BASIC_AXE_HEAD, templateGenerator));
            recipes.add(new BasicWoodenToolRecipeGenerator(material, "shovel_head", RecipeTypes.BASIC_SHOVEL_HEAD, templateGenerator));
            recipes.add(new BasicWoodenToolRecipeGenerator(material, "hoe_head", RecipeTypes.BASIC_HOE_HEAD, templateGenerator));
            recipes.add(new BasicWoodenToolRecipeGenerator(material, "handle", RecipeTypes.BASIC_HANDLE, templateGenerator));
            recipes.add(new BasicWoodenToolRecipeGenerator(material, "sword_blade", RecipeTypes.BASIC_SWORD_BLADE, templateGenerator));
            recipes.add(new BasicWoodenToolRecipeGenerator(material, "sword_guard", RecipeTypes.BASIC_SWORD_GUARD, templateGenerator));
            recipes.add(new BasicWoodenToolRecipeGenerator(material, "shortsword_blade", RecipeTypes.BASIC_SHORT_SWORD_BLADE, templateGenerator));
        }
        return recipes;
    }

    private List<RecipeGenerator> basicStonePartUpgrade() {
        var materials = ForgeroStateRegistry.TREE.find(Type.STONE)
                .map(node -> node.getResources(State.class))
                .orElse(ImmutableList.<State>builder().build());
        var recipes = new ArrayList<RecipeGenerator>();
        for (State material : materials) {
            recipes.add(new BasicStonePartUpgradeRecipeGenerator(material, "pickaxe_head", RecipeTypes.ANY_PART_TO_STONE, templateGenerator));
            recipes.add(new BasicStonePartUpgradeRecipeGenerator(material, "axe_head", RecipeTypes.ANY_PART_TO_STONE, templateGenerator));
            recipes.add(new BasicStonePartUpgradeRecipeGenerator(material, "shovel_head", RecipeTypes.ANY_PART_TO_STONE, templateGenerator));
            recipes.add(new BasicStonePartUpgradeRecipeGenerator(material, "hoe_head", RecipeTypes.ANY_PART_TO_STONE, templateGenerator));
            recipes.add(new BasicStonePartUpgradeRecipeGenerator(material, "handle", RecipeTypes.ANY_PART_TO_STONE, templateGenerator));
            recipes.add(new BasicStonePartUpgradeRecipeGenerator(material, "sword_blade", RecipeTypes.ANY_PART_TO_STONE, templateGenerator));
            recipes.add(new BasicStonePartUpgradeRecipeGenerator(material, "sword_guard", RecipeTypes.ANY_PART_TO_STONE, templateGenerator));
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