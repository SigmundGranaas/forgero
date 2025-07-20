package com.sigmundgranaas.forgero.armor.client.model;

import com.sigmundgranaas.forgero.core.component.api.Component;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A highly decoupled, caching model override list.
 * This class is responsible for taking an ItemStack, resolving it to a Component,
 * and using a provided "baking" function to generate and cache the final BakedModel.
 * It has no knowledge of how models are actually baked.
 */
public class ForgeroItemModelOverrides extends ModelOverrideList {
	private final Function<Component, BakedModel> componentBaker;
	private final Function<ItemStack, Optional<Component>> itemToComponent;
	private final Map<String, BakedModel> modelCache = new ConcurrentHashMap<>();

	public ForgeroItemModelOverrides(Function<Component, BakedModel> componentBaker, Function<ItemStack, Optional<Component>> itemToComponent) {
		super(null, null, Collections.emptyList());
		this.componentBaker = componentBaker;
		this.itemToComponent = itemToComponent;
	}

	@Nullable
	@Override
	public BakedModel apply(BakedModel model, ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity, int seed) {
		// Use the provided function to resolve the component from the item stack.
		Optional<Component> componentOpt = itemToComponent.apply(stack);

		if (componentOpt.isEmpty()) {
			return model; // Return the default model if no component is found
		}
		Component component = componentOpt.get();

		// Use the component's state to generate a unique cache key.
		String cacheKey = generateCacheKey(component);

		// Compute and cache the baked model if it doesn't exist for this specific state.
		// The baking logic is now encapsulated entirely within the injected 'componentBaker' function.
		return modelCache.computeIfAbsent(cacheKey, key -> componentBaker.apply(component));
	}

	private String generateCacheKey(Component component) {
		// The cache key includes the root component and all its children,
		// ensuring that any modification results in a new model bake.
		return component.id().toString() + component.getChildren().stream().map(child -> child.id().toString()).collect(Collectors.joining());
	}
}
