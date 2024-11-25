package com.sigmundgranaas.forgero.dynamicresourcepack.api.resource;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;

public class ShapelessRecipeResource {
	public static final Codec<ShapelessRecipeResource> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.list(IngredientEntry.CODEC).fieldOf("ingredients").forGetter(ShapelessRecipeResource::getIngredients),
			ResultEntry.CODEC.fieldOf("result").forGetter(ShapelessRecipeResource::getResult)
	).apply(instance, ShapelessRecipeResource::new));

	private final List<IngredientEntry> ingredients;
	private final ResultEntry result;

	public ShapelessRecipeResource(List<IngredientEntry> ingredients, ResultEntry result) {
		this.ingredients = ingredients;
		this.result = result;
	}

	public List<IngredientEntry> getIngredients() {
		return ingredients;
	}

	public ResultEntry getResult() {
		return result;
	}

	public record IngredientEntry(String item, String tag) {
		public static final Codec<IngredientEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.optionalFieldOf("item", "").forGetter(IngredientEntry::item),
				Codec.STRING.optionalFieldOf("tag", "").forGetter(IngredientEntry::tag)
		).apply(instance, IngredientEntry::new));
	}

	public record ResultEntry(String item, int count) {
		public static final Codec<ResultEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("item").forGetter(ResultEntry::item),
				Codec.INT.optionalFieldOf("count", 1).forGetter(ResultEntry::count)
		).apply(instance, ResultEntry::new));
	}

	public static class Builder {
		private final ArrayList<IngredientEntry> ingredients = new ArrayList<>();
		private ResultEntry result;

		public Builder addIngredient(String item) {
			ingredients.add(new IngredientEntry(item, ""));
			return this;
		}

		public Builder addTagIngredient(String tag) {
			ingredients.add(new IngredientEntry("", tag));
			return this;
		}

		public Builder result(String item, int count) {
			this.result = new ResultEntry(item, count);
			return this;
		}

		public ShapelessRecipeResource build() {
			return new ShapelessRecipeResource(ingredients, result);
		}
	}
}
