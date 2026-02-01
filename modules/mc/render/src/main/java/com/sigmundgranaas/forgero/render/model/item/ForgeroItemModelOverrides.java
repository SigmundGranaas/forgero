package com.sigmundgranaas.forgero.render.model.item;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.model.api.ModelResolutionContext;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ForgeroItemModelOverrides extends ModelOverrideList {
	private final BiFunction<Component, Map<String, Object>, BakedModel> contextualBaker;
	private final Function<ItemStack, Optional<Component>> itemToComponent;
	private final Map<CacheKey, BakedModel> modelCache = new ConcurrentHashMap<>();

	public ForgeroItemModelOverrides(
			BiFunction<Component, Map<String, Object>, BakedModel> contextualBaker,
			Function<ItemStack, Optional<Component>> itemToComponent
	) {
		super(null, null, Collections.emptyList());
		this.contextualBaker = contextualBaker;
		this.itemToComponent = itemToComponent;
	}

	@Nullable
	@Override
	public BakedModel apply(BakedModel model, ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity, int seed) {
		Optional<Component> componentOpt = itemToComponent.apply(stack);

		if (componentOpt.isEmpty()) {
			return model;
		}
		Component component = componentOpt.get();

		Map<String, Object> dynamicState = new HashMap<>();
		int pullStateIndex = 0;
		int arrowHash = 0;

		if (entity != null && stack.getItem() instanceof BowItem) {
			boolean isPulling = entity.isUsingItem() && entity.getActiveItem() == stack;
			dynamicState.put("pulling", isPulling);

			if (isPulling) {
				float pullProgress = BowItem.getPullProgress(entity.getItemUseTime());
				dynamicState.put("pull", pullProgress);
				pullStateIndex = getPullStateIndex(pullProgress);

				// Find and pass the equipped arrow component for rendering
				if (entity instanceof PlayerEntity player) {
					ItemStack arrowStack = findEquippedArrow(player);
					if (!arrowStack.isEmpty()) {
						Optional<Component> arrowComponent = itemToComponent.apply(arrowStack);
						if (arrowComponent.isPresent()) {
							dynamicState.put("equippedArrow", arrowComponent.get());
							arrowHash = arrowComponent.get().hashCode();
						}
					}
				}
			}
		}

		CacheKey cacheKey = new CacheKey(component.hashCode(), pullStateIndex, arrowHash);

		return modelCache.computeIfAbsent(cacheKey, k -> contextualBaker.apply(component, dynamicState));
	}

	/**
	 * Finds the arrow the player would use when shooting.
	 * Checks offhand first, then main inventory (vanilla behavior).
	 */
	private ItemStack findEquippedArrow(PlayerEntity player) {
		// Check offhand first (vanilla behavior)
		ItemStack offhandStack = player.getOffHandStack();
		if (!offhandStack.isEmpty() && offhandStack.getItem() instanceof ArrowItem) {
			return offhandStack;
		}

		// Search main inventory for arrows
		for (int i = 0; i < player.getInventory().size(); i++) {
			ItemStack inventoryStack = player.getInventory().getStack(i);
			if (!inventoryStack.isEmpty() && inventoryStack.getItem() instanceof ArrowItem) {
				return inventoryStack;
			}
		}

		return ItemStack.EMPTY;
	}

	private int getPullStateIndex(float pullProgress) {
		if (pullProgress >= 0.9f) return 3;
		if (pullProgress >= 0.65f) return 2;
		if (pullProgress > 0f) return 1;
		return 0;
	}

	private record CacheKey(int componentHash, int pullState, int arrowHash) {
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			CacheKey cacheKey = (CacheKey) o;
			return componentHash == cacheKey.componentHash
					&& pullState == cacheKey.pullState
					&& arrowHash == cacheKey.arrowHash;
		}

		@Override
		public int hashCode() {
			return Objects.hash(componentHash, pullState, arrowHash);
		}
	}
}
