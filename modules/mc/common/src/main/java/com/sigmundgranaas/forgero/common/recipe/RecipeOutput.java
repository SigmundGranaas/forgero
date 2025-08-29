package com.sigmundgranaas.forgero.common.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.core.property.api.PropertyKey;
import com.sigmundgranaas.forgero.core.property.api.codec.KeyMapDispatchCodec;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents the "result" object of a ForgeroShapedRecipe.
 * It contains the ID of the base item/component and optional instructions for
 * modifying its structure, adding upgrades, and applying custom properties.
 */
public record RecipeOutput(
		String item,
		Optional<Map<String, String>> structure,
		Optional<List<RecipeUpgrade>> upgrades,
		Optional<Map<String, List<?>>> properties
) {
	/**
	 * Creates a codec for RecipeOutput.
	 *
	 * @param propertyCodecs A map of registered property codecs, required to parse the 'properties' field.
	 * @return A new codec instance.
	 */
	public static Codec<RecipeOutput> codec(Map<PropertyKey<?>, Codec<? extends List<?>>> propertyCodecs) {
		return RecordCodecBuilder.create(instance ->
				instance.group(
						Codec.STRING.fieldOf("item").forGetter(RecipeOutput::item),
						Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("structure").forGetter(RecipeOutput::structure),
						Codec.list(RecipeUpgrade.CODEC).optionalFieldOf("upgrades").forGetter(RecipeOutput::upgrades),
						new KeyMapDispatchCodec(propertyCodecs).codec().optionalFieldOf("properties").forGetter(RecipeOutput::properties)
				).apply(instance, RecipeOutput::new)
		);
	}
}
