package com.sigmundgranaas.forgero.predicate.minecraft.block;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;

import java.util.Map;

/**
 * A predicate for checking the properties of a BlockState.
 * The keys are property names (e.g., "facing", "age"), and values are the string representation of the desired property value.
 *
 * <p><h3>Example:</h3>
 * {@code { "properties": { "lit": "true", "age": "7" } } }
 */
public record BlockStatePropertyPredicate(Map<String, String> properties) {
	public static final Codec<BlockStatePropertyPredicate> CODEC = Codec.unboundedMap(Codec.STRING, Codec.STRING)
			.xmap(BlockStatePropertyPredicate::new, BlockStatePropertyPredicate::properties);

	public boolean test(BlockState state) {
		for (Map.Entry<String, String> entry : properties.entrySet()) {
			String propName = entry.getKey();
			String expectedValueStr = entry.getValue();

			Property<?> property = state.getBlock().getStateManager().getProperty(propName);

			if (property == null) {
				return false; // The block state does not have this property
			}

			// Get the property's value from the state
			Comparable<?> value = state.get(property);
			// Use the property to convert its value to a string name (e.g., Direction.NORTH -> "north")
			String actualValueStr = getName(property, value);


			if (!actualValueStr.equalsIgnoreCase(expectedValueStr)) {
				return false;
			}
		}
		return true;
	}

	private <T extends Comparable<T>> String getName(Property<T> property, Comparable<?> value) {
		//noinspection unchecked
		return property.name((T) value);
	}
}
