package com.sigmundgranaas.forgero.drp.impl.builder;

import com.sigmundgranaas.forgero.drp.api.tag.ItemTagBuilder;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

/**
 * Implementation of ItemTagBuilder.
 */
public class ItemTagBuilderImpl extends AbstractTagBuilder<ItemTagBuilder> implements ItemTagBuilder {

	public ItemTagBuilderImpl(Identifier id) {
		super(id, "items");
	}

	@Override
	public ItemTagBuilder add(Item item) {
		Identifier itemId = Registries.ITEM.getId(item);
		return add(itemId);
	}

	@Override
	public ItemTagBuilder includeTag(TagKey<Item> tagKey) {
		return includeTag(tagKey.id());
	}
}
