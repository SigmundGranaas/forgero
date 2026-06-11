package com.sigmundgranaas.forgero.properties.minecraft.onkill;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.runtime.ContextKeys;
import com.sigmundgranaas.forgero.common.runtime.DynamicContext;
import com.sigmundgranaas.forgero.common.runtime.RuntimeConditions;
import com.sigmundgranaas.forgero.effects.entity.ContextualEffectHandler;
import com.sigmundgranaas.forgero.effects.entity.EntityEffectHandler;
import com.sigmundgranaas.forgero.effects.entity.OnHitEffect;
import com.sigmundgranaas.forgero.common.api.ForgeroApi;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Manager for handling On-Kill events.
 * This is triggered when an entity is killed by another entity.
 */
public class OnKillManager {

	private OnKillManager() {
		// Static class
	}

	/**
	 * Handles the kill event by applying effects from the killer's equipped items.
	 * Checks both armor and held items for on-kill properties.
	 *
	 * @param killer The entity that performed the kill (becomes the "source" for effects)
	 * @param victim The entity that was killed (becomes the "target" for effects)
	 */
	public static void handleKill(LivingEntity killer, LivingEntity victim) {
		if (killer.getWorld().isClient()) {
			return; // Server-side only
		}

		// Collect all equipment (armor + held items)
		List<ItemStack> equipment = new ArrayList<>();
		killer.getHandItems().forEach(equipment::add);
		killer.getArmorItems().forEach(equipment::add);

		// Build context with victim tags
		DynamicContext.Builder contextBuilder = new DynamicContext.Builder();
		Set<OpenIdentifier> victimTags = Registries.ENTITY_TYPE.getEntry(victim.getType())
				.streamTags()
				.map(TagKey::id)
				.map(id -> new OpenIdentifier(id.getNamespace(), id.getPath()))
				.collect(Collectors.toSet());
		contextBuilder.put(ContextKeys.TARGET_TAGS, victimTags);

		// Process each equipped item
		for (ItemStack stack : equipment) {
			if (stack.isEmpty()) {
				continue;
			}

			List<OnKillProperty> properties = RuntimeConditions.filter(ForgeroApi.itemProperty().resolve(stack, OnKillProperty.Engine::new), contextBuilder.build());

			for (OnKillProperty property : properties) {
				// Killer is "source", victim is "initial target"
				List<Entity> finalTargets = property.selector().select(killer, victim);

				for (Entity finalTarget : finalTargets) {
					for (OnHitEffect effect : property.effects()) {
						if (effect instanceof ContextualEffectHandler contextual) {
							contextual.apply(killer, finalTarget);
						} else if (effect instanceof EntityEffectHandler simple) {
							simple.apply(finalTarget);
						}
					}
				}
			}
		}
	}
}
