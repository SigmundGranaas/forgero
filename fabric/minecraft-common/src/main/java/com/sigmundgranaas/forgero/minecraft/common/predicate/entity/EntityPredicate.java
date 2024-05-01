package com.sigmundgranaas.forgero.minecraft.common.predicate.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import com.sigmundgranaas.forgero.minecraft.common.predicate.GroupEntry;
import com.sigmundgranaas.forgero.minecraft.common.predicate.KeyPair;
import com.sigmundgranaas.forgero.minecraft.common.predicate.Predicate;
import net.minecraft.entity.Entity;

import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.ENTITY;
import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.ENTITY_TARGET;
import static com.sigmundgranaas.forgero.minecraft.common.predicate.entity.EntityRegistries.ENTITY_CODEC_REGISTRY;


public class EntityPredicate implements Predicate<Entity>, Matchable {
	private final GroupEntry<KeyPair<Predicate<Entity>>> entry;
	private static final String type = "minecraft:entity";
	public static final MapCodec<GroupEntry<KeyPair<Predicate<Entity>>>> ENTITY_CODEC = new EntityCodec(type, ENTITY_CODEC_REGISTRY);
	public static final Codec<EntityPredicate> CODEC = new MapCodec.MapCodecCodec<>(ENTITY_CODEC.xmap(EntityPredicate::new, entityPredicate -> entityPredicate.entry));

	public EntityPredicate(GroupEntry<KeyPair<Predicate<Entity>>> entry) {
		this.entry = entry;
	}

	@Override
	public boolean test(Entity entity) {
		return entry.entries().stream().allMatch(specification -> specification.value().test(entity));
	}

	@Override
	public boolean test(Matchable match, MatchContext context) {
		return context.get(ENTITY).or(() -> context.get(ENTITY_TARGET)).map(this::test).orElse(false);
	}
}

