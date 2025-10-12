package com.sigmundgranaas.forgero.properties.minecraft.loot.filter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

/**
 * A filter that checks if an ItemStack belongs to a specific item tag.
 */
public record TagFilter(Identifier tag) implements ItemFilter {
	public static final String TYPE = "forgero:tag";
	public static final Codec<TagFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.fieldOf("tag").forGetter(TagFilter::tag)
	).apply(instance, TagFilter::new));

	@Override
	public boolean test(ItemStack stack) {
		TagKey<Item> tagKey = TagKey.of(RegistryKeys.ITEM, tag);
		return stack.isIn(tagKey);
	}

	@Override
	public String type() {
		return TYPE;
	}
}
