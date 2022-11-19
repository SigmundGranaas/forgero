package com.sigmundgranaas.forgero.recipe.implementation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.gem.Gem;
import com.sigmundgranaas.forgero.item.ForgeroToolItem;
import com.sigmundgranaas.forgero.item.ToolPartItem;
import com.sigmundgranaas.forgero.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.recipe.RecipeCreator;
import com.sigmundgranaas.forgero.recipe.RecipeLoader;
import com.sigmundgranaas.forgero.recipe.RecipeWrapper;
import com.sigmundgranaas.forgero.recipe.customrecipe.RecipeTypes;
import com.sigmundgranaas.forgero.registry.ForgeroItemRegistry;
import com.sigmundgranaas.forgero.resource.data.v2.data.DataResource;
import com.sigmundgranaas.forgero.resource.data.v2.data.IngredientData;
import com.sigmundgranaas.forgero.resource.data.v2.data.RecipeData;
import com.sigmundgranaas.forgero.resource.data.v2.data.SlotData;
import com.sigmundgranaas.forgero.schematic.HeadSchematic;
import com.sigmundgranaas.forgero.schematic.Schematic;
import com.sigmundgranaas.forgero.tool.ForgeroTool;
import com.sigmundgranaas.forgero.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.toolpart.head.ToolPartHead;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.sigmundgranaas.forgero.identifier.Common.ELEMENT_SEPARATOR;
import static com.sigmundgranaas.forgero.util.Identifiers.EMPTY_IDENTIFIER;

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

            INSTANCE = new RecipeCreatorImpl(RecipeLoader.INSTANCE.loadRecipeTemplates(),
                    ForgeroItemRegistry.TOOL_ITEM.stream().map(ForgeroToolItem::getTool).toList(),
                    ForgeroItemRegistry.TOOL_PART_ITEM.stream().map(ToolPartItem::getPart).toList(),
                    ForgeroItemRegistry.MATERIAL.getPrimaryMaterials(),
                    ForgeroItemRegistry.SCHEMATIC.list(),
                    ForgeroItemRegistry.MATERIAL.getSecondaryMaterials(),
                    ForgeroItemRegistry.GEM.list()
            );
        }
        return INSTANCE;
    }

    @Override
    public List<RecipeWrapper> createRecipes() {
        List<RecipeWrapper> toolRecipes = tools.stream().map(this::createToolRecipe).flatMap(List::stream).toList();
        List<RecipeWrapper> toolPartSecondaryMaterialUpgradeRecipe = toolParts.stream().map(this::createSecondaryMaterialUpgradeRecipes).flatMap(List::stream).toList();
        List<RecipeWrapper> toolPartGemUpgradeRecipe = toolParts.stream().filter(toolParts -> toolParts.getSchematic().isUnique()).map(this::createGemUpgradeRecipes).flatMap(List::stream).toList();
        List<RecipeWrapper> toolPartSchematicRecipes = createToolPartSchematicRecipes().stream().toList();
        List<RecipeWrapper> stateRecipes = compositeRecipes();
        List<? extends RecipeWrapper> guidebooksRecipes = new ArrayList<>();
        if (FabricLoader.getInstance().isModLoaded("patchouli")) {
            guidebooksRecipes = createGuideBookRecipes();
        }

        var recipes = List.of(toolRecipes, toolPartSecondaryMaterialUpgradeRecipe, toolPartGemUpgradeRecipe, toolPartSchematicRecipes, guidebooksRecipes, stateRecipes);
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
            return new RecipeWrapperImpl(new Identifier(ForgeroInitializer.MOD_NAMESPACE, "forgero_guide_book_recipe_" + toolPartTag), template, RecipeTypes.MISC_SHAPELESS);
        }).toList();
    }

    private List<RecipeWrapper> createSchematicRecipes(Schematic schematic) {
        return materials.stream().map(material -> createRecipeFromMaterialAndSchematic(material, schematic)).collect(Collectors.toList());
    }

    private List<RecipeWrapper> createToolPartSchematicRecipes() {
        return toolParts.stream().map(this::createRecipeFromToolPart).collect(Collectors.toList());
    }

    private RecipeWrapper createRecipeFromToolPart(ForgeroToolPart toolPart) {
        JsonObject template = JsonParser.parseString(recipeTemplates.get(RecipeTypes.TOOLPART_SCHEMATIC_RECIPE).toString()).getAsJsonObject();
        JsonArray ingredients = template.getAsJsonArray("ingredients");
        String schematicTag;
        if (toolPart.getToolPartType() == ForgeroToolPartTypes.HEAD) {
            schematicTag = switch (((ToolPartHead) toolPart).getToolType()) {
                case PICKAXE -> "forgero:pickaxehead_schematics";
                case SHOVEL -> "forgero:shovelhead_schematics";
                case AXE -> "forgero:axehead_schematics";
                case SWORD -> "forgero:swordhead_schematics";
                case HOE -> "forgero:hoehead_schematics";
            };
        } else if (toolPart.getToolPartType() == ForgeroToolPartTypes.BINDING) {
            schematicTag = "forgero:binding_schematics";
        } else {
            schematicTag = "forgero:handle_schematics";
        }
        JsonObject materialIngredient = new JsonObject();
        if (toolPart.getPrimaryMaterial().getIngredient().tag == null) {
            materialIngredient.addProperty("item", toolPart.getPrimaryMaterial().getIngredient().item);
        } else {
            materialIngredient.addProperty("tag", toolPart.getPrimaryMaterial().getIngredient().tag);
        }

        template.getAsJsonObject("result").addProperty("item", new Identifier("forgero", toolPart.getToolPartIdentifier()).toString());
        for (int i = 0; i < toolPart.getSchematic().getMaterialCount(); i++) {
            ingredients.add(materialIngredient);
        }
        JsonObject schematicTagObject = new JsonObject();
        schematicTagObject.addProperty("tag", schematicTag);
        ingredients.add(schematicTagObject);

        return new RecipeWrapperImpl(new Identifier(ForgeroInitializer.MOD_NAMESPACE, toolPart.getToolPartIdentifier()), template, RecipeTypes.TOOLPART_SCHEMATIC_RECIPE);
    }

    private RecipeWrapper createRecipeFromMaterialAndSchematic(PrimaryMaterial material, Schematic schematic) {
        JsonObject template = JsonParser.parseString(recipeTemplates.get(RecipeTypes.TOOLPART_SCHEMATIC_RECIPE).toString()).getAsJsonObject();
        JsonArray ingredients = template.getAsJsonArray("ingredients");
        String toolPartType;
        if (schematic.getType() == ForgeroToolPartTypes.HEAD) {
            toolPartType = switch (((HeadSchematic) schematic).getToolType()) {
                case PICKAXE -> "pickaxehead";
                case SHOVEL -> "shovelhead";
                case AXE -> "axehead";
                case SWORD -> "swordhead";
                case HOE -> "hoehead";
            };
        } else {
            toolPartType = schematic.getType().getName();
        }

        template.getAsJsonObject("result").addProperty("item", new Identifier("forgero", String.format("%s%s%s%s%s", material.getResourceName(), ELEMENT_SEPARATOR, toolPartType, ELEMENT_SEPARATOR, "default")).toString());
        JsonObject materialIngredient = new JsonObject();
        if (material.getIngredient().tag == null) {
            materialIngredient.addProperty("item", material.getIngredient().item);
        } else {
            materialIngredient.addProperty("tag", material.getIngredient().tag);
        }

        JsonObject schematicIngredient = new JsonObject();
        schematicIngredient.addProperty("item", new Identifier("forgero", schematic.getSchematicIdentifier()).toString());
        for (int i = 0; i < schematic.getMaterialCount(); i++) {
            ingredients.add(materialIngredient);
        }
        ingredients.add(schematicIngredient);
        return new RecipeWrapperImpl(new Identifier(ForgeroInitializer.MOD_NAMESPACE, toolPartType + ELEMENT_SEPARATOR + material.getResourceName() + ELEMENT_SEPARATOR + schematic.getSchematicIdentifier()), template, RecipeTypes.TOOLPART_SCHEMATIC_RECIPE);
    }


    private List<RecipeWrapper> createSecondaryMaterialUpgradeRecipes(ForgeroToolPart toolPart) {
        return secondaryMaterials.stream().filter(secondaryMaterial -> !secondaryMaterial.getResourceName().equals(toolPart.getPrimaryMaterial().getResourceName())).map(secondaryMaterial -> createSecondaryMaterialUpgradeRecipe(toolPart, secondaryMaterial)).collect(Collectors.toList());
    }

    private List<RecipeWrapper> createGemUpgradeRecipes(ForgeroToolPart toolPart) {
        return switch (toolPart.getToolPartType()) {
            case HEAD ->
                    gems.stream().filter(gem -> gem.getPlacement().contains(ForgeroToolPartTypes.HEAD)).map(gem -> createGemUpgradeRecipe(toolPart, gem)).collect(Collectors.toList());
            case HANDLE ->
                    gems.stream().filter(gem -> gem.getPlacement().contains(ForgeroToolPartTypes.HANDLE)).map(gem -> createGemUpgradeRecipe(toolPart, gem)).collect(Collectors.toList());
            case BINDING ->
                    gems.stream().filter(gem -> gem.getPlacement().contains(ForgeroToolPartTypes.BINDING)).map(gem -> createGemUpgradeRecipe(toolPart, gem)).collect(Collectors.toList());
        };
    }

    private RecipeWrapper createSecondaryMaterialUpgradeRecipe(ForgeroToolPart toolPart, SecondaryMaterial material) {
        JsonObject template = JsonParser.parseString(recipeTemplates.get(RecipeTypes.TOOL_PART_SECONDARY_MATERIAL_UPGRADE).toString()).getAsJsonObject();
        template.getAsJsonObject("base").addProperty("item", new Identifier("forgero", toolPart.getToolPartIdentifier()).toString());

        if (material.getIngredient().tag == null) {
            template.getAsJsonObject("addition").addProperty("item", material.getIngredient().item);
        } else {
            template.getAsJsonObject("addition").addProperty("tag", material.getIngredient().tag);
        }

        template.getAsJsonObject("result").addProperty("item", new Identifier("forgero", toolPart.getToolPartIdentifier()).toString());
        return new RecipeWrapperImpl(new Identifier(ForgeroInitializer.MOD_NAMESPACE, toolPart.getToolPartIdentifier() + ELEMENT_SEPARATOR + material.getResourceName() + "_secondary_upgrade"), template, RecipeTypes.TOOL_PART_SECONDARY_MATERIAL_UPGRADE);
    }

    private RecipeWrapper createGemUpgradeRecipe(ForgeroToolPart toolPart, Gem gem) {
        JsonObject template = JsonParser.parseString(recipeTemplates.get(RecipeTypes.TOOL_PART_GEM_UPGRADE).toString()).getAsJsonObject();
        template.getAsJsonObject("base").addProperty("item", new Identifier("forgero", toolPart.getToolPartIdentifier()).toString());
        template.getAsJsonObject("addition").addProperty("item", new Identifier(ForgeroInitializer.MOD_NAMESPACE, gem.getStringIdentifier()).toString());
        template.getAsJsonObject("result").addProperty("item", new Identifier("forgero", toolPart.getToolPartIdentifier()).toString());
        return new RecipeWrapperImpl(new Identifier(ForgeroInitializer.MOD_NAMESPACE, toolPart.getToolPartIdentifier() + ELEMENT_SEPARATOR + gem.getStringIdentifier() + "_gem_upgrade"), template, RecipeTypes.TOOL_PART_GEM_UPGRADE);
    }


    private List<RecipeWrapper> createToolRecipe(ForgeroTool tool) {
        ArrayList<RecipeWrapper> toolRecipes = new ArrayList<>();
        toolParts.stream()
                .filter(part -> part instanceof ToolPartHead)
                .map(ToolPartHead.class::cast)
                .filter(toolPartHead -> toolPartHead.getToolType() == tool.getToolType())
                .filter(toolPartHead -> toolPartHead.getPrimaryMaterial().getResourceName().equals(tool.getMaterial().getResourceName()))
                .forEach(toolPartHead -> {
                    toolRecipes.add(createBaseToolRecipe(tool, toolPartHead));
                    toolRecipes.add(createToolWithBindingRecipe(tool, toolPartHead));
                });
        return toolRecipes;
    }

    private RecipeWrapper createBaseToolRecipe(ForgeroTool tool, ToolPartHead head) {
        JsonObject template = JsonParser.parseString(recipeTemplates.get(RecipeTypes.TOOL_RECIPE).toString()).getAsJsonObject();
        template.getAsJsonObject("key").getAsJsonObject("H").addProperty("item", new Identifier(ForgeroInitializer.MOD_NAMESPACE, head.getToolPartIdentifier()).toString());
        template.getAsJsonObject("key").getAsJsonObject("I").remove("item");
        template.getAsJsonObject("key").getAsJsonObject("I").addProperty("tag", new Identifier(ForgeroInitializer.MOD_NAMESPACE, "handles").toString());
        template.getAsJsonObject("result").addProperty("item", new Identifier(ForgeroInitializer.MOD_NAMESPACE, tool.getToolIdentifierString()).toString());
        return new RecipeWrapperImpl(new Identifier(ForgeroInitializer.MOD_NAMESPACE, tool.getToolIdentifierString() + ELEMENT_SEPARATOR + head.getToolPartIdentifier()), template, RecipeTypes.TOOL_RECIPE);
    }

    private List<RecipeWrapper> compositeRecipes() {
        List<RecipeWrapper> recipes = new ArrayList<>();
        recipes.addAll(ForgeroStateRegistry.CONSTRUCTS.stream().map(this::upgradeRecipes).flatMap(List::stream).toList());
        recipes.addAll(ForgeroStateRegistry.RECIPES.stream().map(this::createRecipes).flatMap(Optional::stream).toList());
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
        var recipes = new ArrayList<RecipeWrapper>();
        recipes.addAll(res.construct().get().slots().stream().map(slot -> compositeUpgrade(slot, ForgeroStateRegistry.ID_MAPPER.get(res.identifier()))).flatMap(Optional::stream).toList());
        return recipes;
    }

    private Optional<RecipeWrapper> compositeRecipe(RecipeData data) {
        JsonObject template = JsonParser.parseString(recipeTemplates.get(RecipeTypes.STATE_CRAFTING_RECIPE).toString()).getAsJsonObject();
        template.getAsJsonObject("key").add("H", ingredientToEntry(data.ingredients().get(0)));
        template.getAsJsonObject("key").add("I", ingredientToEntry(data.ingredients().get(1)));
        template.getAsJsonObject("result").addProperty("item", ForgeroStateRegistry.ID_MAPPER.get(data.target()));
        return Optional.of(new RecipeWrapperImpl(new Identifier(ForgeroStateRegistry.ID_MAPPER.get(data.target())), template, RecipeTypes.STATE_CRAFTING_RECIPE));
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
            object.addProperty("tag", "forgero:" + ForgeroStateRegistry.STATES.get(ForgeroStateRegistry.ID_MAPPER.get(data.id())).get().type().typeName().toLowerCase());
        } else {
            object.addProperty("tag", "forgero:" + data.type().toLowerCase());
        }
        return object;
    }

    private RecipeWrapper createToolWithBindingRecipe(ForgeroTool tool, ToolPartHead head) {
        JsonObject template = JsonParser.parseString(recipeTemplates.get(RecipeTypes.TOOL_WITH_BINDING_RECIPE).toString()).getAsJsonObject();

        template.getAsJsonObject("key").getAsJsonObject("I").remove("item");
        template.getAsJsonObject("key").getAsJsonObject("B").remove("item");
        template.getAsJsonObject("key").getAsJsonObject("H").addProperty("item", new Identifier(ForgeroInitializer.MOD_NAMESPACE, head.getToolPartIdentifier()).toString());
        template.getAsJsonObject("key").getAsJsonObject("I").addProperty("tag", new Identifier(ForgeroInitializer.MOD_NAMESPACE, "handles").toString());
        template.getAsJsonObject("key").getAsJsonObject("B").addProperty("tag", new Identifier(ForgeroInitializer.MOD_NAMESPACE, "bindings").toString());
        template.getAsJsonObject("result").addProperty("item", new Identifier(ForgeroInitializer.MOD_NAMESPACE, tool.getToolIdentifierString()).toString());
        return new RecipeWrapperImpl(new Identifier(ForgeroInitializer.MOD_NAMESPACE, tool.getToolIdentifierString() + ELEMENT_SEPARATOR + head.getToolPartIdentifier() + ELEMENT_SEPARATOR + "binding"), template, RecipeTypes.TOOL_WITH_BINDING_RECIPE);
    }
}