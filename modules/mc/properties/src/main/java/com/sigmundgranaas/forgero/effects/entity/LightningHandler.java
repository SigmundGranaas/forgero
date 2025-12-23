package com.sigmundgranaas.forgero.effects.entity;

import com.mojang.serialization.Codec;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;

public record LightningHandler() implements EntityEffectHandler {
	public static final String TYPE = "forgero:lightning";
	public static final LightningHandler INSTANCE = new LightningHandler();
	public static final Codec<LightningHandler> CODEC = Codec.unit(INSTANCE);

	@Override
	public void apply(Entity entity) {
		if (!entity.getWorld().isClient) {
			LightningEntity lightningBolt = EntityType.LIGHTNING_BOLT.create(entity.getWorld());
			if (lightningBolt != null) {
				lightningBolt.refreshPositionAfterTeleport(entity.getX(), entity.getY(), entity.getZ());
				entity.getWorld().spawnEntity(lightningBolt);
			}
		}
	}

	@Override
	public String type() {
		return TYPE;
	}
}
