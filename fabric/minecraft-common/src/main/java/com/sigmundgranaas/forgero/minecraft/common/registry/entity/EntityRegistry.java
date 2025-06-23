package com.sigmundgranaas.forgero.minecraft.common.entity;

import static com.sigmundgranaas.forgero.minecraft.common.handler.use.ThrowableItem.THROWN_ENTITY_IDENTIFIER;

import com.sigmundgranaas.forgero.minecraft.common.handler.use.ThrowableItem;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import net.minecraft.world.World;

public class Entities {
	public static EntityType<ThrowableItem> THROWN_ITEM_ENTITY;


	public static void register() {
		THROWN_ITEM_ENTITY = Registry.register(Registries.ENTITY_TYPE, THROWN_ENTITY_IDENTIFIER, EntityType.Builder.create((EntityType<ThrowableItem> entity, World world) -> new ThrowableItem(entity, world), SpawnGroup.MISC).build(THROWN_ENTITY_IDENTIFIER.toString()));
	}
}
