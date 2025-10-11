package com.sigmundgranaas.forgero.properties.minecraft.loot.filter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Optional;

/**
 * A filter that checks if an ItemStack's item matches a specific Identifier.
 */
public record IsItemFilter(Identifier item) implements ItemFilter {
	public static final String TYPE = "forgero:is_item";
	public static final Codec<IsItemFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.fieldOf("item").forGetter(IsItemFilter::item)
	).apply(instance, IsItemFilter::new));

	@Override
	public boolean test(ItemStack stack) {
		Optional<Item> filterItem = Registries.ITEM.getOrEmpty(item);
		return filterItem.map(stack::isOf).orElse(false);
	}

	@Override
	public String type() {
		return TYPE;
	}
}
