package com.sigmundgranaas.forgero.drp.api.lang;

import com.sigmundgranaas.forgero.drp.impl.builder.LanguageBuilderImpl;
import net.minecraft.util.Identifier;

import java.util.Map;

/**
 * Builder for language translation entries.
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * pack.addLanguage("en_us", lang -> lang
 *     .item(new Identifier("forgero", "iron_pickaxe"), "Iron Pickaxe")
 *     .item(new Identifier("forgero", "diamond_blade"), "Diamond Blade")
 *     .tooltip("forgero.tooltip.durability", "Durability: %s")
 * );
 * }</pre>
 */
public interface LanguageBuilder {

	/**
	 * Creates a new language builder.
	 *
	 * @return A new builder instance
	 */
	static LanguageBuilder create() {
		return new LanguageBuilderImpl();
	}

	/**
	 * Adds a translation for an item.
	 *
	 * @param itemId      The item identifier
	 * @param translation The translated display name
	 * @return This builder for chaining
	 */
	LanguageBuilder item(Identifier itemId, String translation);

	/**
	 * Adds a translation for an item.
	 *
	 * @param itemId      The item identifier (namespace:path format)
	 * @param translation The translated display name
	 * @return This builder for chaining
	 */
	default LanguageBuilder item(String itemId, String translation) {
		return item(Identifier.tryParse(itemId), translation);
	}

	/**
	 * Adds a translation for a block.
	 *
	 * @param blockId     The block identifier
	 * @param translation The translated display name
	 * @return This builder for chaining
	 */
	LanguageBuilder block(Identifier blockId, String translation);

	/**
	 * Adds a translation for a block.
	 *
	 * @param blockId     The block identifier (namespace:path format)
	 * @param translation The translated display name
	 * @return This builder for chaining
	 */
	default LanguageBuilder block(String blockId, String translation) {
		return block(Identifier.tryParse(blockId), translation);
	}

	/**
	 * Adds a translation for an entity.
	 *
	 * @param entityId    The entity type identifier
	 * @param translation The translated display name
	 * @return This builder for chaining
	 */
	LanguageBuilder entity(Identifier entityId, String translation);

	/**
	 * Adds a translation for an entity.
	 *
	 * @param entityId    The entity type identifier (namespace:path format)
	 * @param translation The translated display name
	 * @return This builder for chaining
	 */
	default LanguageBuilder entity(String entityId, String translation) {
		return entity(Identifier.tryParse(entityId), translation);
	}

	/**
	 * Adds a tooltip translation.
	 *
	 * @param key         The tooltip key (e.g., "forgero.tooltip.durability")
	 * @param translation The translated tooltip text
	 * @return This builder for chaining
	 */
	LanguageBuilder tooltip(String key, String translation);

	/**
	 * Adds a generic translation.
	 *
	 * @param key         The full translation key
	 * @param translation The translated text
	 * @return This builder for chaining
	 */
	LanguageBuilder add(String key, String translation);

	/**
	 * Merges another language builder's entries.
	 *
	 * @param other The builder to merge from
	 * @return This builder for chaining
	 */
	LanguageBuilder merge(LanguageBuilder other);

	/**
	 * Gets all translation entries.
	 *
	 * @return Map of translation key to translated value
	 */
	Map<String, String> getEntries();
}
