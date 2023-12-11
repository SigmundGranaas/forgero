package com.sigmundgranaas.forgero.minecraft.common.entity;

import static com.sigmundgranaas.forgero.minecraft.common.entity.SoulEntity.SOUL_ENTITIY_IDENTIFIER;
import static com.sigmundgranaas.forgero.minecraft.common.item.DynamicArrowEntity.DYNAMIC_ARROW_IDENTIFIER;

import com.sigmundgranaas.forgero.minecraft.common.item.DynamicArrowEntity;

import com.sigmundgranaas.forgero.minecraft.common.handler.use.ThrowableItem;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import static com.sigmundgranaas.forgero.minecraft.common.entity.SoulEntity.SOUL_ENTITIY_IDENTIFIER;
import static com.sigmundgranaas.forgero.minecraft.common.handler.use.ThrowableItem.THROWN_ENTITY_IDENTIFIER;

public class Entities {
	public static EntityType<SoulEntity> SOUL_ENTITY;
	public static EntityType<DynamicArrowEntity> DYNAMIC_ARROW_ENTITY;
	public static EntityType<ThrowableItem> THROWN_ITEM_ENTITY;


	public static void register() {
		DYNAMIC_ARROW_ENTITY = Registry.register(Registry.ENTITY_TYPE, DYNAMIC_ARROW_IDENTIFIER, EntityType.Builder.create((EntityType<DynamicArrowEntity> entity, World world) -> new DynamicArrowEntity(entity, world), SpawnGroup.MISC).build(DYNAMIC_ARROW_IDENTIFIER.toString()));
		SOUL_ENTITY = Registry.register(Registry.ENTITY_TYPE, SOUL_ENTITIY_IDENTIFIER, EntityType.Builder.create((EntityType<SoulEntity> entity, World world) -> new SoulEntity(entity, world), SpawnGroup.MISC).setDimensions(0.5f, 0.5f).maxTrackingRange(6).trackingTickInterval(2).disableSaving().build(SOUL_ENTITIY_IDENTIFIER.toString()));
		THROWN_ITEM_ENTITY = Registry.register(Registry.ENTITY_TYPE, THROWN_ENTITY_IDENTIFIER, EntityType.Builder.create((EntityType<ThrowableItem> entity, World world) -> new ThrowableItem(entity, world), SpawnGroup.MISC).build(THROWN_ENTITY_IDENTIFIER.toString()));
	}
}
