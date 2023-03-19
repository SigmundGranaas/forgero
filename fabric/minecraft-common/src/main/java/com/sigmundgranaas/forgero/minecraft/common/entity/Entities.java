package com.sigmundgranaas.forgero.minecraft.common.entity;

import static com.sigmundgranaas.forgero.minecraft.common.entity.SoulEntity.SOUL_ENTITIY_IDENTIFIER;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.World;

public class Entities {
	public static EntityType<SoulEntity> SOUL_ENTITY;

	public static void register() {
		SOUL_ENTITY = Registry.register(Registries.ENTITY_TYPE, SOUL_ENTITIY_IDENTIFIER, EntityType.Builder.create((EntityType<SoulEntity> entity, World world) -> new SoulEntity(entity, world), SpawnGroup.MISC).setDimensions(0.5f, 0.5f).maxTrackingRange(6).trackingTickInterval(2).build(SOUL_ENTITIY_IDENTIFIER.toString()));
	}
}
