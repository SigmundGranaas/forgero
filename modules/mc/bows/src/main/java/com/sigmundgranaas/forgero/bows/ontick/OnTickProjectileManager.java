package com.sigmundgranaas.forgero.bows.ontick;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.runtime.ContextKeys;
import com.sigmundgranaas.forgero.common.runtime.DynamicContext;
import com.sigmundgranaas.forgero.common.runtime.RuntimeConditions;
import com.sigmundgranaas.forgero.effects.entity.ContextualEffectHandler;
import com.sigmundgranaas.forgero.effects.entity.EntityEffectHandler;
import com.sigmundgranaas.forgero.effects.entity.OnHitEffect;
import com.sigmundgranaas.forgero.common.api.ForgeroInitializedCallback;
import com.sigmundgranaas.forgero.properties.minecraft.ontick.OnTickProperty;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;

/**
 * Handles on-tick property effects for projectile entities.
 *
 * <p>This manager is responsible for executing on-tick effects attached to
 * arrow/projectile components while they are in flight. Unlike the LivingEntity
 * on-tick handler, this manager is called directly from the projectile's tick method.
 *
 * <p>Example use case: An arrow with a trailing particle effect, or an arrow
 * that applies a status effect to nearby entities while flying.
 */
public class OnTickProjectileManager {
	private static ComponentConverter converter;

	static {
		// Initialize services when Forgero is ready
		ForgeroInitializedCallback.EVENT.register(services -> {
			converter = services.converter();
		});
	}

	private OnTickProjectileManager() {
		// Static utility class
	}

	/**
	 * Handles on-tick effects for a projectile entity.
	 *
	 * @param projectile The projectile entity (e.g., DynamicArrowEntity)
	 * @param stack      The ItemStack representing the projectile
	 * @param age        The age of the projectile in ticks (used for interval checking)
	 */
	public static void handleTick(ProjectileEntity projectile, ItemStack stack, int age) {
		if (stack.isEmpty()) {
			return;
		}

		// Only run on server side
		if (projectile.getWorld().isClient()) {
			return;
		}

		// Ensure services are available
		ComponentConverter activeConverter = converter;

		if (activeConverter == null) {
			var services = ForgeroInitializedCallback.getServices().orElse(null);
			if (services != null) {
				activeConverter = services.converter();
			}
		}

		if (activeConverter == null) {
			return;
		}

		final ComponentConverter finalConverter = activeConverter;

		finalConverter.toComponent(stack).ifPresent(component -> {
			// Build context with projectile's entity type tags
			DynamicContext.Builder contextBuilder = new DynamicContext.Builder();
			Set<OpenIdentifier> sourceTags = Registries.ENTITY_TYPE.getEntry(projectile.getType())
					.streamTags()
					.map(TagKey::id)
					.map(id -> new OpenIdentifier(id.getNamespace(), id.getPath()))
					.collect(Collectors.toSet());
			contextBuilder.put(ContextKeys.TARGET_TAGS, sourceTags);
			DynamicContext context = contextBuilder.build();

			// Resolve on-tick properties
			var engine = new OnTickProperty.Engine();
			List<OnTickProperty> properties = RuntimeConditions.filter(engine.resolve(component), context);

			// Determine the source entity (owner if available, otherwise projectile itself)
			Entity source = projectile.getOwner() != null ? projectile.getOwner() : projectile;

			for (OnTickProperty property : properties) {
				// Check interval (e.g., every 20 ticks)
				if (age % property.interval() == 0) {
					// Selector handles both selection and filtering
					// For projectiles, the "target" for selection is typically the projectile itself
					List<Entity> finalTargets = property.selector().select(source, projectile);

					for (Entity target : finalTargets) {
						for (OnHitEffect effect : property.effects()) {
							if (effect instanceof ContextualEffectHandler contextual) {
								contextual.apply(source, target);
							} else if (effect instanceof EntityEffectHandler simple) {
								simple.apply(target);
							}
						}
					}
				}
			}
		});
	}
}
