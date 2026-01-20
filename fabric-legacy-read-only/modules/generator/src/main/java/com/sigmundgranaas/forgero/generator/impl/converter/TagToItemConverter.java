package com.sigmundgranaas.forgero.generator.impl.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.registry.RankableConverter;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class TagToItemConverter implements RankableConverter<JsonElement, Collection<?>> {
	@Override
	public Collection<Item> convert(JsonElement entry) {
		var jsonObject = entry.getAsJsonObject();
		JsonElement tagElement = jsonObject.get("tag");

		List<Item> items;
		if (tagElement.isJsonArray()) {
			items = StreamSupport.stream(tagElement.getAsJsonArray().spliterator(), false)
					.flatMap(tag -> fromTag(tag.getAsString()).stream())
					.distinct()
					.collect(Collectors.toList());
		} else {
			String type = tagElement.getAsString();
			items = new ArrayList<>(fromTag(type));
		}
		if (jsonObject.has("filter")) {
			Set<String> exclusions = parseFilter(jsonObject.getAsJsonArray("filter"));
			items.removeIf(item -> exclusions.contains(Registries.ITEM.getId(item).toString()));
		}

		return items;
	}

	private Collection<Item> fromTag(String tag) {
		var tagKey = TagKey.of(Registries.ITEM.getKey(), new Identifier(tag));

		return StreamSupport.stream(Registries.ITEM.iterateEntries(tagKey).spliterator(), false)
				.map(RegistryEntry::value)
				.collect(Collectors.toList());
	}

	private Set<String> parseFilter(JsonArray filterArray) {
		return StreamSupport.stream(filterArray.spliterator(), false)
				.map(JsonElement::getAsString)
				.collect(Collectors.toSet());
	}

	@Override
	public boolean matches(JsonElement entry) {
		if (entry.isJsonObject()) {
			var jsonObject = entry.getAsJsonObject();
			return jsonObject.has("tag");
		}
		return false;
	}

}
