package com.sigmundgranaas.forgero.properties.minecraft.ontick;

import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.context.ContextKeys;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;
import com.sigmundgranaas.forgero.loader.api.ForgeroServices;
import com.sigmundgranaas.forgero.effects.entity.ContextualEffectHandler;
import com.sigmundgranaas.forgero.effects.entity.EntityEffectHandler;
import com.sigmundgranaas.forgero.effects.entity.OnHitEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OnTickManager {

	private static ComponentConverter converter;

	private OnTickManager() {
		// Static class
	}

	/**
	 * Initializes the manager with required services.
	 * Called during Forgero initialization.
	 *
	 * @param services The Forgero services container
	 */
	public static void initialize(ForgeroServices services) {
		converter = services.converter();
	}

	public static void handle(LivingEntity entity) {
		if (entity.getWorld().isClient()) {
			return;
		}

		// Setup context for condition evaluation
		DynamicContext.Builder contextBuilder = new DynamicContext.Builder();
		Set<OpenIdentifier> sourceTags = Registries.ENTITY_TYPE.getEntry(entity.getType())
				.streamTags()
				.map(TagKey::id)
				.map(id -> new OpenIdentifier(id.getNamespace(), id.getPath()))
				.collect(Collectors.toSet());
		// Conditions like `target_has_tag` will check against the entity with the ticking item
		contextBuilder.put(ContextKeys.TARGET_TAGS, sourceTags);
		DynamicContext context = contextBuilder.build();

		List<ItemStack> equipment = new ArrayList<>();
		entity.getHandItems().forEach(equipment::add);
		entity.getArmorItems().forEach(equipment::add);

		for (ItemStack stack : equipment) {
			if (stack.isEmpty()) {
				continue;
			}
			converter.toComponent(stack).ifPresent(component -> {
				var engine = new OnTickProperty.Engine();
				List<OnTickProperty> properties = engine.resolve(component, context);

				for (OnTickProperty property : properties) {
					if (entity.age % property.interval() == 0) {
						// Selector handles both selection and filtering
						List<Entity> finalTargets = property.selector().select(entity, entity);

						for (Entity target : finalTargets) {
							for (OnHitEffect effect : property.effects()) {
								if (effect instanceof ContextualEffectHandler contextual) {
									contextual.apply(entity, target);
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
}
