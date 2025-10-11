package com.sigmundgranaas.forgero.properties.minecraft.loot.filter;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.properties.minecraft.loot.LootPropertiesPlugin;
import com.sigmundgranaas.forgero.utility.codec.DispatchCodecUtils;

import net.minecraft.item.ItemStack;

/**
 * An interface for filtering ItemStacks. Used by ItemFunctions to determine if they should apply.
 */
public interface ItemFilter {
	boolean test(ItemStack stack);

	String type();

	static Codec<? extends ItemFilter> getCodec(String type) {
		Codec<? extends ItemFilter> codec = LootPropertiesPlugin.getFilterCodec(type);
		if (codec == null) {
			throw new IllegalArgumentException("Unknown ItemFilter type: " + type);
		}
		return codec;
	}

	Codec<ItemFilter> CODEC = DispatchCodecUtils.create(ItemFilter::getCodec, ItemFilter::type);
}
