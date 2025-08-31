package com.sigmundgranaas.forgero.properties.minecraft.onhit;

import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import com.sigmundgranaas.forgero.core.property.context.ContextKeys;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OnHitManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(OnHitManager.class);
	private static ComponentConverter converter;
	private static Resolver resolver;
	private static boolean initialized = false;

	private OnHitManager() {
		// Static class
	}

	public static void initialize(ComponentConverter converter, Resolver resolver) {
		if (initialized) {
			LOGGER.warn("OnHitManager is being initialized more than once. This may indicate an issue.");
			return;
		}
		OnHitManager.converter = converter;
		OnHitManager.resolver = resolver;
		OnHitManager.initialized = true;
	}

	public static void handleOnHit(ItemStack stack, Entity source, Entity target) {
		if (!initialized || stack.isEmpty()) {
			return;
		}

		converter.toComponent(stack).ifPresent(component -> {
			List<OnHitProperty> properties = getActiveProperties(component, target);
			properties.forEach(prop -> prop.handler().onHit(source, target));
		});
	}

	private static List<OnHitProperty> getActiveProperties(Component component, Entity target) {
		var engine = new OnHitProperty.Engine();
		DynamicContext.Builder contextBuilder = new DynamicContext.Builder();

		// Populate context with target tags for conditions
		Set<OpenIdentifier> targetTags = Registries.ENTITY_TYPE.getEntry(target.getType())
				.streamTags()
				.map(TagKey::id)
				.map(id -> new OpenIdentifier(id.getNamespace(), id.getPath()))
				.collect(Collectors.toSet());

		contextBuilder.put(ContextKeys.TARGET_TAGS, targetTags);

		return resolver.resolve(component, engine, contextBuilder.build());
	}
}
