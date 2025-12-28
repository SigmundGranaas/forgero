package com.sigmundgranaas.forgero.drp.api.recipe;

import com.sigmundgranaas.forgero.drp.impl.builder.IngredientBuilderImpl;
import net.minecraft.util.Identifier;

import java.util.List;

/**
 * Builder for composing complex recipe ingredients.
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * builder.addIngredient(ingredient -> ingredient
 *     .item("minecraft:diamond")
 *     .or()
 *     .item("minecraft:emerald")
 * );
 * }</pre>
 */
public interface IngredientBuilder {

	/**
	 * Creates a new ingredient builder.
	 *
	 * @return A new builder instance
	 */
	static IngredientBuilder create() {
		return new IngredientBuilderImpl();
	}

	/**
	 * Adds an item to this ingredient.
	 *
	 * @param itemId The item identifier
	 * @return This builder for chaining
	 */
	IngredientBuilder item(String itemId);

	/**
	 * Adds an item to this ingredient.
	 *
	 * @param itemId The item identifier
	 * @return This builder for chaining
	 */
	IngredientBuilder item(Identifier itemId);

	/**
	 * Adds a tag to this ingredient (any item in the tag matches).
	 *
	 * @param tagId The tag identifier
	 * @return This builder for chaining
	 */
	IngredientBuilder tag(String tagId);

	/**
	 * Adds a tag to this ingredient.
	 *
	 * @param tagId The tag identifier
	 * @return This builder for chaining
	 */
	IngredientBuilder tag(Identifier tagId);

	/**
	 * Starts an OR clause (the next item/tag is an alternative).
	 *
	 * @return This builder for chaining
	 */
	IngredientBuilder or();

	/**
	 * Gets all entries in this ingredient.
	 *
	 * @return List of ingredient entries
	 */
	List<IngredientEntry> getEntries();

	/**
	 * Represents an entry in an ingredient.
	 */
	interface IngredientEntry {
		/**
		 * Gets the identifier for this entry.
		 */
		Identifier id();

		/**
		 * Whether this entry is a tag reference.
		 */
		boolean isTag();
	}
}
