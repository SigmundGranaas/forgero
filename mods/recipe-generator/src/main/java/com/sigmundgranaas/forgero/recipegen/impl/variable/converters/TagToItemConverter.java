package com.sigmundgranaas.forgero.recipegen.impl.variable.converters;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.recipegen.api.variable.VariableConverter;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Converts a JSON object with a "tag" field to a collection of Minecraft items.
 *
 * <h2>Example Input</h2>
 * <pre>{@code
 * {
 *   "tag": "minecraft:planks"
 * }
 * }</pre>
 *
 * <p>Or with multiple tags:</p>
 * <pre>{@code
 * {
 *   "tag": ["minecraft:planks", "minecraft:logs"]
 * }
 * }</pre>
 *
 * <p>With filter to exclude certain items:</p>
 * <pre>{@code
 * {
 *   "tag": "minecraft:planks",
 *   "filter": ["minecraft:oak_planks"]
 * }
 * }</pre>
 */
public class TagToItemConverter implements VariableConverter<Item> {

	@Override
	public boolean matches(JsonElement element) {
		if (element.isJsonObject()) {
			var jsonObject = element.getAsJsonObject();
			return jsonObject.has("tag");
		}
		return false;
	}

	@Override
	public Collection<Item> convert(JsonElement element) {
		var jsonObject = element.getAsJsonObject();
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
	public int priority() {
		return 10; // Higher priority than StringListConverter
	}
}
