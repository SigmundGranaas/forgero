package com.sigmundgranaas.forgero.properties.minecraft.ontick;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.runtime.ContextKeys;
import com.sigmundgranaas.forgero.common.runtime.DynamicContext;
import com.sigmundgranaas.forgero.common.runtime.PropertyDispatcher;
import com.sigmundgranaas.forgero.properties.minecraft.EntityEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OnTickManager {

	private OnTickManager() {
		// Static class
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

			List<OnTickProperty> properties = PropertyDispatcher.active(stack, OnTickProperty.KEY, context);

			for (OnTickProperty property : properties) {
				if (entity.age % property.interval() == 0) {
					EntityEffects.apply(property.selector(), property.effects(), entity, entity);
				}
			}
		}
	}
}
