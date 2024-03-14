package com.sigmundgranaas.forgero.minecraft.common.predicate;

import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.ENTITY;
import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.ENTITY_TARGET;

import java.util.HashMap;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import com.sigmundgranaas.forgero.minecraft.common.predicate.flag.FlagGroupPredicate;

import net.minecraft.entity.Entity;


public class EntityPredicate implements Predicate<Entity>, Matchable {
	private final GroupEntry<KeyPair<Predicate<Entity>>> entry;
	private static final String type = "minecraft:entity_properties";
	public static final Map<String, Codec<KeyPair<Predicate<Entity>>>> CODEC_MAP = new HashMap<>();
	public static final MapCodec<GroupEntry<KeyPair<Predicate<Entity>>>> ENTITY_CODEC = new EntityCodec(type, CODEC_MAP);
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

	static {
		CODEC_MAP.put("flag", FlagGroupPredicate.CODEC_SPECIFICATION);
		CODEC_MAP.put("pos", EntityAdapter.entityPosCodec());
	}
}

