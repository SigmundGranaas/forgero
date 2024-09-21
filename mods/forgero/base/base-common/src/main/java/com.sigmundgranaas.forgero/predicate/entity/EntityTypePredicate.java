package com.sigmundgranaas.forgero.predicate.entity;

import java.util.function.Predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public record EntityTypePredicate(Identifier identifier) implements Predicate<EntityType<?>> {
	@Override
	public boolean test(EntityType<?> entityType) {
		return Registries.ENTITY_TYPE.get(identifier) == entityType;
	}

	public static final Codec<EntityTypePredicate> CODEC = RecordCodecBuilder
			.create(instance -> instance.group(
							Identifier.CODEC.fieldOf("id").forGetter(EntityTypePredicate::identifier))
					.apply(instance, EntityTypePredicate::new));

}

