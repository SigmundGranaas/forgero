package com.sigmundgranaas.forgero.properties.minecraft.loot;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.core.property.api.codec.ListCodecWrapper;
import com.sigmundgranaas.forgero.loader.api.DataPlugin;
import com.sigmundgranaas.forgero.loader.api.PluginRegistrationContext;
import com.sigmundgranaas.forgero.properties.minecraft.loot.filter.IsItemFilter;
import com.sigmundgranaas.forgero.properties.minecraft.loot.filter.ItemFilter;
import com.sigmundgranaas.forgero.properties.minecraft.loot.filter.TagFilter;
import com.sigmundgranaas.forgero.properties.minecraft.loot.function.AutoSmeltFunction;
import com.sigmundgranaas.forgero.properties.minecraft.loot.function.ItemFunction;
import com.sigmundgranaas.forgero.properties.minecraft.loot.function.ItemTransformFunction;
import com.sigmundgranaas.forgero.properties.minecraft.loot.handler.ApplyFunctionsHandler;
import com.sigmundgranaas.forgero.properties.minecraft.loot.handler.LootHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LootPropertiesPlugin implements DataPlugin {
	private static final Map<String, Codec<? extends LootHandler>> HANDLERS = new ConcurrentHashMap<>();
	private static final Map<String, Codec<? extends ItemFunction>> FUNCTIONS = new ConcurrentHashMap<>();
	private static final Map<String, Codec<? extends ItemFilter>> FILTERS = new ConcurrentHashMap<>();

	static {
		registerHandler(ApplyFunctionsHandler.TYPE, ApplyFunctionsHandler.CODEC);

		registerFunction(AutoSmeltFunction.TYPE, AutoSmeltFunction.CODEC);
		registerFunction(ItemTransformFunction.TYPE, ItemTransformFunction.CODEC);

		registerFilter(TagFilter.TYPE, TagFilter.CODEC);
		registerFilter(IsItemFilter.TYPE, IsItemFilter.CODEC); // Added new filter
	}

	public static void registerHandler(String type, Codec<? extends LootHandler> codec) {
		HANDLERS.put(type, codec);
	}

	public static Codec<? extends LootHandler> getHandlerCodec(String type) {
		return HANDLERS.get(type);
	}

	public static void registerFunction(String type, Codec<? extends ItemFunction> codec) {
		FUNCTIONS.put(type, codec);
	}

	public static Codec<? extends ItemFunction> getFunctionCodec(String type) {
		return FUNCTIONS.get(type);
	}

	public static void registerFilter(String type, Codec<? extends ItemFilter> codec) {
		FILTERS.put(type, codec);
	}

	public static Codec<? extends ItemFilter> getFilterCodec(String type) {
		return FILTERS.get(type);
	}


	@Override
	public void register(PluginRegistrationContext context) {
		context.registerPropertyCodec(
				LootProperty.PROPERTY_KEY,
				conditionCodecSupplier -> ListCodecWrapper.of(LootProperty.codec(conditionCodecSupplier.get()))
		);
	}

	@Override
	public String getId() {
		return "forgero:loot-properties";
	}
}
