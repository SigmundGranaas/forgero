package com.sigmundgranaas.forgero.properties.minecraft.loot.filter;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.common.filter.TypedFilter;
import com.sigmundgranaas.forgero.properties.minecraft.loot.LootPropertiesPlugin;
import com.sigmundgranaas.forgero.utility.codec.DispatchCodecUtils;

import net.minecraft.item.ItemStack;

/**
 * Filters ItemStacks based on various criteria.
 * <p>
 * Used by ItemFunctions to determine if they should apply to a given item.
 * This filter type has no context (uses {@link Void}).
 *
 * @see com.sigmundgranaas.forgero.common.filter.Filter
 */
public interface ItemFilter extends TypedFilter<ItemStack, Void> {

	/**
	 * Tests whether the given ItemStack passes this filter.
	 *
	 * @param stack The ItemStack to test
	 * @return true if the stack passes the filter
	 */
	boolean test(ItemStack stack);

	/**
	 * Implementation of the base Filter interface.
	 * Delegates to the simple {@link #test(ItemStack)} method.
	 */
	@Override
	default boolean test(Void context, ItemStack target) {
		return test(target);
	}

	static Codec<? extends ItemFilter> getCodec(String type) {
		Codec<? extends ItemFilter> codec = LootPropertiesPlugin.getFilterCodec(type);
		if (codec == null) {
			throw new IllegalArgumentException("Unknown ItemFilter type: " + type);
		}
		return codec;
	}

	Codec<ItemFilter> CODEC = DispatchCodecUtils.create(ItemFilter::getCodec, ItemFilter::type);
}
