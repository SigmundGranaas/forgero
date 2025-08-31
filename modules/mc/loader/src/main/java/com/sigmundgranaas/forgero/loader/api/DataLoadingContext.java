package com.sigmundgranaas.forgero.loader.api;

import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.nbt.ComponentNbtConverter;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import com.sigmundgranaas.forgero.common.tags.engine.TaggedRegistry;
import com.sigmundgranaas.forgero.data.pipeline.api.ForgeroDataBundle;
import net.minecraft.item.ItemStack;

import java.util.Optional;

/**
 * Context object providing access to all loaded data and registries.
 * This is the primary interface for modules to access Forgero data.
 */
public interface DataLoadingContext {
	/**
	 * Get the component registry containing all loaded components.
	 */
	ComponentRegistry getComponentRegistry();

	/**
	 * Get the tagged registry for tag-based queries.
	 */
	TaggedRegistry<Component> getTaggedComponentRegistry();

	/**
	 * Get the property resolver for computing attributes and features.
	 */
	Resolver getResolver();

	/**
	 * Maps an ItemStack to its corresponding Component.
	 * This is the primary method for stateful conversion from an item to a component.
	 */
	Optional<Component> getComponent(ItemStack stack);

	/**
	 * Get the main converter for all Forgero/Minecraft conversions.
	 * This is the preferred way to access conversion logic.
	 */
	ComponentConverter getConverter();

	/**
	 * Get the NBT converter for serializing and deserializing components.
	 */
	ComponentNbtConverter getNbtConverter();

	/**
	 * Get the original data bundle for modules that need raw data access.
	 */
	ForgeroDataBundle getDataBundle();
}
