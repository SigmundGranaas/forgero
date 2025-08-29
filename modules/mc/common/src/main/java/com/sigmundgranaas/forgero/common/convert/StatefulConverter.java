package com.sigmundgranaas.forgero.common.convert;

import com.sigmundgranaas.forgero.common.item.ForgeroHostItem;
import com.sigmundgranaas.forgero.common.nbt.ComponentNbtConverter;
import com.sigmundgranaas.forgero.core.component.api.Component;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import java.util.Optional;

/**
 * Handles stateful conversions between Forgero Components and Minecraft ItemStacks.
 * This class is responsible for reading and writing component data to/from NBT.
 */
public class StatefulConverter {
	private final ComponentNbtConverter nbtConverter;
	private final TypeConverter typeConverter;

	public StatefulConverter(ComponentNbtConverter nbtConverter, TypeConverter typeConverter) {
		this.nbtConverter = nbtConverter;
		this.typeConverter = typeConverter;
	}

	/**
	 * Converts an ItemStack into its corresponding Forgero Component.
	 * This method follows a clear priority:
	 * 1. Deserialize from "ForgeroComponent" NBT tag if present.
	 * 2. If the item is a ForgeroHostItem, get its default component.
	 * 3. Look up a default component based on item/tag mappings.
	 *
	 * @param stack The item stack to convert.
	 * @return The stateful component representation of the ItemStack, or empty if no conversion is possible.
	 */
	public Optional<Component> toComponent(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return Optional.empty();
		}

		// Priority 1: Full component data in NBT
		if (stack.hasNbt()) {
			Optional<Component> componentFromNbt = nbtConverter.fromNbt(stack.getNbt());
			if (componentFromNbt.isPresent()) {
				return componentFromNbt;
			}
		}

		// Priority 2: The item is a ForgeroHostItem, providing a default component
		if (stack.getItem() instanceof ForgeroHostItem host) {
			return Optional.of(host.getForgeroComponent());
		}

		// Priority 3: Mapped vanilla/modded item
		return typeConverter.toComponent(stack.getItem());
	}

	/**
	 * Converts a Forgero Component into an ItemStack with the component's state written to NBT.
	 *
	 * @param component The component to convert.
	 * @return An optional containing the created ItemStack, or empty if no base item could be found.
	 */
	public Optional<ItemStack> toStack(Component component) {
		Optional<Item> baseItemOpt = typeConverter.toItem(component);

		if (baseItemOpt.isEmpty()) {
			return Optional.empty();
		}

		Item baseItem = baseItemOpt.get();
		ItemStack stack = new ItemStack(baseItem);

		// Serialize the component to NBT and attach it
		NbtCompound componentNbt = nbtConverter.toNbt(component);
		stack.setNbt(componentNbt);

		return Optional.of(stack);
	}
}
