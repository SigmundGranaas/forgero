package com.sigmundgranaas.forgero.loader.impl;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.cof.ComponentConstructor;
import com.sigmundgranaas.forgero.cof.codec.CofCodecs;
import com.sigmundgranaas.forgero.cof.codec.ComponentCofCodec;
import com.sigmundgranaas.forgero.cof.dto.CofComponent;
import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.convert.ComponentConverterImpl;
import com.sigmundgranaas.forgero.common.convert.IdMapper;
import com.sigmundgranaas.forgero.common.convert.StatefulConverter;
import com.sigmundgranaas.forgero.common.convert.TypeConverter;
import com.sigmundgranaas.forgero.common.nbt.ComponentNbtConverter;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import com.sigmundgranaas.forgero.core.property.api.codec.KeyMapDispatchCodec;
import com.sigmundgranaas.forgero.core.property.engine.ResolverEngine;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import com.sigmundgranaas.forgero.core.registry.impl.MapBackedComponentRegistry;
import com.sigmundgranaas.forgero.data.pipeline.api.ForgeroDataBundle;
import com.sigmundgranaas.forgero.data.pipeline.api.ForgeroDataInitializer;
import net.minecraft.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Handles the initialization of core services and registries.
 * Responsible for phase 6 of the initialization pipeline.
 */
public class ComponentRegistrationService {
	private static final Logger LOGGER = LoggerFactory.getLogger(ComponentRegistrationService.class);

	/**
	 * Record holding the dynamic item references needed for type conversion.
	 */
	public record DynamicItems(Item item, Item tool, Item sword) {}

	/**
	 * Record holding all initialized services.
	 */
	public record ServiceBundle(
			ComponentRegistry componentRegistry,
			ComponentConverter converter,
			ComponentNbtConverter nbtConverter,
			Resolver resolver,
			IdMapper idMapper
	) {}

	/**
	 * Phase 6: Initialize all core services.
	 *
	 * @param bundle          The loaded data bundle
	 * @param dataConfig      The data configuration with property codecs
	 * @param dynamicItems    The dynamic item references
	 * @return A bundle containing all initialized services
	 */
	public ServiceBundle initializeServices(
			ForgeroDataBundle bundle,
			ForgeroDataInitializer.Config dataConfig,
			DynamicItems dynamicItems
	) {
		LOGGER.debug("Initializing core services...");

		// Create component registry from bundle
		ComponentRegistry componentRegistry = createComponentRegistry(bundle);

		// Create component codec for NBT serialization
		ComponentConstructor constructorRegistry = ComponentConstructor.defaults();
		Codec<Component> componentCodec = createComponentCodec(
				componentRegistry,
				constructorRegistry,
				dataConfig
		);

		// Create NBT converter
		ComponentNbtConverter nbtConverter = new ComponentNbtConverter(componentCodec);

		// Create converters
		IdMapper idMapper = new IdMapper(bundle.hostItemMap());
		TypeConverter typeConverter = new TypeConverter(
				idMapper,
				componentRegistry,
				dynamicItems.item(),
				dynamicItems.tool(),
				dynamicItems.sword()
		);
		StatefulConverter statefulConverter = new StatefulConverter(nbtConverter, typeConverter);
		ComponentConverter componentConverter = new ComponentConverterImpl(
				statefulConverter,
				typeConverter,
				idMapper,
				componentRegistry
		);

		// Create resolver
		Resolver resolver = new ResolverEngine();

		LOGGER.debug("Core services initialized");

		return new ServiceBundle(
				componentRegistry,
				componentConverter,
				nbtConverter,
				resolver,
				idMapper
		);
	}

	private ComponentRegistry createComponentRegistry(ForgeroDataBundle bundle) {
		return new MapBackedComponentRegistry(
				bundle.componentRegistry().all().stream()
						.collect(Collectors.toMap(Component::id, Function.identity()))
		);
	}

	private Codec<Component> createComponentCodec(
			ComponentRegistry componentRegistry,
			ComponentConstructor constructorRegistry,
			ForgeroDataInitializer.Config dataConfig
	) {
		Codec<Map<String, List<?>>> propertyMapCodec =
				new KeyMapDispatchCodec(dataConfig.propertyCodecs()).codec();
		Codec<CofComponent> cofComponentCodec = CofCodecs.create(propertyMapCodec);

		return new ComponentCofCodec(
				componentRegistry,
				constructorRegistry,
				cofComponentCodec
		);
	}
}
