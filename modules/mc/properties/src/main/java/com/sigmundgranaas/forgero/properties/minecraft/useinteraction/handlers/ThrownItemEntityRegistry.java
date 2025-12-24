package com.sigmundgranaas.forgero.properties.minecraft.useinteraction.handlers;

import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.entity.ThrownItemEntity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

/**
 * Registry for the ThrownItemEntity.
 * This class handles registration of the entity type with Minecraft's registry.
 */
public final class ThrownItemEntityRegistry {

	public static EntityType<ThrownItemEntity> THROWN_ITEM_ENTITY;

	private static boolean registered = false;

	private ThrownItemEntityRegistry() {
		// Static utility class
	}

	/**
	 * Registers the ThrownItemEntity type.
	 * This should be called during mod initialization.
	 */
	public static void register() {
		if (registered) {
			return;
		}

		THROWN_ITEM_ENTITY = Registry.register(
				Registries.ENTITY_TYPE,
				ThrownItemEntity.IDENTIFIER,
				EntityType.Builder.<ThrownItemEntity>create(
						ThrownItemEntity::new,
						SpawnGroup.MISC
				).build(ThrownItemEntity.IDENTIFIER.toString())
		);

		registered = true;
	}

	/**
	 * @return true if the entity has been registered
	 */
	public static boolean isRegistered() {
		return registered;
	}
}
