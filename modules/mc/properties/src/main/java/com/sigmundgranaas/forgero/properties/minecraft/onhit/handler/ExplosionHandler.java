package com.sigmundgranaas.forgero.properties.minecraft.onhit.handler;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import java.util.Locale;

public record ExplosionHandler(float power, boolean createFire, World.ExplosionSourceType destructionType) implements OnHitHandler {
	public static final String TYPE = "forgero:explosion";
	public static final Codec<ExplosionHandler> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.FLOAT.fieldOf("power").forGetter(ExplosionHandler::power),
			Codec.BOOL.optionalFieldOf("create_fire", false).forGetter(ExplosionHandler::createFire),
			Codec.STRING.optionalFieldOf("destruction_type", World.ExplosionSourceType.NONE.toString()).forGetter( handler -> handler.destructionType.toString())
	).apply(instance, (power, createFire, destructionType) -> new ExplosionHandler(power, createFire, World.ExplosionSourceType.valueOf(destructionType.toUpperCase(Locale.ROOT)))));

	@Override
	public void onHit(Entity source, Entity target) {
		if (!target.getWorld().isClient) {
			target.getWorld().createExplosion(source, target.getX(), target.getY(), target.getZ(), power, createFire, destructionType);
		}
	}

	@Override
	public String type() {
		return TYPE;
	}
}
