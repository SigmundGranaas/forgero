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
import java.util.Set;

/**
 * Handles stateless, type-level conversions between default Forgero Components and Minecraft Items.
 * This class does not handle NBT data or stateful instances.
 */
public class TypeConverter {
	private final IdMapper idMapper;
	private final ComponentRegistry componentRegistry;
	private final Item dynamicItem;
	private final Item dynamicToolItem;
	private final Item dynamicSwordItem;

	public TypeConverter(IdMapper idMapper, ComponentRegistry componentRegistry, Item dynamicItem, Item dynamicToolItem, Item dynamicSwordItem) {
		this.idMapper = idMapper;
		this.componentRegistry = componentRegistry;
		this.dynamicItem = dynamicItem;
		this.dynamicToolItem = dynamicToolItem;
		this.dynamicSwordItem = dynamicSwordItem;
	}

	/**
	 * Gets the default component for a given item type.
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
	 */
	public Optional<Component> toComponent(Identifier itemId) {
		Item item = Registries.ITEM.get(itemId);
		return toComponent(item);
	}

	/**
	 * Gets the item for a given component.
	 * This is the primary conversion method that supports dynamic components.
	 * It first checks for a data-driven mapping. If none exists, it falls back
	 * to selecting a dynamic container item based on the component's tags.
	 *
	 * @param component The component instance to convert.
	 * @return An Optional containing the mapped item or a dynamic item.
	 */
	public Optional<Item> toItem(Component component) {
		// Priority 1: Check for an explicit mapping in data files.
		return idMapper.toItem(component.id())
				// Priority 2: Fallback to a dynamic container item.
				// This uses the component object directly, which is crucial for
				// in-memory components that don't exist in the registry.
				.or(() -> Optional.of(getDynamicItemForComponent(component)));
	}

	/**
	 * Gets the item for a given component ID.
	 * This method is for scenarios where only the ID is available. It relies on the component
	 * being present in the registry to determine the appropriate dynamic item.
	 *
	 * @param componentId The ID of the component.
	 * @return An Optional containing the mapped item or a dynamic item.
	 */
	public Optional<Item> toItem(OpenIdentifier componentId) {
		// Priority 1: Check for an explicit mapping in data files.
		return idMapper.toItem(componentId)
				// Priority 2: Fallback by looking up the component in the registry.
				.or(() -> componentRegistry.get(componentId).map(this::getDynamicItemForComponent));
	}

	/**
	 * Selects the appropriate dynamic item (tool, sword, or generic) based on the component's tags.
	 */
	private Item getDynamicItemForComponent(Component component) {
		Set<OpenIdentifier> tags = component.getTags();
		if (tags.stream().anyMatch(tag -> tag.name().equals("sword"))) {
			return dynamicSwordItem;
		}
		if (tags.stream().anyMatch(tag -> tag.name().equals("tool"))) {
			return dynamicToolItem;
		}
		return dynamicItem;
	}
}
