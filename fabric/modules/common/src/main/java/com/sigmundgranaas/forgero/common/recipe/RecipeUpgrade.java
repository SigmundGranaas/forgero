package com.sigmundgranaas.forgero.common.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Represents a single upgrade to be applied to a component after it's created.
 *
 * @param slot      The ID of the slot to place the upgrade in.
 * @param component The source of the upgrade component, which can be a recipe character key or a component ID.
 */
public record RecipeUpgrade(String slot, String component) {
	public static final Codec<RecipeUpgrade> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.STRING.fieldOf("slot").forGetter(RecipeUpgrade::slot),
					Codec.STRING.fieldOf("component").forGetter(RecipeUpgrade::component)
			).apply(instance, RecipeUpgrade::new)
	);
}
