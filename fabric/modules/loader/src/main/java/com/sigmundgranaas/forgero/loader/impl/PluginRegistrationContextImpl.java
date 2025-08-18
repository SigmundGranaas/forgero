package com.sigmundgranaas.forgero.loader.impl;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.core.property.condition.Condition;
import com.sigmundgranaas.forgero.loader.api.ItemCreator;
import com.sigmundgranaas.forgero.loader.api.PluginRegistrationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Implementation of the registration context that collects all plugin registrations.
 */
public class PluginRegistrationContextImpl implements PluginRegistrationContext {
	private static final Logger LOGGER = LoggerFactory.getLogger(PluginRegistrationContextImpl.class);

	private final Map<String, ItemCreator> itemCreators = new HashMap<>();
	private final Map<String, Object> componentConstructors = new HashMap<>();
	private final Map<String, Object> conditionCodecs = new HashMap<>();
	private final Map<String, Function<Supplier<Codec<Condition>>, Codec<? extends List<?>>>> propertyCodecBuilders = new HashMap<>();


	@Override
	public void registerItemCreator(String itemClass, ItemCreator creator) {
		if (itemCreators.containsKey(itemClass)) {
			LOGGER.warn("Item creator for class '{}' is being overwritten", itemClass);
		}
		itemCreators.put(itemClass, creator);
		LOGGER.trace("Registered item creator for class '{}'", itemClass);
	}


	@Override
	public void registerConditionCodec(String type, Object codec) {
		if (conditionCodecs.containsKey(type)) {
			LOGGER.warn("Condition codec for type '{}' is being overwritten", type);
		}
		conditionCodecs.put(type, codec);
		LOGGER.trace("Registered condition codec for type '{}'", type);
	}

	@Override
	public void registerPropertyCodec(String key, Function<Supplier<Codec<Condition>>, Codec<? extends List<?>>> codecBuilder) {
		if (propertyCodecBuilders.containsKey(key)) {
			LOGGER.warn("Property codec builder for key '{}' is being overwritten by a new plugin.", key);
		}
		propertyCodecBuilders.put(key, codecBuilder);
		LOGGER.trace("Registered property codec builder for key '{}'", key);
	}


	public Map<String, ItemCreator> getItemCreators() {
		return new HashMap<>(itemCreators);
	}

	public Map<String, Object> getComponentConstructors() {
		return new HashMap<>(componentConstructors);
	}

	public Map<String, Object> getConditionCodecs() {
		return new HashMap<>(conditionCodecs);
	}

	public Map<String, Function<Supplier<Codec<Condition>>, Codec<? extends List<?>>>> getPropertyCodecBuilders() {
		return new HashMap<>(propertyCodecBuilders);
	}
}
