package com.sigmundgranaas.forgero.drp.api.tag;

import com.sigmundgranaas.forgero.drp.impl.builder.ItemTagBuilderImpl;
import com.sigmundgranaas.forgero.drp.impl.builder.BlockTagBuilderImpl;
import net.minecraft.util.Identifier;

import java.util.List;

/**
 * Type-safe builder for creating Minecraft tags.
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * TagBuilder<ItemTagBuilder> itemTag = TagBuilder.items("forgero:swords")
 *     .add("minecraft:diamond_sword")
 *     .add("minecraft:iron_sword")
 *     .includeTag("forgero:custom_swords")
 *     .setReplace(false);
 *
 * pack.addTag(itemTag);
 * }</pre>
 *
 * @param <T> The concrete builder type for fluent method chaining
 */
public interface TagBuilder<T extends TagBuilder<T>> {

	// ============================================================
	// Factory Methods
	// ============================================================

	/**
	 * Creates a new item tag builder.
	 *
	 * @param id The tag identifier (namespace:path format)
	 * @return A new ItemTagBuilder
	 */
	static ItemTagBuilder items(String id) {
		return items(Identifier.tryParse(id));
	}

	/**
	 * Creates a new item tag builder.
	 *
	 * @param id The tag identifier
	 * @return A new ItemTagBuilder
	 */
	static ItemTagBuilder items(Identifier id) {
		return new ItemTagBuilderImpl(id);
	}

	/**
	 * Creates a new block tag builder.
	 *
	 * @param id The tag identifier
	 * @return A new BlockTagBuilder
	 */
	static BlockTagBuilder blocks(String id) {
		return blocks(Identifier.tryParse(id));
	}

	/**
	 * Creates a new block tag builder.
	 *
	 * @param id The tag identifier
	 * @return A new BlockTagBuilder
	 */
	static BlockTagBuilder blocks(Identifier id) {
		return new BlockTagBuilderImpl(id);
	}

	// ============================================================
	// Common Builder Methods
	// ============================================================

	/**
	 * Gets the identifier for this tag.
	 *
	 * @return The tag identifier
	 */
	Identifier getId();

	/**
	 * Gets the tag type (e.g., "items", "blocks").
	 *
	 * @return The tag type
	 */
	String getType();

	/**
	 * Adds an entry to the tag by string identifier.
	 *
	 * @param id The entry identifier (e.g., "minecraft:diamond")
	 * @return This builder for chaining
	 * @throws IllegalArgumentException if id format is invalid
	 */
	T add(String id);

	/**
	 * Adds an entry to the tag.
	 *
	 * @param id The entry identifier
	 * @return This builder for chaining
	 */
	T add(Identifier id);

	/**
	 * Adds multiple entries to the tag.
	 *
	 * @param ids The entry identifiers
	 * @return This builder for chaining
	 */
	T addAll(List<Identifier> ids);

	/**
	 * Adds multiple entries to the tag by string identifiers.
	 *
	 * @param ids The entry identifiers
	 * @return This builder for chaining
	 */
	T addAll(String... ids);

	/**
	 * Includes another tag's contents in this tag.
	 *
	 * @param tagId The tag to include (will be prefixed with #)
	 * @return This builder for chaining
	 */
	T includeTag(String tagId);

	/**
	 * Includes another tag's contents in this tag.
	 *
	 * @param tagId The tag to include
	 * @return This builder for chaining
	 */
	T includeTag(Identifier tagId);

	/**
	 * Sets whether this tag replaces or merges with existing tags.
	 *
	 * @param replace true to replace, false to merge (default)
	 * @return This builder for chaining
	 */
	T setReplace(boolean replace);

	/**
	 * Adds an optional entry that won't cause errors if missing.
	 *
	 * @param id The optional entry identifier
	 * @return This builder for chaining
	 */
	T addOptional(Identifier id);

	/**
	 * Adds an optional entry that won't cause errors if missing.
	 *
	 * @param id The optional entry identifier
	 * @return This builder for chaining
	 */
	T addOptional(String id);

	/**
	 * Includes an optional tag that won't cause errors if missing.
	 *
	 * @param tagId The optional tag to include
	 * @return This builder for chaining
	 */
	T includeOptionalTag(Identifier tagId);

	/**
	 * Returns whether this tag should replace existing tags.
	 *
	 * @return true if replacing, false if merging
	 */
	boolean isReplace();

	/**
	 * Gets all entries added to this tag.
	 *
	 * @return List of tag entries
	 */
	List<TagEntry> getEntries();

	/**
	 * Represents an entry in a tag.
	 */
	interface TagEntry {
		/**
		 * Gets the identifier for this entry.
		 */
		Identifier id();

		/**
		 * Whether this entry is a tag reference (prefixed with #).
		 */
		boolean isTag();

		/**
		 * Whether this entry is optional.
		 */
		boolean isOptional();
	}
}
