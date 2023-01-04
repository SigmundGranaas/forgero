package com.sigmundgranaas.forgero.minecraft.common.recipe.implementation;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.DataResource;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.IngredientData;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.RecipeData;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.SlotData;
import com.sigmundgranaas.forgero.core.settings.ForgeroSettings;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.minecraft.common.recipe.RecipeCreator;
import com.sigmundgranaas.forgero.minecraft.common.recipe.RecipeLoader;
import com.sigmundgranaas.forgero.minecraft.common.recipe.RecipeWrapper;
import com.sigmundgranaas.forgero.minecraft.common.recipe.customrecipe.RecipeTypes;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.sigmundgranaas.forgero.core.identifier.Common.ELEMENT_SEPARATOR;
import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;

//TODO Lots of deprecated methods are used to create recipes.
//TODO THIS IS NOT OBJECT ORIENTED AT ALL, rework to use objects
public record RecipeCreatorImpl(
        Map<RecipeTypes, JsonObject> recipeTemplates
) implements RecipeCreator {
    private static RecipeCreator INSTANCE;

    public static RecipeCreator getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RecipeCreatorImpl(RecipeLoader.INSTANCE.loadRecipeTemplates()
            );
        }
        return INSTANCE;
    }

    @Override
    public List<RecipeWrapper> createRecipes() {
        List<RecipeWrapper> stateRecipes = compositeRecipes();

        var repairKits = ForgeroSettings.SETTINGS.getEnableRepairKits() ? createRepairKitToolRecipes() : new ArrayList<RecipeWrapper>();
        List<? extends RecipeWrapper> guidebooksRecipes = new ArrayList<>();
        if (FabricLoader.getInstance().isModLoaded("patchouli")) {
            guidebooksRecipes = createGuideBookRecipes();
        }

        var recipes = List.of(guidebooksRecipes, stateRecipes, repairKits);
        return recipes.stream().flatMap(List::stream).collect(Collectors.toList());
    }


    private List<? extends RecipeWrapper> createGuideBookRecipes() {
        var tags = List.of("bindings", "heads", "handles");
        return tags.stream().map(toolPartTag -> {
            JsonObject template = JsonParser.parseString(recipeTemplates.get(RecipeTypes.TOOLPART_SCHEMATIC_RECIPE).toString()).getAsJsonObject();
            template.addProperty("type", "patchouli:shapeless_book_recipe");
            JsonArray ingredients = template.getAsJsonArray("ingredients");
            JsonObject tag = new JsonObject();
            tag.addProperty("tag", "forgero:" + toolPartTag);
            JsonObject book = new JsonObject();
            book.addProperty("item", "minecraft:book");

            ingredients.add(book);
            ingredients.add(tag);
            template.add("ingredients", ingredients);


            template.addProperty("book", "forgero:forgero_guide");
            return new RecipeWrapperImpl(new Identifier(Forgero.NAMESPACE, "forgero_guide_book_recipe_" + toolPartTag), template, RecipeTypes.MISC_SHAPELESS);
        }).toList();
    }

    private List<RecipeWrapper> compositeRecipes() {
        List<RecipeWrapper> recipes = new ArrayList<>();
        recipes.addAll(ForgeroStateRegistry.CONSTRUCTS.stream().map(this::upgradeRecipes).flatMap(List::stream).toList());
        recipes.addAll(ForgeroStateRegistry.RECIPES.stream().map(this::createRecipes).flatMap(Optional::stream).toList());
        return recipes;
    }

    private List<RecipeWrapper> createRepairKitToolRecipes() {
        var materials = ForgeroStateRegistry.TREE.find(Type.TOOL_MATERIAL)
                .map(node -> node.getResources(State.class))
                .orElse(ImmutableList.<State>builder().build());
        var recipes = new ArrayList<RecipeWrapper>();
        for (State material : materials) {
            if (ForgeroStateRegistry.STATE_TO_CONTAINER.containsKey(material.identifier())) {

                JsonObject template = JsonParser.parseString(recipeTemplates.get(RecipeTypes.TOOLPART_SCHEMATIC_RECIPE).toString()).getAsJsonObject();
                template.addProperty("type", "forgero:repair_kit_recipe");
                JsonArray ingredients = template.getAsJsonArray("ingredients");
                JsonObject toolTag = new JsonObject();
                toolTag.addProperty("tag", "forgero:" + String.format("%s_tool", material.name()));
                JsonObject repairKit = new JsonObject();
                repairKit.addProperty("item", "forgero:" + material.name() + "_repair_kit");

                ingredients.add(toolTag);
                ingredients.add(repairKit);
                template.add("ingredients", ingredients);

                template.getAsJsonObject("result").addProperty("item", "forgero:" + material.name() + "_repair_kit");
                recipes.add(new RecipeWrapperImpl(new Identifier(Forgero.NAMESPACE, material.name() + "_tool_repair_kit_recipe"), template, RecipeTypes.REPAIR_KIT_RECIPE));
            }
        }


        return recipes;
    }

    private Optional<RecipeWrapper> createRecipes(RecipeData res) {
        RecipeTypes type = RecipeTypes.valueOf(res.type());
        if (type == RecipeTypes.SCHEMATIC_PART_CRAFTING) {
            return Optional.of(schematicPartCrafting(res));
        } else if (type == RecipeTypes.STATE_CRAFTING_RECIPE) {
            return compositeRecipe(res);
        }
        return Optional.empty();
    }

    private List<RecipeWrapper> upgradeRecipes(DataResource res) {
        var recipes = new ArrayList<RecipeWrapper>(res.construct().get().slots().stream().map(slot -> compositeUpgrade(slot, ForgeroStateRegistry.ID_MAPPER.get(res.identifier()))).flatMap(Optional::stream).toList());
        return recipes;
    }

    private Optional<RecipeWrapper> compositeRecipe(RecipeData data) {
        JsonObject template = JsonParser.parseString(recipeTemplates.get(RecipeTypes.STATE_CRAFTING_RECIPE).toString()).getAsJsonObject();
        template.getAsJsonObject("key").add("H", ingredientToEntry(data.ingredients().get(0)));
        template.getAsJsonObject("key").add("I", ingredientToEntry(data.ingredients().get(1)));
        template.getAsJsonObject("result").addProperty("item", ForgeroStateRegistry.ID_MAPPER.get(data.target()));
        return Optional.ofNullable(ForgeroStateRegistry.ID_MAPPER.get(data.target())).map(id -> new RecipeWrapperImpl(new Identifier(id), template, RecipeTypes.STATE_CRAFTING_RECIPE));
    }

    private RecipeWrapper schematicPartCrafting(RecipeData data) {
        JsonObject template = JsonParser.parseString(recipeTemplates.get(RecipeTypes.SCHEMATIC_PART_CRAFTING).toString()).getAsJsonObject();
        IntStream.range(0, data.ingredients().get(0).amount()).forEach(i -> template.getAsJsonArray("ingredients").add(ingredientToEntry(data.ingredients().get(0))));
        IntStream.range(0, data.ingredients().get(1).amount()).forEach(i -> template.getAsJsonArray("ingredients").add(ingredientToEntry(data.ingredients().get(1))));

        template.getAsJsonObject("result").addProperty("item", ForgeroStateRegistry.ID_MAPPER.get(data.target()));
        return new RecipeWrapperImpl(new Identifier(ForgeroStateRegistry.ID_MAPPER.get(data.target())), template, RecipeTypes.SCHEMATIC_PART_CRAFTING);
    }

    private Optional<RecipeWrapper> compositeUpgrade(SlotData data, String target) {
        JsonObject template = JsonParser.parseString(recipeTemplates.get(RecipeTypes.STATE_UPGRADE_RECIPE).toString()).getAsJsonObject();
        template.getAsJsonObject("base").addProperty("item", target);
        template.getAsJsonObject("addition").addProperty("tag", "forgero:" + data.type().toLowerCase());
        template.getAsJsonObject("result").addProperty("item", target);
        return Optional.of(new RecipeWrapperImpl(new Identifier(target + ELEMENT_SEPARATOR + data.type().toLowerCase()), template, RecipeTypes.STATE_UPGRADE_RECIPE));
    }

    private JsonObject ingredientToEntry(IngredientData data) {
        var object = new JsonObject();
        if (data.unique() && !data.id().equals(EMPTY_IDENTIFIER)) {
            String id;
            if (ForgeroStateRegistry.STATE_TO_CONTAINER.containsKey(ForgeroStateRegistry.ID_MAPPER.get(data.id()))) {
                id = ForgeroStateRegistry.STATE_TO_CONTAINER.get(ForgeroStateRegistry.ID_MAPPER.get(data.id()));
            } else if (ForgeroStateRegistry.STATE_TO_CONTAINER.containsValue(data.id())) {
                id = ForgeroStateRegistry.STATE_TO_CONTAINER.entrySet().stream().filter(entry -> entry.getValue().equals(data.id())).map(Map.Entry::getKey).findFirst().orElse(data.id());
            } else {
                id = data.id();
            }
            object.addProperty("item", id);
        } else if (!data.id().equals(EMPTY_IDENTIFIER)) {
            object.addProperty("tag", "forgero:" + ForgeroStateRegistry.stateFinder().find(ForgeroStateRegistry.ID_MAPPER.get(data.id())).get().type().typeName().toLowerCase());
        } else {
            object.addProperty("tag", "forgero:" + data.type().toLowerCase());
        }
        return object;
    }
}