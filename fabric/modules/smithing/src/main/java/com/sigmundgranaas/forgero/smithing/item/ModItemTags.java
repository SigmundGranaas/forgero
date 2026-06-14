package com.sigmundgranaas.forgero.smithing.item;

import com.sigmundgranaas.forgero.core.Forgero;

import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class ModItemTags {
	private ModItemTags() {
	}

	public static final TagKey<Item> SMITHING_HAMMERS = of("smithing_hammers");

	private static TagKey<Item> of(String path) {
		return TagKey.of(
				RegistryKeys.ITEM,
				new Identifier(Forgero.NAMESPACE, path)
		);
	}
}
