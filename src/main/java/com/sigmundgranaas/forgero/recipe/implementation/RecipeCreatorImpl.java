package com.sigmundgranaas.forgero.recipe.implementation;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.core.material.MaterialCollection;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolCollection;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.tool.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.core.tool.toolpart.ForgeroToolPartCollection;
import com.sigmundgranaas.forgero.core.tool.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartHead;
import com.sigmundgranaas.forgero.recipe.RecipeCreator;
import com.sigmundgranaas.forgero.recipe.RecipeLoader;
import com.sigmundgranaas.forgero.recipe.RecipeTypes;
import com.sigmundgranaas.forgero.recipe.RecipeWrapper;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RecipeCreatorImpl implements RecipeCreator {
    private static RecipeCreator INSTANCE;
    private final Map<RecipeTypes, JsonObject> recipeTemplates;
    private final List<ForgeroTool> tools;
    private final List<ForgeroToolPart> toolParts;
    private final List<PrimaryMaterial> materials;

    public RecipeCreatorImpl(Map<RecipeTypes, JsonObject> recipeTemplates, List<ForgeroTool> tools, List<ForgeroToolPart> toolParts, List<PrimaryMaterial> materials) {
        this.recipeTemplates = recipeTemplates;
        this.tools = tools;
        this.toolParts = toolParts;
        this.materials = materials;
    }

    public static RecipeCreator getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RecipeCreatorImpl(RecipeLoader.INSTANCE.loadRecipeTemplates(), ForgeroToolCollection.INSTANCE.getTools(), ForgeroToolPartCollection.INSTANCE.getToolParts(), MaterialCollection.INSTANCE.getPrimaryMaterialsAsList());
        }
        return INSTANCE;
    }

    @Override
    public List<RecipeWrapper> createRecipes() {
        List<RecipeWrapper> toolRecipes = tools.stream().map(this::createToolRecipe).flatMap(List::stream).collect(Collectors.toList());
        List<RecipeWrapper> toolPartRecipes = toolParts.stream().map(this::createToolPartRecipe).flatMap(List::stream).collect(Collectors.toList());

        return Stream.concat(toolRecipes.stream(), toolPartRecipes.stream()).collect(Collectors.toList());
    }

    private List<RecipeWrapper> createToolPartRecipe(ForgeroToolPart toolPart) {
        if (toolPart.getToolPartType() == ForgeroToolPartTypes.HEAD) {
            return createToolPartHeadRecipe(toolPart);
        } else {
            return createHandleAndBindingRecipe(toolPart);
        }
    }

    private List<RecipeWrapper> createHandleAndBindingRecipe(ForgeroToolPart toolPart) {
        if (toolPart.getToolPartType() == ForgeroToolPartTypes.HANDLE) {
            return createToolPartRecipe(toolPart, RecipeTypes.HANDLE_RECIPE);
        } else {
            return createToolPartRecipe(toolPart, RecipeTypes.BINDING_RECIPE);
        }
    }

    private List<RecipeWrapper> createToolPartRecipe(ForgeroToolPart toolPart, RecipeTypes templateIdentifier) {
        JsonObject template = recipeTemplates.get(templateIdentifier);
        template.getAsJsonObject("key").getAsJsonObject("H").addProperty("item", toolPart.getPrimaryMaterial().getIngredientAsString());
        template.getAsJsonObject("result").addProperty("item", toolPart.getToolPartIdentifier());
        return List.of(new RecipeWrapperImpl(new Identifier(Forgero.MOD_NAMESPACE, toolPart.getToolPartIdentifier()), template));
    }

    private List<RecipeWrapper> createToolPartHeadRecipe(ForgeroToolPart part) {
        if (((ToolPartHead) part).getHeadType() == ForgeroToolTypes.PICKAXE) {
            return createToolPartRecipe(part, RecipeTypes.PICKAXEHEAD_RECIPE);
        } else {
            return createToolPartRecipe(part, RecipeTypes.SHOVELHEAD_RECIPE);
        }
    }

    private List<RecipeWrapper> createToolRecipe(ForgeroTool tool) {
        ArrayList<RecipeWrapper> toolRecipes = new ArrayList<>();
        toolRecipes.add(createBaseToolRecipe(tool));
        materials.forEach(material -> toolRecipes.add(createToolWithBinding(tool, material)));
        return toolRecipes;
    }

    private RecipeWrapper createBaseToolRecipe(ForgeroTool tool) {
        JsonObject template = recipeTemplates.get(RecipeTypes.TOOL_RECIPE);
        template.getAsJsonObject("key").getAsJsonObject("I").addProperty("item", tool.getToolHandle().getToolPartIdentifier());
        template.getAsJsonObject("key").getAsJsonObject("H").addProperty("item", tool.getToolHead().getToolPartIdentifier());
        template.getAsJsonObject("result").addProperty("item", tool.getToolIdentifierString());
        return new RecipeWrapperImpl(new Identifier(Forgero.MOD_NAMESPACE, tool.getToolIdentifierString()), template);
    }

    private RecipeWrapper createToolWithBinding(ForgeroTool tool, PrimaryMaterial material) {
        JsonObject template = recipeTemplates.get(RecipeTypes.TOOL_WITH_BINDING_RECIPE);
        template.getAsJsonObject("key").getAsJsonObject("I").addProperty("item", tool.getToolHandle().getToolPartIdentifier());
        template.getAsJsonObject("key").getAsJsonObject("H").addProperty("item", tool.getToolHead().getToolPartIdentifier());
        template.getAsJsonObject("key").getAsJsonObject("B").addProperty("item", material.getName() + "_binding");
        template.getAsJsonObject("result").addProperty("item", tool.getToolIdentifierString());
        return new RecipeWrapperImpl(new Identifier(Forgero.MOD_NAMESPACE, tool.getToolIdentifierString()), template);
    }
}
