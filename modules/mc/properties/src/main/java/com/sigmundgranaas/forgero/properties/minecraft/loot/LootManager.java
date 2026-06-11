package com.sigmundgranaas.forgero.properties.minecraft.loot;

import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.runtime.ContextKeys;
import com.sigmundgranaas.forgero.common.runtime.DynamicContext;
import com.sigmundgranaas.forgero.common.runtime.PropertyDispatcher;
import com.sigmundgranaas.forgero.common.api.ForgeroServices;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class LootManager {

	private static ComponentConverter converter;

	private LootManager() {
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

	public static List<ItemStack> handleBlockLoot(List<ItemStack> loot, LootContext context) {
		ItemStack tool = context.get(LootContextParameters.TOOL);
		if (tool == null || tool.isEmpty()) {
			return loot;
		}

		List<LootProperty> properties = PropertyDispatcher.active(tool, LootProperty.KEY, DynamicContext.empty());

		List<ItemStack> currentLoot = loot;
		for (LootProperty property : properties) {
			currentLoot = property.handler().handle(currentLoot, context);
		}

		return currentLoot;
	}

	public static List<ItemStack> handleEntityLoot(List<ItemStack> loot, LootContext context) {
		Entity killer = context.get(LootContextParameters.KILLER_ENTITY);
		if (killer == null) {
			return loot;
		}

		Optional<ItemStack> tool = getToolFromEntity(killer);
		if (tool.isEmpty() || tool.get().isEmpty()) {
			return loot;
		}

		DynamicContext.Builder dynamicContextBuilder = new DynamicContext.Builder();
		Entity killedEntity = context.get(LootContextParameters.THIS_ENTITY);
		if (killedEntity != null) {
			dynamicContextBuilder.put(ContextKeys.TARGET_TAGS, getEntityTags(killedEntity));
		}

		List<LootProperty> properties = PropertyDispatcher.active(tool.get(), LootProperty.KEY, dynamicContextBuilder.build());

		List<ItemStack> currentLoot = loot;
		for (LootProperty property : properties) {
			currentLoot = property.handler().handle(currentLoot, context);
		}

		return currentLoot;
	}

	private static Optional<ItemStack> getToolFromEntity(Entity entity) {
		if (entity instanceof LivingEntity living) {
			ItemStack stack = living.getMainHandStack();
			if (!stack.isEmpty()) {
				return Optional.of(stack);
			}
		}
		return Optional.empty();
	}

	private static Set<OpenIdentifier> getEntityTags(Entity entity) {
		return Registries.ENTITY_TYPE.getEntry(entity.getType())
				.streamTags()
				.map(TagKey::id)
				.map(id -> new OpenIdentifier(id.getNamespace(), id.getPath()))
				.collect(Collectors.toSet());
	}
}
