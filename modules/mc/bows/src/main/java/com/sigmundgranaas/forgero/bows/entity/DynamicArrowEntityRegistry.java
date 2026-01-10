package com.sigmundgranaas.forgero.bows.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class DynamicArrowEntityRegistry {
	public static final Identifier IDENTIFIER = new Identifier("forgero", "dynamic_arrow");
	
	public static final EntityType<DynamicArrowEntity> DYNAMIC_ARROW_ENTITY = Registry.register(
			Registries.ENTITY_TYPE,
			IDENTIFIER,
			EntityType.Builder.<DynamicArrowEntity>create(
					DynamicArrowEntity::new,
					SpawnGroup.MISC
			)
			.setDimensions(0.5f, 0.5f)
			.maxTrackingRange(4)
			.trackingTickInterval(20)
			.build(IDENTIFIER.toString())
	);

	public static void register() {
	}

	private DynamicArrowEntityRegistry() {
	}
}
