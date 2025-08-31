package com.sigmundgranaas.forgero.predicate.minecraft.item;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.predicate.minecraft.util.NumericPredicate;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;

/**
 * Predicate for checking an ItemStack.
 * Checks for item type(s), a tag, and count.
 *
 * <p><h3>Examples:</h3>
 * {@code { "item": "minecraft:diamond" } }
 * <br>
 * {@code { "tag": "minecraft:swords", "count": { "min": 1 } } }
 */
public record ItemPredicate(
		Optional<List<Item>> item,
		Optional<TagKey<Item>> tag,
		Optional<NumericPredicate> count
) {
	private static final Codec<List<Item>> ITEM_LIST_CODEC = Codec.either(Identifier.CODEC, Codec.list(Identifier.CODEC))
			.xmap(
					either -> either.map(List::of, list -> list).stream().map(Registries.ITEM::get).toList(),
					items -> items.size() == 1 ? Either.left(Registries.ITEM.getId(items.get(0))) : Either.right(items.stream().map(Registries.ITEM::getId).toList())
			);

	private static final Codec<TagKey<Item>> TAG_CODEC = Identifier.CODEC.xmap(
			id -> TagKey.of(Registries.ITEM.getKey(), id),
			TagKey::id
	);

	public static final Codec<ItemPredicate> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					ITEM_LIST_CODEC.optionalFieldOf("item").forGetter(ItemPredicate::item),
					TAG_CODEC.optionalFieldOf("tag").forGetter(ItemPredicate::tag),
					NumericPredicate.CODEC.optionalFieldOf("count").forGetter(ItemPredicate::count)
			).apply(instance, ItemPredicate::new)
	);

	public boolean test(ItemStack stack) {
		if (stack.isEmpty()) {
			// An empty stack should not match a predicate with any criteria.
			return item.isEmpty() && tag.isEmpty() && count.isEmpty();
		}
		boolean itemMatch = item.map(items -> items.contains(stack.getItem())).orElse(true);
		boolean tagMatch = tag.map(stack::isIn).orElse(true);
		boolean countMatch = count.map(p -> p.test(stack.getCount())).orElse(true);

		return itemMatch && tagMatch && countMatch;
	}
}
