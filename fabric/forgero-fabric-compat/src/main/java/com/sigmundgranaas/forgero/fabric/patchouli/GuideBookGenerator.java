package com.sigmundgranaas.forgero.fabric.patchouli;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.minecraft.common.recipe.RecipeCreator;
import com.sigmundgranaas.forgero.minecraft.common.recipe.RecipeGenerator;
import com.sigmundgranaas.forgero.minecraft.common.recipe.RecipeWrapper;
import com.sigmundgranaas.forgero.minecraft.common.recipe.customrecipe.RecipeTypes;
import com.sigmundgranaas.forgero.minecraft.common.recipe.implementation.generator.TemplateGenerator;

import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.util.Identifier;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GuideBookGenerator implements RecipeGenerator {

	private final TemplateGenerator generator;
	private final String value;

	public GuideBookGenerator(TemplateGenerator generator, String value) {
		this.generator = generator;
		this.value = value;
	}

	public static void registerGuideBookRecipes() {
		List<RecipeGenerator> recipes = Stream.of("binding", "pickaxe_head", "shovel_head", "axe_head", "hoe_head", "sword_blade", "handle")
				.map(tag -> new GuideBookGenerator(RecipeCreator.INSTANCE.templates(), tag))
				.collect(Collectors.toList());
		RecipeCreator.INSTANCE.registerGenerator(recipes);
	}

	@Override
	public RecipeWrapper generate() {
		JsonObject template = generator.generate(RecipeTypes.TOOLPART_SCHEMATIC_RECIPE).orElse(new JsonObject());
		template.addProperty("type", "patchouli:shapeless_book_recipe");
		JsonArray ingredients = template.getAsJsonArray("ingredients");
		JsonObject tag = new JsonObject();
		tag.addProperty("tag", "forgero:" + value);
		JsonObject book = new JsonObject();
		book.addProperty("item", "minecraft:book");

		ingredients.add(book);
		ingredients.add(tag);
		template.add("ingredients", ingredients);


		template.addProperty("book", "forgero:forgero_guide");
		return RecipeWrapper.of(new Identifier(Forgero.NAMESPACE, "forgero_guide_book_recipe_" + value), template, RecipeTypes.MISC_SHAPELESS);
	}

	@Override
	public boolean isValid() {
		return FabricLoader.getInstance().isModLoaded("patchouli");
	}
}
