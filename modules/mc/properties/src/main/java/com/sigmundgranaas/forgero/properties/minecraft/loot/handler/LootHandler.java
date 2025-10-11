package com.sigmundgranaas.forgero.properties.minecraft.loot.handler;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.properties.minecraft.loot.LootPropertiesPlugin;
import com.sigmundgranaas.forgero.utility.codec.DispatchCodecUtils;

import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;

import java.util.List;

/**
 * An interface for handling a list of loot drops.
 */
public interface LootHandler {
	List<ItemStack> handle(List<ItemStack> loot, LootContext context);

	String type();

	static Codec<? extends LootHandler> getCodec(String type) {
		Codec<? extends LootHandler> codec = LootPropertiesPlugin.getHandlerCodec(type);
		if (codec == null) {
			throw new IllegalArgumentException("Unknown LootHandler type: " + type);
		}
		return codec;
	}

	Codec<LootHandler> CODEC = DispatchCodecUtils.create(LootHandler::getCodec, LootHandler::type);
}
