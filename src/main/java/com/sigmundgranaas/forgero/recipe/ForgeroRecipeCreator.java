package com.sigmundgranaas.forgero.recipe;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartHead;
import com.sigmundgranaas.forgero.item.ItemCollection;
import com.sigmundgranaas.forgero.item.ToolPartItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ForgeroRecipeCreator {
    private final Map<Identifier, JsonElement> map;
    private final List<ToolMaterial> materials;

    public ForgeroRecipeCreator(Map<Identifier, JsonElement> map, List<ToolMaterial> materials) {
        this.map = map;
        this.materials = materials;
    }

    public static JsonObject createForgeroBaseToolRecipeJson(ToolPartItem head, ToolPartItem handle, String type) {
        //Creating a new json object, where we will store our recipe.
        JsonObject json = new JsonObject();
        //The "type" of the recipe we are creating. In this case, a shaped recipe.
        json.addProperty("type", "forgero:base_tool_recipe");
        //This creates:
        //"type": "minecraft:crafting_shaped"
        JsonObject headProperty = new JsonObject();
        headProperty.addProperty("item", head.getIdentifier().toString());
        json.add("head", headProperty);

        JsonObject handleProperty = new JsonObject();
        handleProperty.addProperty("item", handle.getIdentifier().toString());
        json.add("handle", handleProperty);
        //We create a new Json Element, and add our crafting pattern to it.

        json.addProperty("outputItem", Forgero.MOD_NAMESPACE + ":" + head.getPrimaryMaterial().getName() + "_" + type + "_" + head.getPart().getToolPartIdentifier() + "_" + handle.getPart().getToolPartIdentifier());
        //Then we add the pattern to our json object.

        return json;
    }

    public static JsonObject createForgeroToolWithBinding(ToolPartItem head, ToolPartItem handle, ToolPartItem binding, String type) {
        //Creating a new json object, where we will store our recipe.
        JsonObject json = new JsonObject();
        //The "type" of the recipe we are creating. In this case, a shaped recipe.
        json.addProperty("type", "forgero:tool_recipe_binding");
        //This creates:
        //"type": "minecraft:crafting_shaped"
        JsonObject headProperty = new JsonObject();
        headProperty.addProperty("item", head.getIdentifier().toString());
        json.add("head", headProperty);

        JsonObject bindingProperty = new JsonObject();
        bindingProperty.addProperty("item", binding.getIdentifier().toString());
        json.add("binding", bindingProperty);

        JsonObject handleProperty = new JsonObject();
        handleProperty.addProperty("item", handle.getIdentifier().toString());
        json.add("handle", handleProperty);
        //We create a new Json Element, and add our crafting pattern to it.

        json.addProperty("outputItem", Forgero.MOD_NAMESPACE + ":" + head.getPrimaryMaterial().getName() + "_" + type + "_" + head.getPart().getToolPartIdentifier() + "_" + handle.getPart().getToolPartIdentifier());
        //Then we add the pattern to our json object.

        return json;
    }

    public static JsonObject createShapedRecipeJson(ArrayList<Character> keys, ArrayList<Identifier> items, ArrayList<String> type, ArrayList<String> pattern, Identifier output) {
        //Creating a new json object, where we will store our recipe.
        JsonObject json = new JsonObject();
        //The "type" of the recipe we are creating. In this case, a shaped recipe.
        json.addProperty("type", "minecraft:crafting_shaped");
        //This creates:
        //"type": "minecraft:crafting_shaped"

        //We create a new Json Element, and add our crafting pattern to it.
        JsonArray jsonArray = new JsonArray();
        for (String s : pattern) {
            jsonArray.add(s);
        }

        //Then we add the pattern to our json object.
        json.add("pattern", jsonArray);
        //This creates:
        //"pattern": [
        //  "###",
        //  " | ",
        //  " | "
        //]

        //Next we need to define what the keys in the pattern are. For this we need different JsonObjects per key definition, and one main JsonObject that will contain all of the defined keys.
        JsonObject individualKey; //Individual key
        JsonObject keyList = new JsonObject(); //The main key object, containing all the keys

        for (int i = 0; i < keys.size(); ++i) {
            individualKey = new JsonObject();
            individualKey.addProperty(type.get(i), items.get(i).toString()); //This will create a key in the form "type": "input", where type is either "item" or "tag", and input is our input item.
            keyList.add(keys.get(i) + "", individualKey); //Then we add this key to the main key object.
            //This will add:
            //"#": { "tag": "c:copper_ingots" }
            //and after that
            //"|": { "item": "minecraft:sticks" }
            //and so on.
        }

        json.add("key", keyList);
        //And so we get:
        //"key": {
        //  "#": {
        //    "tag": "c:copper_ingots"
        //  },
        //  "|": {
        //    "item": "minecraft:stick"
        //  }
        //},

        //Finally, we define our result object
        JsonObject result = new JsonObject();
        result.addProperty("item", output.toString());
        result.addProperty("count", 1);
        json.add("result", result);
        //This creates:
        //"result": {
        //  "item": "modid:copper_pickaxe",
        //  "count": 1
        //}

        return json;
    }

    public void createAndRegisterHandles() {
        for (ToolMaterial material : materials) {
            String element = "item";
            JsonElement ingredient = material.getRepairIngredient().toJson().getAsJsonObject().get("item");
            if (ingredient == null) {
                element = "tag";
                ingredient = material.getRepairIngredient().toJson().getAsJsonObject().get("tag");
            }
            JsonObject recipe = createShapedRecipeJson(
                    Lists.newArrayList(
                            '#'
                    ), //The keys we are using for the input items/tags.
                    Lists.newArrayList(new Identifier(ingredient.getAsString())),
                    //The items/tags we are using as input.
                    Lists.newArrayList(element), //Whether the input we provided is a tag or an item.
                    Lists.newArrayList(
                            "  #",
                            " # ",
                            "#  "

                    ), //The crafting pattern.
                    new Identifier(Forgero.MOD_NAMESPACE, material.toString().toLowerCase(Locale.ROOT) + "_handle") //The crafting output
            );
            map.put(new Identifier(Forgero.MOD_NAMESPACE, material.toString().toLowerCase(Locale.ROOT) + "_handle"), recipe);
        }
    }

    public void createAndRegisterHeads() {
        createAndRegisterPickaxeHeads();
        createAndRegisterShovelHeads();
        createAndRegisterTestRecipe();
    }

    private void createAndRegisterTestRecipe() {
        String element = "item";
        JsonElement ingredient = new JsonPrimitive("oak_planks");

        JsonObject recipe = createShapedRecipeJson(
                Lists.newArrayList(
                        '#'
                ), //The keys we are using for the input items/tags.
                Lists.newArrayList(new Identifier(ingredient.getAsString())),
                //The items/tags we are using as input.
                Lists.newArrayList(element), //Whether the input we provided is a tag or an item.
                Lists.newArrayList(
                        "##"
                ), //The crafting pattern.
                new Identifier(Forgero.MOD_NAMESPACE, "oak" + "_pickaxehead") //The crafting output
        );
        recipe.addProperty("type", "forgero_recipe_test");
        map.put(new Identifier(Forgero.MOD_NAMESPACE, "oak" + "_pickaxehead"), recipe);
    }

    public void createAndRegisterPickaxeHeads() {
        for (ToolMaterial material : materials) {
            String element = "item";
            JsonElement ingredient = material.getRepairIngredient().toJson().getAsJsonObject().get("item");
            if (ingredient == null) {
                element = "tag";
                ingredient = material.getRepairIngredient().toJson().getAsJsonObject().get("tag");
            }
            JsonObject recipe = createShapedRecipeJson(
                    Lists.newArrayList(
                            '#'
                    ), //The keys we are using for the input items/tags.
                    Lists.newArrayList(new Identifier(ingredient.getAsString())),
                    //The items/tags we are using as input.
                    Lists.newArrayList(element), //Whether the input we provided is a tag or an item.
                    Lists.newArrayList(
                            "###"
                    ), //The crafting pattern.
                    new Identifier(Forgero.MOD_NAMESPACE, material.toString().toLowerCase(Locale.ROOT) + "_pickaxehead") //The crafting output
            );
            map.put(new Identifier(Forgero.MOD_NAMESPACE, material.toString().toLowerCase(Locale.ROOT) + "_pickaxehead"), recipe);
        }
    }

    public void createAndRegisterShovelHeads() {
        for (ToolMaterial material : materials) {
            String element = "item";
            JsonElement ingredient = material.getRepairIngredient().toJson().getAsJsonObject().get("item");
            if (ingredient == null) {
                element = "tag";
                ingredient = material.getRepairIngredient().toJson().getAsJsonObject().get("tag");
            }
            JsonObject recipe = createShapedRecipeJson(
                    Lists.newArrayList(
                            '#'
                    ), //The keys we are using for the input items/tags.
                    Lists.newArrayList(new Identifier(ingredient.getAsString())),
                    //The items/tags we are using as input.
                    Lists.newArrayList(element), //Whether the input we provided is a tag or an item.
                    Lists.newArrayList(
                            " # ",
                            "# #"
                    ), //The crafting pattern.
                    new Identifier(Forgero.MOD_NAMESPACE, material.toString().toLowerCase(Locale.ROOT) + "_shovelhead") //The crafting output
            );
            map.put(new Identifier(Forgero.MOD_NAMESPACE, material.toString().toLowerCase(Locale.ROOT) + "_shovelhead"), recipe);
        }
    }

    public void createAndRegisterBindings() {
        for (ToolMaterial material : materials) {
            String element = "item";
            JsonElement ingredient = material.getRepairIngredient().toJson().getAsJsonObject().get("item");
            if (ingredient == null) {
                element = "tag";
                ingredient = material.getRepairIngredient().toJson().getAsJsonObject().get("tag");
            }
            JsonObject recipe = createShapedRecipeJson(
                    Lists.newArrayList(
                            '#'
                    ), //The keys we are using for the input items/tags.
                    Lists.newArrayList(new Identifier(ingredient.getAsString())),
                    //The items/tags we are using as input.
                    Lists.newArrayList(element), //Whether the input we provided is a tag or an item.
                    Lists.newArrayList(
                            " # ",
                            "# #",
                            " # "
                    ), //The crafting pattern.
                    new Identifier(Forgero.MOD_NAMESPACE, material.toString().toLowerCase(Locale.ROOT) + "_binding") //The crafting output
            );
            map.put(new Identifier(Forgero.MOD_NAMESPACE, material.toString().toLowerCase(Locale.ROOT) + "_binding"), recipe);
        }
    }

    public void createAndRegisterTools() {
        for (ToolPartItem handle : ItemCollection.INSTANCE.getToolPartHandles()) {
            for (ToolPartItem head : ItemCollection.INSTANCE.getToolPartHeads()) {
                JsonObject recipe;
                switch (((ToolPartHead) head.getPart()).getHeadType()) {
                    case PICKAXE -> {
                        recipe = createForgeroBaseToolRecipeJson(head, handle, "pickaxe");
                        map.put(new Identifier(Forgero.MOD_NAMESPACE, "pickaxe_" + head.getPart().getToolPartIdentifier() + "_" + handle.getPart().getToolPartIdentifier()), recipe);
                    }
                    case SHOVEL -> {
                        recipe = createForgeroBaseToolRecipeJson(head, handle, "shovel");
                        map.put(new Identifier(Forgero.MOD_NAMESPACE, "shovel_" + head.getPart().getToolPartIdentifier() + "_" + handle.getPart().getToolPartIdentifier()), recipe);
                    }
                    //default -> throw new IllegalStateException("Unexpected value: " + head.getToolPartType());
                }
            }
        }
    }

    public void createAndRegisterToolsWithBinding() {
        for (ToolPartItem handle : ItemCollection.INSTANCE.getToolPartHandles()) {
            for (ToolPartItem head : ItemCollection.INSTANCE.getToolPartHeads()) {
                for (ToolPartItem binding : ItemCollection.INSTANCE.getToolPartBindings()) {
                    JsonObject recipe;
                    switch (((ToolPartHead) head.getPart()).getHeadType()) {
                        case PICKAXE -> {
                            recipe = createForgeroToolWithBinding(head, handle, binding, "pickaxe");
                            map.put(new Identifier(Forgero.MOD_NAMESPACE, "pickaxe_" + head.getPart().getToolPartIdentifier() + "_" + handle.getPart().getToolPartIdentifier() + "_" + binding.getPart().getToolPartIdentifier()), recipe);
                        }
                        case SHOVEL -> {
                            recipe = createForgeroToolWithBinding(head, handle, binding, "shovel");
                            map.put(new Identifier(Forgero.MOD_NAMESPACE, "shovel_" + head.getPart().getToolPartIdentifier() + "_" + handle.getPart().getToolPartIdentifier() + "_" + binding.getPart().getToolPartIdentifier()), recipe);
                        }
                        //default -> throw new IllegalStateException("Unexpected value: " + head.getToolPartType());
                    }
                }
            }
        }
    }
}
