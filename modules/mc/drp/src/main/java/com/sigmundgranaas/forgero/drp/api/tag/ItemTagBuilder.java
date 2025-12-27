package com.sigmundgranaas.forgero.drp.api.tag;

import net.minecraft.item.Item;
import net.minecraft.registry.tag.TagKey;

/**
 * Specialized tag builder for item tags with additional type safety.
 */
public interface ItemTagBuilder extends TagBuilder<ItemTagBuilder> {

	/**
	 * Adds an item directly (uses registry lookup).
	 *
	 * @param item The item to add
	 * @return This builder for chaining
	 */
	ItemTagBuilder add(Item item);

	/**
	 * Includes a vanilla item tag.
	 *
	 * @param tagKey The vanilla tag key
	 * @return This builder for chaining
	 */
	ItemTagBuilder includeTag(TagKey<Item> tagKey);
}
