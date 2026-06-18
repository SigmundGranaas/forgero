package com.sigmundgranaas.forgero.properties.minecraft.onblock;

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
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;

/**
 * Handles shield-block (parry) events from the defender's equipment.
 */
public class OnBlockManager {

	private OnBlockManager() {
	}

	public static void handleBlock(LivingEntity defender, LivingEntity attacker) {
		if (defender.getWorld().isClient()) {
			return;
		}

		List<ItemStack> equipment = new ArrayList<>();
		defender.getHandItems().forEach(equipment::add);
		defender.getArmorItems().forEach(equipment::add);

		DynamicContext.Builder contextBuilder = new DynamicContext.Builder();
		Set<OpenIdentifier> attackerTags = Registries.ENTITY_TYPE.getEntry(attacker.getType())
				.streamTags()
				.map(TagKey::id)
				.map(id -> new OpenIdentifier(id.getNamespace(), id.getPath()))
				.collect(Collectors.toSet());
		contextBuilder.put(ContextKeys.TARGET_TAGS, attackerTags);
		contextBuilder.put(MinecraftContextKeys.SOURCE_ENTITY, defender);
		contextBuilder.put(MinecraftContextKeys.TARGET_ENTITY, attacker);
		contextBuilder.put(MinecraftContextKeys.WORLD, defender.getWorld());
		DynamicContext context = contextBuilder.build();

		for (ItemStack stack : equipment) {
			if (stack.isEmpty()) {
				continue;
			}
			List<OnBlockProperty> properties = PropertyDispatcher.active(stack, OnBlockProperty.KEY, context);
			for (OnBlockProperty property : properties) {
				EntityEffects.apply(property.selector(), property.effects(), defender, attacker);
			}
		}
	}
}
