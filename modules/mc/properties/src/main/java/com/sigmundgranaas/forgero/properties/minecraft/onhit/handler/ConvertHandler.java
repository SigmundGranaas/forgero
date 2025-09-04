package com.sigmundgranaas.forgero.properties.minecraft.onhit.handler;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Optional;

public record ConvertHandler(Identifier convertTo) implements OnHitHandler {
	public static final String TYPE = "forgero:convert";
	public static final Codec<ConvertHandler> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.fieldOf("convert_to").forGetter(ConvertHandler::convertTo)
	).apply(instance, ConvertHandler::new));

	@Override
	public void onHit(Entity source, Entity target) {
		if (!target.getWorld().isClient) {
			Optional<EntityType<?>> entityType = Registries.ENTITY_TYPE.getOrEmpty(convertTo);

			entityType.ifPresent(type -> {
				Entity newEntity = type.create(target.getWorld());
				if (newEntity != null) {
					newEntity.copyPositionAndRotation(target);
					target.getWorld().spawnEntity(newEntity);
					target.remove(Entity.RemovalReason.DISCARDED);
				}
			});
		}
	}

	@Override
	public String type() {
		return TYPE;
	}
}
