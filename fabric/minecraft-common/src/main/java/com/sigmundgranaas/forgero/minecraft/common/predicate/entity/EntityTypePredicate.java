package com.sigmundgranaas.forgero.minecraft.common.predicate.entity;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.minecraft.common.predicate.Predicate;
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

	public static final Codec<Predicate<EntityType<?>>> GENERAL_CODEC = new Codec<>() {
		@Override
		public <T> DataResult<Pair<Predicate<EntityType<?>>, T>> decode(DynamicOps<T> ops, T input) {
			return CODEC.decode(ops, input).map(pair -> pair.mapFirst(xyz -> xyz));
		}

		@Override
		public <T> DataResult<T> encode(Predicate<EntityType<?>> input, DynamicOps<T> ops, T prefix) {
			return CODEC.encode((EntityTypePredicate) input, ops, prefix);
		}
	};

}

