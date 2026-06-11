package com.sigmundgranaas.forgero.properties.minecraft.onsneak;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.runtime.ContextKeys;
import com.sigmundgranaas.forgero.common.runtime.DynamicContext;
import com.sigmundgranaas.forgero.common.runtime.RuntimeConditions;
import com.sigmundgranaas.forgero.effects.entity.ContextualEffectHandler;
import com.sigmundgranaas.forgero.effects.entity.EntityEffectHandler;
import com.sigmundgranaas.forgero.effects.entity.OnHitEffect;
import com.sigmundgranaas.forgero.common.api.ForgeroApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manager for handling On-Sneak-Toggle events with state tracking.
 * Tracks sneak state per entity to detect toggle events (start sneaking).
 */
public class OnSneakToggleManager {
	// Track sneak state per entity UUID
	private static final ConcurrentHashMap<UUID, Boolean> sneakStates = new ConcurrentHashMap<>();

	static {
		// Register player disconnect cleanup hook (server-side only)
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
			ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
				cleanup(handler.player.getUuid());
			});
		}
	}

	private OnSneakToggleManager() {
		// Static class
	}

	/**
	 * Called every tick to check for sneak toggle events.
	 * Detects when an entity starts sneaking and triggers effects.
	 *
	 * @param entity The entity to check
	 */
	public static void handleTick(LivingEntity entity) {
		if (entity.getWorld().isClient()) {
			return; // Server-side only
		}

		boolean currentlySneak = entity.isSneaking();
		boolean previouslySneak = sneakStates.getOrDefault(entity.getUuid(), false);

		// Detect toggle (started sneaking)
		if (currentlySneak && !previouslySneak) {
			triggerSneakEffects(entity);
		}

		// Update state
		sneakStates.put(entity.getUuid(), currentlySneak);
	}

	/**
	 * Triggers effects from equipped items when sneak is toggled on.
	 */
	private static void triggerSneakEffects(LivingEntity entity) {
		// Collect all equipment (armor + held items)
		List<ItemStack> equipment = new ArrayList<>();
		entity.getHandItems().forEach(equipment::add);
		entity.getArmorItems().forEach(equipment::add);

		// Build context with entity tags
		DynamicContext.Builder contextBuilder = new DynamicContext.Builder();
		Set<OpenIdentifier> entityTags = Registries.ENTITY_TYPE.getEntry(entity.getType())
				.streamTags()
				.map(TagKey::id)
				.map(id -> new OpenIdentifier(id.getNamespace(), id.getPath()))
				.collect(Collectors.toSet());
		contextBuilder.put(ContextKeys.TARGET_TAGS, entityTags);

		// Process each equipped item
		for (ItemStack stack : equipment) {
			if (stack.isEmpty()) {
				continue;
			}

			List<OnSneakToggleProperty> properties = RuntimeConditions.filter(ForgeroApi.itemProperty().resolve(stack, OnSneakToggleProperty.Engine::new), contextBuilder.build());

			for (OnSneakToggleProperty property : properties) {
				// Entity sneaks targeting self or nearby entities
				List<Entity> finalTargets = property.selector().select(entity, entity);

				for (Entity finalTarget : finalTargets) {
					for (OnHitEffect effect : property.effects()) {
						if (effect instanceof ContextualEffectHandler contextual) {
							contextual.apply(entity, finalTarget);
						} else if (effect instanceof EntityEffectHandler simple) {
							simple.apply(finalTarget);
						}
					}
				}
			}
		}
	}

	/**
	 * Cleans up tracked state for an entity when they disconnect.
	 * IMPORTANT: This must be called on player disconnect to prevent memory leaks.
	 *
	 * @param entityId The UUID of the entity to clean up
	 */
	public static void cleanup(UUID entityId) {
		sneakStates.remove(entityId);
	}

	/**
	 * Clears all tracked states. Useful for testing or server shutdown.
	 */
	public static void clearAll() {
		sneakStates.clear();
	}
}
