package com.sigmundgranaas.forgero.recipe.implementation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.core.ForgeroRegistry;
import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.core.pattern.HeadPattern;
import com.sigmundgranaas.forgero.core.pattern.Pattern;
import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.core.toolpart.head.ToolPartHead;
import com.sigmundgranaas.forgero.recipe.RecipeCreator;
import com.sigmundgranaas.forgero.recipe.RecipeLoader;
import com.sigmundgranaas.forgero.recipe.RecipeWrapper;
import com.sigmundgranaas.forgero.recipe.customrecipe.RecipeTypes;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//TODO Lots of deprecated methods are used to create recipes.
//TODO THIS IS NOT OBJECT ORIENTED AT ALL, rework to use objects
public record RecipeCreatorImpl(
        Map<RecipeTypes, JsonObject> recipeTemplates,
        List<ForgeroTool> tools,
        List<ForgeroToolPart> toolParts,
        List<PrimaryMaterial> materials,
        List<Pattern> patterns,
        List<SecondaryMaterial> secondaryMaterials, List<Gem> gems) implements RecipeCreator {
    private static RecipeCreator INSTANCE;

    public static RecipeCreator getInstance() {
        if (INSTANCE == null) {
            ForgeroRegistry registry = ForgeroRegistry.getInstance();
            INSTANCE = new RecipeCreatorImpl(RecipeLoader.INSTANCE.loadRecipeTemplates(),
                    registry.toolCollection().getTools(),
                    registry.toolPartCollection().getToolParts(),
                    registry.materialCollection().getPrimaryMaterialsAsList(),
                    registry.patternCollection().getPatterns(),
                    registry.materialCollection().getSecondaryMaterialsAsList(),
                    registry.gemCollection().getGems()
            );
        }
        return INSTANCE;
    }

    @Override
    public List<RecipeWrapper> createRecipes() {
        List<RecipeWrapper> toolRecipes = tools.stream().map(this::createToolRecipe).flatMap(List::stream).collect(Collectors.toList());
        List<RecipeWrapper> toolPartRecipes = toolParts.stream().map(this::createToolPartRecipe).flatMap(List::stream).collect(Collectors.toList());
        List<RecipeWrapper> toolPartSecondaryMaterialUpgradeRecipe = toolParts.stream().map(this::createSecondaryMaterialUpgradeRecipes).flatMap(List::stream).collect(Collectors.toList());
        List<RecipeWrapper> toolPartGemUpgradeRecipe = toolParts.stream().map(this::createGemUpgradeRecipes).flatMap(List::stream).collect(Collectors.toList());
        List<RecipeWrapper> toolPartPatternRecipes = patterns.stream().map(this::createPatternRecipes).flatMap(List::stream).collect(Collectors.toList());


        var recipes = List.of(toolRecipes, toolPartRecipes, toolPartSecondaryMaterialUpgradeRecipe, toolPartGemUpgradeRecipe, toolPartPatternRecipes);
        return recipes.stream().flatMap(List::stream).collect(Collectors.toList());
    }

    private List<RecipeWrapper> createPatternRecipes(Pattern pattern) {
        return materials.stream().map(material -> createRecipeFromMaterialAndPattern(material, pattern)).collect(Collectors.toList());
    }

    private RecipeWrapper createRecipeFromMaterialAndPattern(PrimaryMaterial material, Pattern pattern) {
        JsonObject template = JsonParser.parseString(recipeTemplates.get(RecipeTypes.TOOLPART_PATTERN_RECIPE).toString()).getAsJsonObject();
        JsonArray ingredients = template.getAsJsonArray("ingredients");
        String toolpartType;
        if (pattern.getType() == ForgeroToolPartTypes.HEAD) {
            toolpartType = switch (((HeadPattern) pattern).getToolType()) {
                case PICKAXE -> "pickaxehead";
                case SHOVEL -> "shovelhead";
                case AXE -> "axehead";
                case SWORD -> "null";
            };
        } else {
            toolpartType = pattern.getType().getName();
        }

        template.getAsJsonObject("result").addProperty("item", new Identifier("forgero", String.format("%s_%s_%s", material.getName(), toolpartType, pattern.getVariant())).toString());
        ingredients.get(0).getAsJsonObject().addProperty("item", material.getIngredient());
        ingredients.get(1).getAsJsonObject().addProperty("item", new Identifier("forgero", pattern.getPatternIdentifier()).toString());
        return new RecipeWrapperImpl(new Identifier(ForgeroInitializer.MOD_NAMESPACE, toolpartType + "_" + material.getName() + "_" + pattern.getPatternIdentifier()), template, RecipeTypes.TOOL_PART_SECONDARY_MATERIAL_UPGRADE);
    }

    private List<RecipeWrapper> createToolPartRecipe(ForgeroToolPart toolPart) {
        if (toolPart.getToolPartType() == ForgeroToolPartTypes.HEAD) {
            return createToolPartHeadRecipe(toolPart);
        } else {
            return createHandleAndBindingRecipe(toolPart);
        }
    }

    private List<RecipeWrapper> createSecondaryMaterialUpgradeRecipes(ForgeroToolPart toolPart) {
        return secondaryMaterials.stream().filter(secondaryMaterial -> !secondaryMaterial.getName().equals(toolPart.getPrimaryMaterial().getName())).map(secondaryMaterial -> createSecondaryMaterialUpgradeRecipe(toolPart, secondaryMaterial)).collect(Collectors.toList());
    }

    private List<RecipeWrapper> createGemUpgradeRecipes(ForgeroToolPart toolPart) {
        return switch (toolPart.getToolPartType()) {
            case HEAD -> gems.stream().filter(gem -> gem.getPlacement().contains(ForgeroToolPartTypes.HEAD)).map(gem -> createGemUpgradeRecipe(toolPart, gem)).collect(Collectors.toList());
            case HANDLE -> gems.stream().filter(gem -> gem.getPlacement().contains(ForgeroToolPartTypes.HANDLE)).map(gem -> createGemUpgradeRecipe(toolPart, gem)).collect(Collectors.toList());
            case BINDING -> gems.stream().filter(gem -> gem.getPlacement().contains(ForgeroToolPartTypes.BINDING)).map(gem -> createGemUpgradeRecipe(toolPart, gem)).collect(Collectors.toList());
        };
    }

    private RecipeWrapper createSecondaryMaterialUpgradeRecipe(ForgeroToolPart toolPart, SecondaryMaterial material) {
        JsonObject template = JsonParser.parseString(recipeTemplates.get(RecipeTypes.TOOL_PART_SECONDARY_MATERIAL_UPGRADE).toString()).getAsJsonObject();
        template.getAsJsonObject("base").addProperty("item", new Identifier("forgero", toolPart.getToolPartIdentifier()).toString());
        template.getAsJsonObject("addition").addProperty("item", material.getIngredient());
        template.getAsJsonObject("result").addProperty("item", new Identifier("forgero", toolPart.getToolPartIdentifier()).toString());
        return new RecipeWrapperImpl(new Identifier(ForgeroInitializer.MOD_NAMESPACE, toolPart.getToolPartIdentifier() + "_" + material.getName() + "_secondary_upgrade"), template, RecipeTypes.TOOL_PART_SECONDARY_MATERIAL_UPGRADE);
    }

    private RecipeWrapper createGemUpgradeRecipe(ForgeroToolPart toolPart, Gem gem) {
        JsonObject template = JsonParser.parseString(recipeTemplates.get(RecipeTypes.TOOL_PART_GEM_UPGRADE).toString()).getAsJsonObject();
        template.getAsJsonObject("base").addProperty("item", new Identifier("forgero", toolPart.getToolPartIdentifier()).toString());
        template.getAsJsonObject("addition").addProperty("item", new Identifier(ForgeroInitializer.MOD_NAMESPACE, gem.getIdentifier()).toString());
        template.getAsJsonObject("result").addProperty("item", new Identifier("forgero", toolPart.getToolPartIdentifier()).toString());
        return new RecipeWrapperImpl(new Identifier(ForgeroInitializer.MOD_NAMESPACE, toolPart.getToolPartIdentifier() + "_" + gem.getIdentifier() + "_gem_upgrade"), template, RecipeTypes.TOOL_PART_GEM_UPGRADE);
    }

    private List<RecipeWrapper> createHandleAndBindingRecipe(ForgeroToolPart toolPart) {
        if (toolPart.getToolPartType() == ForgeroToolPartTypes.HANDLE) {
            return createRecipe(toolPart, RecipeTypes.HANDLE_RECIPE);
        } else {
            return createRecipe(toolPart, RecipeTypes.BINDING_RECIPE);
        }
    }

    private List<RecipeWrapper> createRecipe(ForgeroToolPart toolPart, RecipeTypes templateIdentifier) {
        JsonObject template = JsonParser.parseString(recipeTemplates.get(templateIdentifier).toString()).getAsJsonObject();
        JsonArray ingredients = template.getAsJsonArray("ingredients");
        for (int i = 0; i < ingredients.size() - 1; i++) {
            ((JsonObject) ingredients.get(i)).addProperty("item", toolPart.getPrimaryMaterial().getIngredient());
        }
        template.getAsJsonObject("result").addProperty("item", new Identifier(ForgeroInitializer.MOD_NAMESPACE, toolPart.getToolPartIdentifier()).toString());
        return List.of(new RecipeWrapperImpl(new Identifier(ForgeroInitializer.MOD_NAMESPACE, toolPart.getToolPartIdentifier()), template, templateIdentifier));
    }

    private List<RecipeWrapper> createToolPartRecipe(ForgeroToolPart toolPart, RecipeTypes templateIdentifier) {
        JsonObject template = JsonParser.parseString(recipeTemplates.get(templateIdentifier).toString()).getAsJsonObject();
        template.getAsJsonObject("key").getAsJsonObject("H").addProperty("item", toolPart.getPrimaryMaterial().getIngredient());
        template.getAsJsonObject("result").addProperty("item", new Identifier(ForgeroInitializer.MOD_NAMESPACE, toolPart.getToolPartIdentifier()).toString());
        return List.of(new RecipeWrapperImpl(new Identifier(ForgeroInitializer.MOD_NAMESPACE, toolPart.getToolPartIdentifier()), template, templateIdentifier));
    }

    private List<RecipeWrapper> createToolPartHeadRecipe(ForgeroToolPart part) {
        return switch (((ToolPartHead) part).getToolType()) {
            case PICKAXE -> createRecipe(part, RecipeTypes.PICKAXEHEAD_RECIPE);
            case SHOVEL -> createRecipe(part, RecipeTypes.SHOVELHEAD_RECIPE);
            case AXE -> createRecipe(part, RecipeTypes.AXEHEAD_RECIPE);
            case SWORD -> null;
        };
    }

    private List<RecipeWrapper> createToolRecipe(ForgeroTool tool) {
        ArrayList<RecipeWrapper> toolRecipes = new ArrayList<>();
        materials.forEach(handleMaterial -> {
            toolRecipes.add(createBaseToolRecipe(tool, handleMaterial));
            materials.forEach(bindingMaterial -> toolRecipes.add(createToolWithBindingRecipe(tool, handleMaterial, bindingMaterial)));
        });
        return toolRecipes;
    }

    private RecipeWrapper createBaseToolRecipe(ForgeroTool tool, PrimaryMaterial handleMaterial) {
        JsonObject template = JsonParser.parseString(recipeTemplates.get(RecipeTypes.TOOL_RECIPE).toString()).getAsJsonObject();
        template.getAsJsonObject("key").getAsJsonObject("H").addProperty("item", new Identifier(ForgeroInitializer.MOD_NAMESPACE, tool.getToolHead().getToolPartIdentifier()).toString());
        template.getAsJsonObject("key").getAsJsonObject("I").remove("item");
        template.getAsJsonObject("key").getAsJsonObject("I").addProperty("tag", new Identifier(ForgeroInitializer.MOD_NAMESPACE, "handles").toString());
        template.getAsJsonObject("result").addProperty("item", new Identifier(ForgeroInitializer.MOD_NAMESPACE, tool.getToolIdentifierString()).toString());
        return new RecipeWrapperImpl(new Identifier(ForgeroInitializer.MOD_NAMESPACE, tool.getToolIdentifierString() + "_" + tool.getToolHead().getToolPartIdentifier() + "_" + handleMaterial.getName() + "_handle"), template, RecipeTypes.TOOL_RECIPE);
    }

    private RecipeWrapper createToolWithBindingRecipe(ForgeroTool tool, PrimaryMaterial handleMaterial, PrimaryMaterial bindingMaterial) {
        JsonObject template = JsonParser.parseString(recipeTemplates.get(RecipeTypes.TOOL_WITH_BINDING_RECIPE).toString()).getAsJsonObject();

        template.getAsJsonObject("key").getAsJsonObject("I").remove("item");
        template.getAsJsonObject("key").getAsJsonObject("B").remove("item");
        template.getAsJsonObject("key").getAsJsonObject("H").addProperty("item", new Identifier(ForgeroInitializer.MOD_NAMESPACE, tool.getToolHead().getToolPartIdentifier()).toString());
        template.getAsJsonObject("key").getAsJsonObject("I").addProperty("tag", new Identifier(ForgeroInitializer.MOD_NAMESPACE, "handles").toString());
        template.getAsJsonObject("key").getAsJsonObject("B").addProperty("tag", new Identifier(ForgeroInitializer.MOD_NAMESPACE, "bindings").toString());
        template.getAsJsonObject("result").addProperty("item", new Identifier(ForgeroInitializer.MOD_NAMESPACE, tool.getToolIdentifierString()).toString());
        return new RecipeWrapperImpl(new Identifier(ForgeroInitializer.MOD_NAMESPACE, tool.getToolIdentifierString() + "_" + tool.getToolHead().getToolPartIdentifier() + "_" + handleMaterial.getName() + "_handle" + "_" + bindingMaterial.getName() + "_binding"), template, RecipeTypes.TOOL_WITH_BINDING_RECIPE);
    }
}