package com.sigmundgranaas.forgero.recipe.implementation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.core.ForgeroRegistry;
import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.core.schematic.HeadSchematic;
import com.sigmundgranaas.forgero.core.schematic.Schematic;
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
        List<Schematic> schematics,
        List<SecondaryMaterial> secondaryMaterials, List<Gem> gems) implements RecipeCreator {
    private static RecipeCreator INSTANCE;

    public static RecipeCreator getInstance() {
        if (INSTANCE == null) {
            ForgeroRegistry registry = ForgeroRegistry.getInstance();
            INSTANCE = new RecipeCreatorImpl(RecipeLoader.INSTANCE.loadRecipeTemplates(),
                    registry.toolCollection().getTools(),
                    registry.toolPartCollection().getToolParts(),
                    registry.materialCollection().getPrimaryMaterialsAsList(),
                    registry.schematicCollection().getSchematics(),
                    registry.materialCollection().getSecondaryMaterialsAsList(),
                    registry.gemCollection().getGems()
            );
        }
        return INSTANCE;
    }

    @Override
    public List<RecipeWrapper> createRecipes() {
        List<RecipeWrapper> toolRecipes = tools.stream().map(this::createToolRecipe).flatMap(List::stream).toList();
        List<RecipeWrapper> toolPartSecondaryMaterialUpgradeRecipe = toolParts.stream().map(this::createSecondaryMaterialUpgradeRecipes).flatMap(List::stream).toList();
        List<RecipeWrapper> toolPartGemUpgradeRecipe = toolParts.stream().map(this::createGemUpgradeRecipes).flatMap(List::stream).toList();
        List<RecipeWrapper> toolPartSchematicRecipes = schematics.stream().map(this::createSchematicRecipes).flatMap(List::stream).toList();


        var recipes = List.of(toolRecipes, toolPartSecondaryMaterialUpgradeRecipe, toolPartGemUpgradeRecipe, toolPartSchematicRecipes);
        return recipes.stream().flatMap(List::stream).collect(Collectors.toList());
    }

    private List<RecipeWrapper> createSchematicRecipes(Schematic schematic) {
        return materials.stream().map(material -> createRecipeFromMaterialAndSchematic(material, schematic)).collect(Collectors.toList());
    }

    private RecipeWrapper createRecipeFromMaterialAndSchematic(PrimaryMaterial material, Schematic schematic) {
        JsonObject template = JsonParser.parseString(recipeTemplates.get(RecipeTypes.TOOLPART_SCHEMATIC_RECIPE).toString()).getAsJsonObject();
        JsonArray ingredients = template.getAsJsonArray("ingredients");
        String toolpartType;
        if (schematic.getType() == ForgeroToolPartTypes.HEAD) {
            toolpartType = switch (((HeadSchematic) schematic).getToolType()) {
                case PICKAXE -> "pickaxehead";
                case SHOVEL -> "shovelhead";
                case AXE -> "axehead";
                case SWORD -> "null";
            };
        } else {
            toolpartType = schematic.getType().getName();
        }

        template.getAsJsonObject("result").addProperty("item", new Identifier("forgero", String.format("%s_%s_%s", material.getName(), toolpartType, schematic.getVariant())).toString());
        JsonObject materialIngredient = new JsonObject();
        materialIngredient.addProperty("item", material.getIngredient());
        JsonObject schematicIngredient = new JsonObject();
        schematicIngredient.addProperty("item", new Identifier("forgero", schematic.getSchematicIdentifier()).toString());
        for (int i = 0; i < schematic.getMaterialCount(); i++) {
            ingredients.add(materialIngredient);
        }
        ingredients.add(schematicIngredient);
        return new RecipeWrapperImpl(new Identifier(ForgeroInitializer.MOD_NAMESPACE, toolpartType + "_" + material.getName() + "_" + schematic.getSchematicIdentifier()), template, RecipeTypes.TOOL_PART_SECONDARY_MATERIAL_UPGRADE);
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


    private List<RecipeWrapper> createToolRecipe(ForgeroTool tool) {
        ArrayList<RecipeWrapper> toolRecipes = new ArrayList<>();
        toolParts.stream()
                .filter(part -> part instanceof ToolPartHead)
                .map(ToolPartHead.class::cast)
                .filter(toolPartHead -> toolPartHead.getToolType() == tool.getToolType())
                .filter(toolPartHead -> toolPartHead.getPrimaryMaterial().getName().equals(tool.getMaterial().getName()))
                .forEach(toolPartHead -> {
                    materials.forEach(handleMaterial -> {
                        toolRecipes.add(createBaseToolRecipe(tool, toolPartHead, handleMaterial));
                        materials.forEach(bindingMaterial -> toolRecipes.add(createToolWithBindingRecipe(tool, toolPartHead, handleMaterial, bindingMaterial)));
                    });
                });
        return toolRecipes;
    }

    private RecipeWrapper createBaseToolRecipe(ForgeroTool tool, ToolPartHead head, PrimaryMaterial handleMaterial) {
        JsonObject template = JsonParser.parseString(recipeTemplates.get(RecipeTypes.TOOL_RECIPE).toString()).getAsJsonObject();
        template.getAsJsonObject("key").getAsJsonObject("H").addProperty("item", new Identifier(ForgeroInitializer.MOD_NAMESPACE, head.getToolPartIdentifier()).toString());
        template.getAsJsonObject("key").getAsJsonObject("I").remove("item");
        template.getAsJsonObject("key").getAsJsonObject("I").addProperty("tag", new Identifier(ForgeroInitializer.MOD_NAMESPACE, "handles").toString());
        template.getAsJsonObject("result").addProperty("item", new Identifier(ForgeroInitializer.MOD_NAMESPACE, tool.getToolIdentifierString()).toString());
        return new RecipeWrapperImpl(new Identifier(ForgeroInitializer.MOD_NAMESPACE, tool.getToolIdentifierString() + "_" + head.getToolPartIdentifier() + "_" + handleMaterial.getName() + "_handle"), template, RecipeTypes.TOOL_RECIPE);
    }

    private RecipeWrapper createToolWithBindingRecipe(ForgeroTool tool, ToolPartHead head, PrimaryMaterial handleMaterial, PrimaryMaterial bindingMaterial) {
        JsonObject template = JsonParser.parseString(recipeTemplates.get(RecipeTypes.TOOL_WITH_BINDING_RECIPE).toString()).getAsJsonObject();

        template.getAsJsonObject("key").getAsJsonObject("I").remove("item");
        template.getAsJsonObject("key").getAsJsonObject("B").remove("item");
        template.getAsJsonObject("key").getAsJsonObject("H").addProperty("item", new Identifier(ForgeroInitializer.MOD_NAMESPACE, head.getToolPartIdentifier()).toString());
        template.getAsJsonObject("key").getAsJsonObject("I").addProperty("tag", new Identifier(ForgeroInitializer.MOD_NAMESPACE, "handles").toString());
        template.getAsJsonObject("key").getAsJsonObject("B").addProperty("tag", new Identifier(ForgeroInitializer.MOD_NAMESPACE, "bindings").toString());
        template.getAsJsonObject("result").addProperty("item", new Identifier(ForgeroInitializer.MOD_NAMESPACE, tool.getToolIdentifierString()).toString());
        return new RecipeWrapperImpl(new Identifier(ForgeroInitializer.MOD_NAMESPACE, tool.getToolIdentifierString() + "_" + head.getToolPartIdentifier() + "_" + handleMaterial.getName() + "_handle" + "_" + bindingMaterial.getName() + "_binding"), template, RecipeTypes.TOOL_WITH_BINDING_RECIPE);
    }
}