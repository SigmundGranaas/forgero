package com.sigmundgranaas.forgero.properties.minecraft.loot.function;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.properties.minecraft.loot.LootPropertiesPlugin;
import com.sigmundgranaas.forgero.utility.codec.DispatchCodecUtils;

import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import org.jetbrains.annotations.NotNull;

/**
 * An interface for functions that transform a single ItemStack into another.
 */
public interface ItemFunction {
	@NotNull
	ItemStack apply(ItemStack stack, LootContext context);

	String type();

	static Codec<? extends ItemFunction> getCodec(String type) {
		Codec<? extends ItemFunction> codec = LootPropertiesPlugin.getFunctionCodec(type);
		if (codec == null) {
			throw new IllegalArgumentException("Unknown ItemFunction type: " + type);
		}
		return codec;
	}

	Codec<ItemFunction> CODEC = DispatchCodecUtils.create(ItemFunction::getCodec, ItemFunction::type);
}
