package com.sigmundgranaas.forgero.minecraft.common.entity;

import static com.sigmundgranaas.forgero.minecraft.common.entity.SoulEntity.SOUL_ENTITIY_IDENTIFIER;
import static com.sigmundgranaas.forgero.minecraft.common.item.DynamicArrowEntity.DYNAMIC_ARROW_IDENTIFIER;

import com.sigmundgranaas.forgero.minecraft.common.item.DynamicArrowEntity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class Entities {
	public static EntityType<SoulEntity> SOUL_ENTITY;
	public static EntityType<DynamicArrowEntity> DYNAMIC_ARROW_ENTITY;

	public static void register() {
		SOUL_ENTITY = Registry.register(Registry.ENTITY_TYPE, SOUL_ENTITIY_IDENTIFIER, EntityType.Builder.create((EntityType<SoulEntity> entity, World world) -> new SoulEntity(entity, world), SpawnGroup.MISC).setDimensions(0.5f, 0.5f).maxTrackingRange(6).trackingTickInterval(2).build(SOUL_ENTITIY_IDENTIFIER.toString()));
		DYNAMIC_ARROW_ENTITY = Registry.register(Registry.ENTITY_TYPE, DYNAMIC_ARROW_IDENTIFIER, EntityType.Builder.create((EntityType<DynamicArrowEntity> entity, World world) -> new DynamicArrowEntity(entity, world), SpawnGroup.MISC).build(DYNAMIC_ARROW_IDENTIFIER.toString()));
	}
}
