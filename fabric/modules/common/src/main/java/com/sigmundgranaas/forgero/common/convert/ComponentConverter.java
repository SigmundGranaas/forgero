package com.sigmundgranaas.forgero.common.convert;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.Optional;

/**
 * A comprehensive, public-facing API for all conversions between Forgero concepts
 * (Components, OpenIdentifiers) and Minecraft concepts (ItemStacks, Items, Identifiers).
 * <p>
 * This facade provides a single point of entry for conversion logic, with convenient
 * overloaded methods for different use cases.
 */
public interface ComponentConverter {

	// ========== To Component (Stateful and Stateless) ==========

	/**
	 * Converts an ItemStack to its Component representation.
	 * This is a STATEFUL conversion that prioritizes NBT data.
	 * Falls back to default component mappings if no NBT is found.
	 *
	 * @see StatefulConverter#toComponent(ItemStack)
	 */
	Optional<Component> toComponent(ItemStack stack);

	/**
	 * Converts an Item to its default Component representation.
	 * This is a STATELESS conversion based on type mappings.
	 *
	 * @see TypeConverter#toComponent(Item)
	 */
	Optional<Component> toComponent(Item item);

	/**
	 * Converts a Minecraft item Identifier to its default Component representation.
	 * This is a STATELESS conversion based on type mappings.
	 *
	 * @see TypeConverter#toComponent(Identifier)
	 */
	Optional<Component> toComponent(Identifier itemId);

	// ========== To ItemStack / Item (Stateful and Stateless) ==========

	/**
	 * Converts a Component to an ItemStack, serializing the component's state into NBT.
	 * This is a STATEFUL conversion.
	 *
	 * @see StatefulConverter#toStack(Component)
	 */
	Optional<ItemStack> toStack(Component component);

	/**
	 * Converts a Component ID to an ItemStack of its default state.
	 * This is a STATEFUL conversion that creates a default component and then serializes it.
	 */
	Optional<ItemStack> toStack(OpenIdentifier componentId);

	/**
	 * Converts a Component to its default mapped Item.
	 * This is a STATELESS conversion.
	 *
	 * @see TypeConverter#toItem(Component)
	 */
	Optional<Item> toItem(Component component);

	/**
	 * Converts a Component ID to its default mapped Item.
	 * This is a STATELESS conversion.
	 *
	 * @see TypeConverter#toItem(OpenIdentifier)
	 */
	Optional<Item> toItem(OpenIdentifier componentId);

	// ========== To Identifier (ID-level mapping) ==========

	/**
	 * Converts a Component ID to its mapped Minecraft item Identifier.
	 * This is a STATELESS conversion.
	 *
	 * @see IdMapper#toItemId(OpenIdentifier)
	 */
	Optional<Identifier> toItemId(OpenIdentifier componentId);

	/**
	 * Converts an ItemStack to its mapped Component ID based on its type.
	 * This is a STATELESS conversion that does NOT read NBT.
	 *
	 * @see IdMapper#toComponentId(ItemStack)
	 */
	Optional<OpenIdentifier> toComponentId(ItemStack stack);

	/**
	 * Converts an Item to its mapped Component ID.
	 * This is a STATELESS conversion.
	 */
	Optional<OpenIdentifier> toComponentId(Item item);
}
