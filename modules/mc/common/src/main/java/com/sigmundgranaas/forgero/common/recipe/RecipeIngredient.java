package com.sigmundgranaas.forgero.common.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents a single ingredient in the "key" map of a recipe.
 * It can be defined by either an item ID or a tag ID and can be converted to a vanilla Ingredient.
 */
public final class RecipeIngredient {
	private final Optional<String> item;
	private final Optional<String> tag;

	public RecipeIngredient(Optional<String> item, Optional<String> tag) {
		this.item = item;
		this.tag = tag;
	}

	public static final Codec<RecipeIngredient> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.STRING.optionalFieldOf("item").forGetter(RecipeIngredient::item),
					Codec.STRING.optionalFieldOf("tag").forGetter(RecipeIngredient::tag)
			).apply(instance, RecipeIngredient::new)
	);

	public Ingredient toVanillaIngredient() {
		if (item.isPresent()) {
			Item mcItem = Registries.ITEM.get(new Identifier(item.get()));
			return Ingredient.ofItems(mcItem);
		}
		if (tag.isPresent()) {
			TagKey<Item> tagKey = TagKey.of(Registries.ITEM.getKey(), new Identifier(tag.get()));
			return Ingredient.fromTag(tagKey);
		}
		return Ingredient.EMPTY;
	}

	public Optional<String> item() {
		return item;
	}

	public Optional<String> tag() {
		return tag;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null || obj.getClass() != this.getClass()) return false;
		var that = (RecipeIngredient) obj;
		return Objects.equals(this.item, that.item) &&
				Objects.equals(this.tag, that.tag);
	}

	@Override
	public int hashCode() {
		return Objects.hash(item, tag);
	}

	@Override
	public String toString() {
		return "RecipeIngredient[" +
				"item=" + item + ", " +
				"tag=" + tag + ']';
	}
}
