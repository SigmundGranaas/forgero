package com.sigmundgranaas.forgero.minecraft.common.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import static com.sigmundgranaas.forgero.minecraft.common.entity.SoulEntity.SOUL_ENTITIY_IDENTIFIER;

public class Entities {
    public static EntityType<SoulEntity> SOUL_ENTITY;

    public static void register() {
        SOUL_ENTITY = Registry.register(Registry.ENTITY_TYPE, SOUL_ENTITIY_IDENTIFIER, EntityType.Builder.create((EntityType<SoulEntity> entity, World world) -> new SoulEntity(entity, world), SpawnGroup.MISC).setDimensions(5.5f, 5.5f).maxTrackingRange(6).trackingTickInterval(20).build(SOUL_ENTITIY_IDENTIFIER.toString()));
    }
}
