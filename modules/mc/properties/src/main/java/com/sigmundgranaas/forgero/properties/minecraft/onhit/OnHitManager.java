package com.sigmundgranaas.forgero.properties.minecraft.onhit;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.runtime.ContextKeys;
import com.sigmundgranaas.forgero.common.runtime.DynamicContext;
import com.sigmundgranaas.forgero.common.runtime.MinecraftContextKeys;
import com.sigmundgranaas.forgero.common.runtime.PropertyDispatcher;
import com.sigmundgranaas.forgero.properties.minecraft.EntityEffects;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OnHitManager {

	private OnHitManager() {
		// Static class
	}

	public static void handleOnHit(ItemStack stack, Entity source, Entity target) {
		if (stack.isEmpty()) {
			return;
		}

		// Build context with target tags AND the entities/world so dynamic conditions
		// (e.g. minecraft:entity, forgero:backstab, forgero:target_has_mark) can evaluate.
		DynamicContext.Builder contextBuilder = new DynamicContext.Builder();
		Set<OpenIdentifier> targetTags = Registries.ENTITY_TYPE.getEntry(target.getType())
				.streamTags()
				.map(TagKey::id)
				.map(id -> new OpenIdentifier(id.getNamespace(), id.getPath()))
				.collect(Collectors.toSet());
		contextBuilder.put(ContextKeys.TARGET_TAGS, targetTags);
		contextBuilder.put(MinecraftContextKeys.SOURCE_ENTITY, source);
		contextBuilder.put(MinecraftContextKeys.TARGET_ENTITY, target);
		if (source.getWorld() != null) {
			contextBuilder.put(MinecraftContextKeys.WORLD, source.getWorld());
		}

		List<OnHitProperty> properties = PropertyDispatcher.active(stack, OnHitProperty.KEY, contextBuilder.build());

		for (OnHitProperty property : properties) {
			EntityEffects.apply(property.selector(), property.effects(), source, target);
		}
	}
}
