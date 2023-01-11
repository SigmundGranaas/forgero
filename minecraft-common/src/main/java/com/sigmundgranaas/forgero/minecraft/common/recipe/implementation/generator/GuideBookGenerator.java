package com.sigmundgranaas.forgero.minecraft.common.recipe.implementation.generator;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.minecraft.common.recipe.RecipeGenerator;
import com.sigmundgranaas.forgero.minecraft.common.recipe.RecipeWrapper;
import com.sigmundgranaas.forgero.minecraft.common.recipe.customrecipe.RecipeTypes;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

public class GuideBookGenerator implements RecipeGenerator {
    private final TemplateGenerator generator;
    private final String value;

    public GuideBookGenerator(TemplateGenerator generator, String value) {
        this.generator = generator;
        this.value = value;
    }

    @Override
    public RecipeWrapper generate() {
        JsonObject template = generator.generate(RecipeTypes.TOOLPART_SCHEMATIC_RECIPE).orElse(new JsonObject());
        template.addProperty("type", "patchouli:shapeless_book_recipe");
        JsonArray ingredients = template.getAsJsonArray("ingredients");
        JsonObject tag = new JsonObject();
        tag.addProperty("tag", value);
        JsonObject book = new JsonObject();
        book.addProperty("item", "minecraft:book");

        ingredients.add(book);
        ingredients.add(tag);
        template.add("ingredients", ingredients);

        template.addProperty("book", "forgero:forgero_guide");

        Identifier id = new Identifier(Forgero.NAMESPACE, "forgero_guide_book_recipe_" + value);

        return RecipeWrapper.of(id, tag, RecipeTypes.MISC_SHAPELESS);
    }

    @Override
    public boolean isValid() {
        return FabricLoader.getInstance().isModLoaded("patchouli");
    }
}
