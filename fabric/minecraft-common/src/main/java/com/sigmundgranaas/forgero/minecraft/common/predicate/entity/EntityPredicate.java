package com.sigmundgranaas.forgero.minecraft.common.predicate.entity;

import static com.sigmundgranaas.forgero.minecraft.common.api.v0.predicate.Registries.ENTITY_CODEC_REGISTRY;
import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.ENTITY;
import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.ENTITY_TARGET;

import java.util.function.Predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import com.sigmundgranaas.forgero.minecraft.common.predicate.codecs.GroupEntry;
import com.sigmundgranaas.forgero.minecraft.common.predicate.codecs.KeyPair;
import com.sigmundgranaas.forgero.minecraft.common.predicate.codecs.SpecificationBackedPredicateCodec;

import net.minecraft.entity.Entity;


public class EntityPredicate implements Predicate<Entity>, Matchable {
	private final GroupEntry<KeyPair<Predicate<Entity>>> entry;
	private static final String type = "minecraft:entity";
	public static final MapCodec<GroupEntry<KeyPair<Predicate<Entity>>>> ENTITY_CODEC = new SpecificationBackedPredicateCodec<>(type, ENTITY_CODEC_REGISTRY);
	public static final Codec<EntityPredicate> CODEC = new MapCodec.MapCodecCodec<>(ENTITY_CODEC.xmap(EntityPredicate::new, entityPredicate -> entityPredicate.entry));

	public EntityPredicate(GroupEntry<KeyPair<Predicate<Entity>>> entry) {
		this.entry = entry;
	}

	@Override
	public boolean test(Entity entity) {
		return entry.entries().stream()
				.map(KeyPair::value)
				.allMatch(specification -> specification.test(entity));
	}

	@Override
	public boolean test(Matchable match, MatchContext context) {
		var entity = context.get(ENTITY)
				.map(this::test)
				.orElse(false);

		if (entity) {
			return true;
		}
		return context.get(ENTITY_TARGET)
				.map(this::test)
				.orElse(false);
	}
}

