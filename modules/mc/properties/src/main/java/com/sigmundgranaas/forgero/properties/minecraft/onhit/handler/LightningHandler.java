package com.sigmundgranaas.forgero.properties.minecraft.onhit.handler;

import com.mojang.serialization.Codec;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;

public record LightningHandler() implements OnHitHandler {
	public static final String TYPE = "forgero:lightning";
	public static final LightningHandler INSTANCE = new LightningHandler();
	public static final Codec<LightningHandler> CODEC = Codec.unit(INSTANCE);

	@Override
	public void onHit(Entity source, Entity target) {
		if (!target.getWorld().isClient) {
			LightningEntity lightningBolt = EntityType.LIGHTNING_BOLT.create(target.getWorld());
			if (lightningBolt != null) {
				lightningBolt.refreshPositionAfterTeleport(target.getX(), target.getY(), target.getZ());
				target.getWorld().spawnEntity(lightningBolt);
			}
		}
	}

	@Override
	public String type() {
		return TYPE;
	}
}
