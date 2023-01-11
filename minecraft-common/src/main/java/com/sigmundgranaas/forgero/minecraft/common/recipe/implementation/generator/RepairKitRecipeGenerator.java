package com.sigmundgranaas.forgero.minecraft.common.recipe.implementation.generator;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.settings.ForgeroSettings;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.recipe.RecipeGenerator;
import com.sigmundgranaas.forgero.minecraft.common.recipe.RecipeWrapper;
import com.sigmundgranaas.forgero.minecraft.common.recipe.customrecipe.RecipeTypes;
import net.minecraft.util.Identifier;

public class RepairKitRecipeGenerator implements RecipeGenerator {
    private final RecipeTypes type = RecipeTypes.REPAIR_KIT_RECIPE;
    private final State material;

    private final TemplateGenerator generator;

    public RepairKitRecipeGenerator(State material, TemplateGenerator generator) {
        this.material = material;
        this.generator = generator;
    }

    @Override
    public RecipeWrapper generate() {
        JsonObject template = generator.generate(RecipeTypes.SCHEMATIC_PART_CRAFTING).orElse(new JsonObject());
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
        Identifier id = new Identifier(Forgero.NAMESPACE, material.name() + "_tool_repair_kit_recipe");
        return RecipeWrapper.of(id, template, type);
    }

    @Override
    public boolean isValid() {
        return ForgeroStateRegistry.STATE_TO_CONTAINER.containsKey(material.identifier()) && ForgeroSettings.SETTINGS.getEnableRepairKits();
    }
}
