package com.sigmundgranaas.forgero.armor.client.model;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.model.registry.api.item.ItemModelRegistry;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelResolver;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A general-purpose model resolver for Forgero items.
 * This class is responsible for intercepting model load requests for specific items
 * and providing a custom UnbakedModel. It is configured with a map of baseline components
 * and all necessary dependencies during initialization.
 */
public class ForgeroModelProvider implements ModelResolver {
	private final Map<Identifier, UnbakedModel> unbakedModelMap;

	/**
	 * Constructs a new model provider.
	 *
	 * @param componentMap              A map from an item's Identifier to its baseline Forgero Component.
	 * @param itemToComponentConverter  A function to resolve a Component from an ItemStack.
	 * @param modelRegistry             The model registry needed for the resolver pipeline.
	 */
	public ForgeroModelProvider(Map<Identifier, Component> componentMap, Function<ItemStack, Optional<Component>> itemToComponentConverter, ItemModelRegistry modelRegistry) {
		this.unbakedModelMap = componentMap.entrySet().stream()
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						entry -> new UnbakedForgeroModel(entry.getValue(), itemToComponentConverter, modelRegistry)
				));
	}

	@Override
	public @Nullable UnbakedModel resolveModel(Context context) {
		Identifier resourceId = context.id();

		if (resourceId.getPath().startsWith("item/")) {
			String itemPath = resourceId.getPath().substring("item/".length());
			Identifier itemId = new Identifier(resourceId.getNamespace(), itemPath);
			return unbakedModelMap.get(itemId);
		}

		return null;
	}
}
