package com.sigmundgranaas.forgero.recipe.implementation;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.material.MaterialCollection;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.tool.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartHead;
import com.sigmundgranaas.forgero.item.ForgeroToolItem;
import com.sigmundgranaas.forgero.item.ItemCollection;
import com.sigmundgranaas.forgero.item.ToolPartItem;
import com.sigmundgranaas.forgero.recipe.RecipeCreator;
import com.sigmundgranaas.forgero.recipe.RecipeLoader;
import com.sigmundgranaas.forgero.recipe.RecipeTypes;
import com.sigmundgranaas.forgero.recipe.RecipeWrapper;
import net.minecraft.item.Item;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RecipeCreatorImpl implements RecipeCreator {
    private static RecipeCreator INSTANCE;
    private final Map<RecipeTypes, JsonObject> recipeTemplates;
    private final List<Item> itemCollection;
    private final List<PrimaryMaterial> materials;

    public RecipeCreatorImpl(Map<RecipeTypes, JsonObject> recipeTemplates, List<Item> itemCollection, List<PrimaryMaterial> materials) {
        this.recipeTemplates = recipeTemplates;
        this.itemCollection = itemCollection;
        this.materials = materials;
    }

    public static RecipeCreator getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RecipeCreatorImpl(RecipeLoader.INSTANCE.loadRecipeTemplates(), Stream.concat(ItemCollection.INSTANCE.getTools().stream(), ItemCollection.INSTANCE.getToolParts().stream()).collect(Collectors.toList()), MaterialCollection.INSTANCE.getPrimaryMaterialsAsList());
        }
        return INSTANCE;
    }

    @Override
    public List<RecipeWrapper> createRecipes() {
        return itemCollection.stream().map(this::createRecipe).flatMap(List::stream).collect(Collectors.toList());
    }

    private List<RecipeWrapper> createRecipe(Item item) {
        if (item instanceof ForgeroToolItem) {
            return createItemRecipe((ForgeroToolItem) item);
        } else {
            return createToolPartRecipe((ToolPartItem) item);
        }
    }

    private List<RecipeWrapper> createToolPartRecipe(ToolPartItem toolPartItem) {
        if (toolPartItem.getPart().getToolPartType() == ForgeroToolPartTypes.HEAD) {
            return createToolPartHeadRecipe((ToolPartItem) toolPartItem.getPart());
        } else {
            return createHandleAndBindingRecipe(toolPartItem);
        }
    }

    private List<RecipeWrapper> createHandleAndBindingRecipe(ToolPartItem toolPartItem) {
        if (toolPartItem.getPart().getToolPartType() == ForgeroToolPartTypes.HANDLE) {
            return createToolPartRecipe(toolPartItem, RecipeTypes.HANDLE_RECIPE);
        } else {
            return createToolPartRecipe(toolPartItem, RecipeTypes.BINDING_RECIPE);
        }
    }

    private List<RecipeWrapper> createToolPartRecipe(ToolPartItem toolPartItem, RecipeTypes templateIdentifier) {
        JsonObject template = recipeTemplates.get(templateIdentifier);
        template.getAsJsonObject("key").getAsJsonObject("H").add("item", toolPartItem.getPrimaryMaterial().getRepairIngredient().toJson());
        template.getAsJsonObject("result").addProperty("item", toolPartItem.getPart().toString());
        return List.of(new RecipeWrapperImpl(toolPartItem.getIdentifier(), template));
    }

    private List<RecipeWrapper> createToolPartHeadRecipe(ToolPartItem part) {
        if (((ToolPartHead) part.getPart()).getHeadType() == ForgeroToolTypes.PICKAXE) {
            return createToolPartRecipe(part, RecipeTypes.PICKAXEHEAD_RECIPE);
        } else {
            return createToolPartRecipe(part, RecipeTypes.SHOVELHEAD_RECIPE);
        }
    }

    private List<RecipeWrapper> createItemRecipe(ForgeroToolItem item) {
        List<RecipeWrapper> toolRecipes = List.of(createBaseToolRecipe(item));
        materials.forEach(material -> toolRecipes.add(createToolWithBinding(item, material)));
        return toolRecipes;
    }

    private RecipeWrapper createBaseToolRecipe(ForgeroToolItem item) {
        JsonObject template = recipeTemplates.get(RecipeTypes.TOOL_RECIPE);
        template.getAsJsonObject("key").getAsJsonObject("I").addProperty("item", item.getTool().getToolHandle().getToolPartIdentifier());
        template.getAsJsonObject("key").getAsJsonObject("H").addProperty("item", item.getTool().getToolHead().getToolPartIdentifier());
        template.getAsJsonObject("result").addProperty("item", item.getTool().getToolIdentifierString());
        return new RecipeWrapperImpl(item.getIdentifier(), template);
    }

    private RecipeWrapper createToolWithBinding(ForgeroToolItem item, PrimaryMaterial material) {
        JsonObject template = recipeTemplates.get(RecipeTypes.TOOL_WITH_BINDING_RECIPE);
        template.getAsJsonObject("key").getAsJsonObject("I").addProperty("item", item.getTool().getToolHandle().getToolPartIdentifier());
        template.getAsJsonObject("key").getAsJsonObject("H").addProperty("item", item.getTool().getToolHead().getToolPartIdentifier());
        template.getAsJsonObject("key").getAsJsonObject("B").addProperty("item", material.getName() + "_binding");
        template.getAsJsonObject("result").addProperty("item", item.getTool().getToolIdentifierString());
        return new RecipeWrapperImpl(item.getIdentifier(), template);
    }
}
