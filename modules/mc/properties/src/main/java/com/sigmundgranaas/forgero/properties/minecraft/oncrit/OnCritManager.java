package com.sigmundgranaas.forgero.properties.minecraft.oncrit;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.runtime.ContextKeys;
import com.sigmundgranaas.forgero.common.runtime.DynamicContext;
import com.sigmundgranaas.forgero.common.runtime.MinecraftContextKeys;
import com.sigmundgranaas.forgero.common.runtime.PropertyDispatcher;
import com.sigmundgranaas.forgero.properties.minecraft.EntityEffects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;

/**
 * Handles critical-hit events from the attacker's equipment.
 */
public class OnCritManager {

	private OnCritManager() {
	}

	public static void handleCrit(PlayerEntity attacker, LivingEntity victim) {
		if (attacker.getWorld().isClient()) {
			return;
		}

		List<ItemStack> equipment = new ArrayList<>();
		attacker.getHandItems().forEach(equipment::add);
		attacker.getArmorItems().forEach(equipment::add);

		DynamicContext.Builder contextBuilder = new DynamicContext.Builder();
		Set<OpenIdentifier> victimTags = Registries.ENTITY_TYPE.getEntry(victim.getType())
				.streamTags()
				.map(TagKey::id)
				.map(id -> new OpenIdentifier(id.getNamespace(), id.getPath()))
				.collect(Collectors.toSet());
		contextBuilder.put(ContextKeys.TARGET_TAGS, victimTags);
		contextBuilder.put(MinecraftContextKeys.SOURCE_ENTITY, attacker);
		contextBuilder.put(MinecraftContextKeys.TARGET_ENTITY, victim);
		contextBuilder.put(MinecraftContextKeys.WORLD, attacker.getWorld());
		DynamicContext context = contextBuilder.build();

		for (ItemStack stack : equipment) {
			if (stack.isEmpty()) {
				continue;
			}
			List<OnCritProperty> properties = PropertyDispatcher.active(stack, OnCritProperty.KEY, context);
			for (OnCritProperty property : properties) {
				EntityEffects.apply(property.selector(), property.effects(), attacker, victim);
			}
		}
	}
}
