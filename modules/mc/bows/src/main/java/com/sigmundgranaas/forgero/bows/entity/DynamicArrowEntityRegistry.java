package com.sigmundgranaas.forgero.bows.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Registry for DynamicArrowEntity.
 * Follows the same pattern as ThrownItemEntityRegistry for consistency.
 */
public final class DynamicArrowEntityRegistry {
	public static final Identifier IDENTIFIER = new Identifier("forgero", "dynamic_arrow");
	public static EntityType<DynamicArrowEntity> DYNAMIC_ARROW_ENTITY;

	private static boolean registered = false;

	/**
	 * Registers the DynamicArrowEntity type.
	 * Safe to call multiple times (idempotent).
	 */
	public static void register() {
		if (registered) {
			return;
		}

		DYNAMIC_ARROW_ENTITY = Registry.register(
				Registries.ENTITY_TYPE,
				IDENTIFIER,
				EntityType.Builder.<DynamicArrowEntity>create(
						DynamicArrowEntity::new,
						SpawnGroup.MISC
				).build(IDENTIFIER.toString())
		);

		registered = true;
	}

	private DynamicArrowEntityRegistry() {
		// Prevent instantiation
	}
}
