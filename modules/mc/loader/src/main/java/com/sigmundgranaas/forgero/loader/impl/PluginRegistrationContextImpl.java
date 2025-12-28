package com.sigmundgranaas.forgero.loader.impl;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.core.property.api.PropertyKey;
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
	private final Map<String, Codec<? extends StaticCondition>> staticConditionCodecs = new HashMap<>();
	private final Map<String, Codec<? extends DynamicCondition>> dynamicConditionCodecs = new HashMap<>();
	private final Map<PropertyKey<?>, Function<Supplier<Codec<Condition>>, Codec<? extends List<?>>>> propertyCodecBuilders = new HashMap<>();
	private final Map<String, Codec<? extends com.sigmundgranaas.forgero.core.component.api.Slot>> slotCodecs = new HashMap<>();
	private final Supplier<TagResolver> tagResolverSupplier;

	public PluginRegistrationContextImpl(Supplier<TagResolver> tagResolverSupplier) {
		this.tagResolverSupplier = tagResolverSupplier;
	}

	@Override
	public void registerItemCreator(String itemClass, ItemCreator creator) {
		if (itemCreators.containsKey(itemClass)) {
			LOGGER.warn("Item creator for class '{}' is being overwritten", itemClass);
		}
		itemCreators.put(itemClass, creator);
		LOGGER.trace("Registered item creator for class '{}'", itemClass);
	}

	@Override
	public void registerStaticConditionCodec(String type, Function<Supplier<TagResolver>, Codec<? extends StaticCondition>> factory) {
		if (staticConditionCodecs.containsKey(type)) {
			LOGGER.warn("Static condition codec for type '{}' is being overwritten", type);
		}
		staticConditionCodecs.put(type, factory.apply(tagResolverSupplier));
		LOGGER.trace("Registered static condition codec for type '{}'", type);
	}

	@Override
	public void registerStaticConditionCodec(String type, Codec<? extends StaticCondition> codec) {
		if (staticConditionCodecs.containsKey(type)) {
			LOGGER.warn("Static condition codec for type '{}' is being overwritten", type);
		}
		staticConditionCodecs.put(type, codec);
		LOGGER.trace("Registered static condition codec for type '{}'", type);
	}

	@Override
	public void registerDynamicConditionCodec(String type, Codec<? extends DynamicCondition> codec) {
		if (dynamicConditionCodecs.containsKey(type)) {
			LOGGER.warn("Dynamic condition codec for type '{}' is being overwritten", type);
		}
		dynamicConditionCodecs.put(type, codec);
		LOGGER.trace("Registered dynamic condition codec for type '{}'", type);
	}


	@Override
	public void registerPropertyCodec(PropertyKey<?> key, Function<Supplier<Codec<Condition>>, Codec<? extends List<?>>> codecBuilder) {
		if (propertyCodecBuilders.containsKey(key)) {
			LOGGER.warn("Property codec builder for key '{}' is being overwritten by a new plugin.", key);
		}
		propertyCodecBuilders.put(key, codecBuilder);
		LOGGER.trace("Registered property codec builder for key '{}'", key);
	}

	@Override
	public void registerSlotCodec(String type, Codec<? extends com.sigmundgranaas.forgero.core.component.api.Slot> codec) {
		if (slotCodecs.containsKey(type)) {
			LOGGER.warn("Slot codec for type '{}' is being overwritten", type);
		}
		slotCodecs.put(type, codec);
		LOGGER.trace("Registered slot codec for type '{}'", type);
	}


	public Map<String, ItemCreator> getItemCreators() {
		return new HashMap<>(itemCreators);
	}

	public Map<String, Codec<? extends StaticCondition>> getStaticConditionCodecs() {
		return new HashMap<>(staticConditionCodecs);
	}

	public Map<String, Codec<? extends DynamicCondition>> getDynamicConditionCodecs() {
		return new HashMap<>(dynamicConditionCodecs);
	}

	public Map<PropertyKey<?>, Function<Supplier<Codec<Condition>>, Codec<? extends List<?>>>> getPropertyCodecBuilders() {
		return new HashMap<>(propertyCodecBuilders);
	}

	public Map<String, Codec<? extends com.sigmundgranaas.forgero.core.component.api.Slot>> getSlotCodecs() {
		return new HashMap<>(slotCodecs);
	}
}
