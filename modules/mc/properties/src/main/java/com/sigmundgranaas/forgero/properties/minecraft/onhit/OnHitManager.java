package com.sigmundgranaas.forgero.properties.minecraft.onhit;

import com.sigmundgranaas.forgero.common.api.item.ItemPropertyApi;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.runtime.ContextKeys;
import com.sigmundgranaas.forgero.common.runtime.DynamicContext;
import com.sigmundgranaas.forgero.common.runtime.RuntimeConditions;
import com.sigmundgranaas.forgero.common.api.ForgeroApi;
import com.sigmundgranaas.forgero.effects.entity.ContextualEffectHandler;
import com.sigmundgranaas.forgero.effects.entity.EntityEffectHandler;
import com.sigmundgranaas.forgero.effects.entity.OnHitEffect;
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

		// Build context with target tags for conditions
		DynamicContext.Builder contextBuilder = new DynamicContext.Builder();
		Set<OpenIdentifier> targetTags = Registries.ENTITY_TYPE.getEntry(target.getType())
				.streamTags()
				.map(TagKey::id)
				.map(id -> new OpenIdentifier(id.getNamespace(), id.getPath()))
				.collect(Collectors.toSet());
		contextBuilder.put(ContextKeys.TARGET_TAGS, targetTags);

		List<OnHitProperty> properties = RuntimeConditions.filter(ForgeroApi.itemProperty().resolve(stack, OnHitProperty.Engine::new), contextBuilder.build());

		for (OnHitProperty property : properties) {
			// Selector handles both selection and filtering
			List<Entity> finalTargets = property.selector().select(source, target);

			for (Entity finalTarget : finalTargets) {
				for (OnHitEffect effect : property.effects()) {
					if (effect instanceof ContextualEffectHandler contextual) {
						contextual.apply(source, finalTarget);
					} else if (effect instanceof EntityEffectHandler simple) {
						simple.apply(finalTarget);
					}
				}
			}
		}
	}
}
