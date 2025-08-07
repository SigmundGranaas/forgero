package com.sigmundgranaas.forgero.common.convert;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Optional;

/**
 * Handles stateless, type-level conversions between default Forgero Components and Minecraft Items.
 * This class does not handle NBT data or stateful instances.
 */
public class TypeConverter {
	private final IdMapper idMapper;
	private final ComponentRegistry componentRegistry;

	public TypeConverter(IdMapper idMapper, ComponentRegistry componentRegistry) {
		this.idMapper = idMapper;
		this.componentRegistry = componentRegistry;
	}

	/**
	 * Gets the default component for a given item type.
	 *
	 * @param item The item type.
	 * @return The default component, if a mapping exists.
	 */
	public Optional<Component> toComponent(Item item) {
		if (item == null || item == Items.AIR) {
			return Optional.empty();
		}
		// Creating a dummy stack is the most reliable way to check tags.
		return idMapper.toComponentId(new ItemStack(item))
				.flatMap(componentRegistry::get);
	}

	/**
	 * Gets the default component for a given item ID.
	 *
	 * @param itemId The Identifier of the item type.
	 * @return The default component, if a mapping exists.
	 */
	public Optional<Component> toComponent(Identifier itemId) {
		Item item = Registries.ITEM.get(itemId);
		return toComponent(item);
	}

	/**
	 * Gets the default item for a given component.
	 *
	 * @param component The component.
	 * @return The default item, if a mapping exists.
	 */
	public Optional<Item> toItem(Component component) {
		return idMapper.toItem(component.id());
	}

	/**
	 * Gets the default item for a given component ID.
	 *
	 * @param componentId The ID of the component.
	 * @return The default item, if a mapping exists.
	 */
	public Optional<Item> toItem(OpenIdentifier componentId) {
		return idMapper.toItem(componentId);
	}
}
