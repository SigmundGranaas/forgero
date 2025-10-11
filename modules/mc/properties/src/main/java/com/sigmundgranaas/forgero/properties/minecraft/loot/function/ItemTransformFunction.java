package com.sigmundgranaas.forgero.properties.minecraft.loot.function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.properties.minecraft.loot.filter.ItemFilter;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * A function that transforms a filtered item into a completely different item.
 */
public record ItemTransformFunction(ItemFilter input, Identifier output, int count) implements ItemFunction {
	public static final String TYPE = "forgero:item_transform";
	public static final Codec<ItemTransformFunction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ItemFilter.CODEC.fieldOf("input").forGetter(ItemTransformFunction::input),
			Identifier.CODEC.fieldOf("output").forGetter(ItemTransformFunction::output),
			Codec.INT.optionalFieldOf("count", 1).forGetter(ItemTransformFunction::count)
	).apply(instance, ItemTransformFunction::new));

	@Override
	@NotNull
	public ItemStack apply(ItemStack stack, LootContext context) {
		if (!input.test(stack)) {
			return stack;
		}

		Optional<Item> outputItem = Registries.ITEM.getOrEmpty(output);
		if (outputItem.isPresent()) {
			ItemStack newStack = new ItemStack(outputItem.get(), count);
			newStack.setNbt(stack.getNbt()); // Preserve NBT
			return newStack;
		}

		return stack; // Return original if output item not found
	}

	@Override
	public String type() {
		return TYPE;
	}
}
